package server.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;
import org.redisson.api.RMap;
import server.WebSocketServer;
import server.modules.MessagesDTO;
import server.modules.MessagesResponseDTO;
import server.modules.UserDTO;
import server.utilities.HttpHelper;
import server.utilities.RedissonHelper;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Messages {

    static {
        init();
    }

    private static Gson gson = new GsonBuilder().serializeNulls().create();

    private final static Logger logger = Logger.getLogger(Messages.class);

    private static RMap<String, MessagesDTO> messagesDTORMap;

    public static void loadMessagesByConversationID(ChannelHandlerContext context, HttpRequest request) {

        logger.info("Handle api: " + request.getUri());

        ChannelBuffer buffer = request.getContent();
        String jsonPayload = buffer.toString(CharsetUtil.UTF_8);
        MessagesDTO messagesDTORequest = gson.fromJson(jsonPayload, MessagesDTO.class);
        String conversationID = messagesDTORequest.getConversationID();

        logger.info("Entry get messages for conversationID: " + conversationID);

        // get messages by conversationID from Redis
        ArrayList<MessagesResponseDTO> listMessages = new ArrayList<>();
        messagesDTORMap = RedissonHelper.getRedisson().getMap(conversationID);

        Set<Map.Entry<String, MessagesDTO>> allEntries = messagesDTORMap.readAllEntrySet();

        for (Map.Entry<String, MessagesDTO> entry : allEntries) {
//            String key = entry.getKey();
            MessagesDTO message = entry.getValue();

            listMessages.add(new MessagesResponseDTO(
                            message.getSender(),
                            getUsernameByUserID(message.getSender()),
                            message.getContent(),
                            message.getTime(),
                            message.getConversationID()
                    )
            );
        }

        logger.info("Size of list messages by conversationID: " + listMessages.size());

        // response to client
        String resContent = gson.toJson(listMessages);
        HttpHelper.sendHttpResponse(context, request, resContent);
        logger.info("Response list messages to channelID: " + context.getChannel().getId());

        return;
    }

    /**
     * method to init log4j configurations
     */
    private static void init() {
        URL u = WebSocketServer.class.getClassLoader().getResource("./log4j.xml");
        DOMConfigurator.configure(u);
    }

    public static String getUsernameByUserID(String userID) {
        RMap<String, UserDTO> userDTORMap = RedissonHelper.getRedisson().getMap("USERS");

        Set<Map.Entry<String, UserDTO>> allEntries = userDTORMap.readAllEntrySet();

        for (Map.Entry<String, UserDTO> entry : allEntries) {
            String key = entry.getKey();
            UserDTO user = entry.getValue();

            if (key.equals(userID))
                return user.getUserName();
        }

        return "Unknown";
    }
}
