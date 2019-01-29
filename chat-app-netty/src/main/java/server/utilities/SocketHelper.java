package server.utilities;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import server.WebSocketServer;

import java.net.URL;
import java.util.Map;

public class SocketHelper {

    static {
        init();
    }

    private final static Logger logger = Logger.getLogger(SocketHelper.class);

    /**
     * Broadcast the specified message to all registered channels except the sender.
     *
     * @param context Message context
     * @param message Message to broadcast
     */

    public static void broadcast(ChannelHandlerContext context, Map<String, ChannelGroup> channelGroupMap,
                                 String conversationsID, String message) {

//        logger.info("Broadcast message in conversation " + roomid );

        if (channelGroupMap.containsKey(conversationsID)){
            for (Channel channel : channelGroupMap.get(conversationsID)) {
//            if (channel != context.getChannel()) {
//                channel.write(new TextWebSocketFrame(message));
//            }

                channel.write(new TextWebSocketFrame(message));
                logger.info("BroadcastMessage to ConversationID: " + conversationsID);
            }
        }
        else {
            logger.info("Don't have conversationsID: " + conversationsID + " in channelGroupMap");
        }


    }

    public static void broadcastOnlyChannel(ChannelHandlerContext context, String message) {

//        logger.info("Broadcast message in conversation " + roomid );

        context.getChannel().write(new TextWebSocketFrame(message));
    }

    /**
     * method to init log4j configurations
     */
    private static void init() {
        URL u = WebSocketServer.class.getClassLoader().getResource("./log4j.xml");
        DOMConfigurator.configure(u);
    }

}
