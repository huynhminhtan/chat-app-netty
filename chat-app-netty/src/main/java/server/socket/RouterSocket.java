package server.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.redisson.api.RMap;
import server.modules.ConversationsDTO;
import server.modules.ConversationsResponseDTO;
import server.modules.SocketDTO;
import server.utilities.RedissonHelper;
import server.utilities.SocketHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RouterSocket {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private static Map<String, ChannelGroup> channelGroupMap = new ConcurrentHashMap<String, ChannelGroup>();
    private static Map<String, ChannelGroup> userOnlines = new ConcurrentHashMap<String, ChannelGroup>();
    private static RMap<String, ConversationsDTO> conversationsDTORMap =
            RedissonHelper.getRedisson().getMap("CONVERSATIONS");
    ;

//    private static Map<String, ChannelGroup> channelGroupMap = new ConcurrentHashMap<String, ChannelGroup>();

//    private static Map<String, Conversations> = new

    public static void router(ChannelHandlerContext context, String message, Logger logger) {

//        System.out.println(message);
        String messageType = null;

        try {

            SocketDTO.SocketRequestTypeDTO socketRequestTypeDTO = gson.fromJson(message, SocketDTO.SocketRequestTypeDTO.class);
            messageType = socketRequestTypeDTO.getMessageType();

            System.out.println("MessageType: " + socketRequestTypeDTO.getMessageType());

            switch (messageType) {
                case "firstRequest":
                    firstRequest(context, message, logger);
                    break;
                case "createConversation":
                    createConversation(context, message, logger);
                    break;

            }

        } catch (Exception e) {
            System.out.println(e);
        }


    }

    private static void firstRequest(ChannelHandlerContext context, String message, Logger logger) {
//        System.out.println("first request");
//        System.out.println(message);

        SocketDTO.SocketFirstRequestDTO socketFirstRequestDTO =
                gson.fromJson(message, SocketDTO.SocketFirstRequestDTO.class);
        SocketDTO.SocketFirstRequest content = socketFirstRequestDTO.getContent();
        String userID = content.getUserID();

//        System.out.println(content.getUserID());

        // save user online
        saveUserOnline(context, userID);

        // load all conversations of current user
        loadAllConversationsForUser(context, userID);

    }

    private static void loadAllConversationsForUser(ChannelHandlerContext context, String userID) {

        ArrayList<ConversationsResponseDTO> listConversationsResponseDTO = new ArrayList<>();

        Set<Map.Entry<String, ConversationsDTO>> allEntries = conversationsDTORMap.readAllEntrySet();

        // get list conversations of current user to update channel ID
        for (Map.Entry<String, ConversationsDTO> entry : allEntries) {
//            String key = entry.getKey();
            ConversationsDTO cvs = entry.getValue();

            // get list conversations of current user
            for (String uID : cvs.getUsers()) {
//                System.out.println(userID);

                if (uID.equals(userID)) {
                    listConversationsResponseDTO.add(
                            new ConversationsResponseDTO(cvs.getConversationsID(), cvs.getConversationsName())
                    );
                }

            }

//            listUsers.add(new UserResponseDTO(user.getUserName(), user.getPhone()));
//            System.out.println("\n");
//            System.out.println(cvs.getConversationsID() + " " + cvs.getConversationsName() + " " + cvs.getUsers());
        }

        String resContent = gson.toJson(listConversationsResponseDTO);
        SocketHelper.broadcastOnlyChannel(context, resContent);

    }

    private static void saveUserOnline(ChannelHandlerContext context, String userID) {
        if (!userOnlines.containsKey(userID)) {
            userOnlines.put(userID, new DefaultChannelGroup());
        }

        userOnlines.get(userID).add(context.getChannel());
    }

    private static void createConversation(ChannelHandlerContext context, String message, Logger logger) {


        SocketDTO.SocketCreateConversationDTO socketCreateConversationDTO =
                gson.fromJson(message, SocketDTO.SocketCreateConversationDTO.class);
        SocketDTO.SocketCreateConversation content = socketCreateConversationDTO.getContent();

        System.out.println(content.getConversationName());
        System.out.println(content.getUsers());

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

            // response
            // getRBucket user from redis
//            ConversationsDTO userLogin = conversationsDTORMap.get(cv.getConversationsID());

//            System.out.println("modo");
//            System.out.println(userLogin.getConversationsID());
//            System.out.println(userLogin.getUsers());
        }
        System.out.println("xxxx");


        // get list channelID for user of new conversation
        ConversationsDTO conversationsDTO = conversationsDTORMap.get(cv.getConversationsID());

        ChannelGroup listChannelForNewConversation = new DefaultChannelGroup();

        for (String userID : conversationsDTO.getUsers()) {

            System.out.println("1x");

            if (userOnlines.containsKey(userID)) {
                for (Channel channel : userOnlines.get(userID)) {

                    System.out.println("xx22222xx");

                    listChannelForNewConversation.add(channel);
                }
            }
        }


        // update channelGroupMap
//        channelGroupMap.get(cv.getConversationsID()).
        if (!channelGroupMap.containsKey(cv.getConversationsID())) {

            System.out.println("Update channelGroupMap");
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

        // broadcast notifi have new conversation
        SocketHelper.broadcast(context, channelGroupMap, cv.getConversationsID(), convertedObject.toString());
    }
}
