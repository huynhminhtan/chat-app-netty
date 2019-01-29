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
import server.utilities.HttpHelper;
import server.utilities.RedissonHelper;

import java.net.URL;

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
        messagesDTORMap = RedissonHelper.getRedisson().getMap(conversationID);


        // response to client
        HttpHelper.sendHttpResponse(context, request, "okay");
        return;
    }

    /**
     * method to init log4j configurations
     */
    private static void init() {
        URL u = WebSocketServer.class.getClassLoader().getResource("./log4j.xml");
        DOMConfigurator.configure(u);
    }
}
