package server.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.redisson.api.RMap;
import server.WebSocketServer;
import server.modules.ConversationsDTO;
import server.modules.MessagesDTO;
import server.modules.MessagesResponseDTO;
import server.modules.SocketDTO;
import server.utilities.RedissonHelper;
import server.utilities.SocketHelper;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static server.api.Messages.getUsernameByUserID;

public class RouterSocket {
    static {
        init();
    }

    private final static Logger logger = Logger.getLogger(RouterSocket.class);

    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private static Map<String, ChannelGroup> channelGroupMap = new ConcurrentHashMap<String, ChannelGroup>();
    private static Map<String, ChannelGroup> userOnlines = new ConcurrentHashMap<String, ChannelGroup>();
    private static RMap<String, ConversationsDTO> conversationsDTORMap =
            RedissonHelper.getRedisson().getMap("CONVERSATIONS");
    private static RMap<String, MessagesDTO> messagesDTORMap;

//    private static Map<String, ChannelGroup> channelGroupMap = new ConcurrentHashMap<String, ChannelGroup>();

//    private static Map<String, Conversations> = new

    public static void router(ChannelHandlerContext context, String message, Logger logger) {

//        System.out.println(message);
        String messageType = null;

        try {

            SocketDTO.SocketRequestTypeDTO socketRequestTypeDTO = gson.fromJson(message, SocketDTO.SocketRequestTypeDTO.class);
            messageType = socketRequestTypeDTO.getMessageType();

//            System.out.println("MessageType: " + socketRequestTypeDTO.getMessageType());

            logger.info("Socket Router");
            logger.info("MessageType: " + socketRequestTypeDTO.getMessageType());

            switch (messageType) {
                case "firstRequest":
                    firstRequest(context, message);
                    break;
                case "createConversation":
                    createConversation(context, message);
                    break;
                case "sendMessage":
                    sendMessage(context, message);
                    break;

            }

        } catch (Exception e) {
            System.out.println(e);
        }


    }

    private static void sendMessage(ChannelHandlerContext context, String message) {

        SocketDTO.SendMessageDTO sendMessageDTO =
                gson.fromJson(message, SocketDTO.SendMessageDTO.class);
        SocketDTO.SendMessage content = sendMessageDTO.getContent();

        String sender = content.getSender();
        String userMessage = content.getContent();
        String conversationID = content.getConversationID();

        logger.info("SendMessage:: UserID: " + sender +
                " send message: " + userMessage +
                " in conversationID: " + conversationID);

        // Save to Redis
        messagesDTORMap = RedissonHelper.getRedisson().getMap(conversationID);

        //// Create instance message
        MessagesDTO messagesDTO = new MessagesDTO();
        messagesDTO.setSender(sender);
        messagesDTO.setContent(userMessage);
        messagesDTO.setConversationID(conversationID);
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        messagesDTO.setTime(time);

        String key = messagesDTO.getTime() + messagesDTO.getSender();

        if (!messagesDTORMap.containsKey(key)) {

            // Save to Redis
            messagesDTORMap.put(key, messagesDTO);
            logger.info("SaveMessage:: '" + messagesDTO.getContent() + "' to Redis");
        }

        // Get message from Redis (option)
        messagesDTO = messagesDTORMap.get(key);

        MessagesResponseDTO messagesResponse = new MessagesResponseDTO(
                messagesDTO.getSender(),
                getUsernameByUserID(messagesDTO.getSender()),
                messagesDTO.getContent(),
                messagesDTO.getTime(),
                messagesDTO.getConversationID()
        );

        logger.info("GetMessage from Redis:: '" +
                messagesDTO.getContent() + "' in " +
                messagesDTO.getConversationID());

        // broadcast message to all channelID in conversationID
        SocketDTO.SendMessageResponseDTO sendMessageDTOResponse = new SocketDTO.SendMessageResponseDTO();
        sendMessageDTOResponse.setContent(messagesResponse);
        sendMessageDTOResponse.setMessageType("sendMessage");

        String messageResponse = gson.toJson(sendMessageDTOResponse);
        SocketHelper.broadcast(context, channelGroupMap, messagesResponse.getConversationID(), messageResponse);
    }

    private static void firstRequest(ChannelHandlerContext context, String message) {
//        System.out.println("first request");
//        System.out.println(message);

        SocketDTO.SocketFirstRequestDTO socketFirstRequestDTO =
                gson.fromJson(message, SocketDTO.SocketFirstRequestDTO.class);
        SocketDTO.SocketFirstRequest content = socketFirstRequestDTO.getContent();
        String userID = content.getUserID();

        logger.info("UserID: " + userID + " is first request");

//        System.out.println(content.getUserID());

        // save user online
        saveUserOnline(context, userID);

        // load all conversations of current user
        loadAllConversationsForUser(context, userID);

    }

    private static void loadAllConversationsForUser(ChannelHandlerContext context, String userID) {

        logger.info("Entry load all converstaions of UserID: " + userID);

        ArrayList<SocketDTO.LoadAllConversationsForUser> listConversationsResponseDTO = new ArrayList<>();

        Set<Map.Entry<String, ConversationsDTO>> allEntries = conversationsDTORMap.readAllEntrySet();

        // get list conversations of current user to update channel ID
        for (Map.Entry<String, ConversationsDTO> entry : allEntries) {
//            String key = entry.getKey();
            ConversationsDTO cvs = entry.getValue();

            // get list conversations of current user

            if (!cvs.getUsers().isEmpty()) {
                for (String uID : cvs.getUsers()) {

                    if (uID.equals(userID)) {
                        logger.info("UserID: " + userID + " have conversationID: " + cvs.getConversationsID());
                        listConversationsResponseDTO.add(
                                new SocketDTO.LoadAllConversationsForUser(
                                        cvs.getConversationsID(),
                                        cvs.getConversationsName()
                                )
                        );
                    }

                }
            } else {
                logger.info("ConversationsID: " + cvs.getConversationsID() + " is empty");
            }

//            listUsers.add(new UserResponseDTO(user.getUserName(), user.getPhone()));
//            System.out.println("\n");
//            System.out.println(cvs.getConversationsID() + " " + cvs.getConversationsName() + " " + cvs.getUsers());
        }

        logger.info("Size converstaions of UserID: " + listConversationsResponseDTO.size());

        // Update channelID by list conversationID
        for (SocketDTO.LoadAllConversationsForUser cvforUser : listConversationsResponseDTO) {
            String cvID = cvforUser.getConversationID();

            if(!channelGroupMap.containsKey(cvID)) {
                channelGroupMap.put(cvID, new DefaultChannelGroup());
            }

            channelGroupMap.get(cvID).add(context.getChannel());
        }

        // Sent all conversations to client
        SocketDTO.LoadAllConversationsForUserDTO loadAllConversationsForUserDTO =
                new SocketDTO.LoadAllConversationsForUserDTO();

        loadAllConversationsForUserDTO.setMessageType("loadAllConversationsForUser");
        loadAllConversationsForUserDTO.setContent(listConversationsResponseDTO);

        String resContent = gson.toJson(loadAllConversationsForUserDTO);
        SocketHelper.broadcastOnlyChannel(context, resContent);

    }

    private static void saveUserOnline(ChannelHandlerContext context, String userID) {
        if (!userOnlines.containsKey(userID)) {
            userOnlines.put(userID, new DefaultChannelGroup());
        }

        userOnlines.get(userID).add(context.getChannel());
        logger.info("UserID: " + userID + " is save to online");

    }

    private static void createConversation(ChannelHandlerContext context, String message) {

        logger.info("Entry create conversations");

        SocketDTO.SocketCreateConversationDTO socketCreateConversationDTO =
                gson.fromJson(message, SocketDTO.SocketCreateConversationDTO.class);
        SocketDTO.SocketCreateConversation content = socketCreateConversationDTO.getContent();

//        System.out.println(content.getConversationName());
//        System.out.println(content.getUsers());

        logger.info("Create conversationName: " + content.getConversationName());
        logger.info("Create conversation for users: " + content.getUsers());


        // save conversations for all user
//        conversationsDTORMap = RedissonHelper.getRedisson().getMap("CONVERSATIONS");

        // create new conversation
        String conversationID = String.valueOf(System.currentTimeMillis() / 1000);
        ConversationsDTO cv = new ConversationsDTO(
                conversationID,
                content.getConversationName(),
                content.getUsers()
        );

//        System.out.println("check value");
////        System.out.println(cv.getUsers());
//        System.out.println(cv.getConversationsID());
//        System.out.println(cv.getConversationsName());
//

        // account not exists
        if (!conversationsDTORMap.containsKey(cv.getConversationsID())) {

            // save to redis
            conversationsDTORMap.put(cv.getConversationsID(), cv);
            logger.info("Put new conversation["
                    + cv.getConversationsID() + ","
                    + cv.getConversationsName() + "] to Redis");


            // response
            // getRBucket user from redis
//            ConversationsDTO userLogin = conversationsDTORMap.get(cv.getConversationsID());

//            System.out.println("modo");
//            System.out.println(userLogin.getConversationsID());
//            System.out.println(userLogin.getUsers());
        }

        // get list channelID for user of new conversation
        ConversationsDTO conversationsDTO = conversationsDTORMap.get(cv.getConversationsID());

        ChannelGroup listChannelForNewConversation = new DefaultChannelGroup();

        for (String userID : conversationsDTO.getUsers()) {

            if (userOnlines.containsKey(userID)) {
                for (Channel channel : userOnlines.get(userID)) {

                    listChannelForNewConversation.add(channel);
                    logger.info("Add channelID: " + channel + " to list channel for new conversation");
                }
            }
        }

        // update channelGroupMap
//        channelGroupMap.get(cv.getConversationsID()).
        if (!channelGroupMap.containsKey(cv.getConversationsID())) {

            logger.info("Update ChannelGroupMap to save list channelID for new conversation");
            channelGroupMap.put(cv.getConversationsID(), listChannelForNewConversation);
        }

//        channelGroupMap.get(cv.getConversationsID()).add(context.getChannel());

//
//        String userIDTemp = "09888777162";
//
//        ArrayList<ConversationsDTO> listConversationsForUser = new ArrayList<>();
//
//        Set<Map.Entry<String, ConversationsDTO>> allEntries = conversationsDTORMap.readAllEntrySet();
//
//        // get list conversations of current user to update channel ID
//        for (Map.Entry<String, ConversationsDTO> entry : allEntries) {
////            String key = entry.getKey();
//            ConversationsDTO cvs = entry.getValue();
//
//            // get list conversations of current user
//            for (String userID : cvs.getUsers()) {
//                System.out.println(userID);
//                if (userID.equals(userIDTemp)) {
//                    listConversationsForUser.add(cvs);
//                }
//            }
//
////            listUsers.add(new UserResponseDTO(user.getUserName(), user.getPhone()));
////            System.out.println("\n");
//
//            System.out.println(cvs.getConversationsID() + " " + cvs.getConversationsName() + " " + cvs.getUsers());
//        }
//
////        System.out.println("-----------");
//
//        // update channel ID for all converstaions' current user
//        for (ConversationsDTO conversation : listConversationsForUser) {
//            System.out.println(conversation.getConversationsID());
//
//            if (!channelGroupMap.containsKey(conversation.getConversationsID())) {
//                channelGroupMap.put(conversation.getConversationsID(), new DefaultChannelGroup());
//            }
//
//            channelGroupMap.get(conversation.getConversationsID()).add(context.getChannel());
//        }

        String data = "{" +
                "messageType: 'createConversation'," +
                "content : {" +
                "conversationID: '" + cv.getConversationsID() + "'," +
                "conversationName : '" + cv.getConversationsName() + "'" +
                "}" +
                "}";

//        data = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(data, JsonObject.class);

        // broadcast notifi new conversation
        logger.info("Broadcast notifi new conversation for user is online");
        SocketHelper.broadcast(context, channelGroupMap, cv.getConversationsID(), convertedObject.toString());
    }

    /**
     * method to init log4j configurations
     */
    private static void init() {
        URL u = WebSocketServer.class.getClassLoader().getResource("./log4j.xml");
        DOMConfigurator.configure(u);
    }
}
