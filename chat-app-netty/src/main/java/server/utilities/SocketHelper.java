package server.utilities;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

public class SocketHelper {

    /**
     * Broadcast the specified message to all registered channels except the sender.
     *
     * @param context Message context
     * @param message Message to broadcast
     */

    public static void broadcast(ChannelHandlerContext context, Map<String, ChannelGroup> channelGroupMap,
                           String conversationsID, String message) {

//        logger.info("Broadcast message in conversation " + roomid );

        for (Channel channel: channelGroupMap.get(conversationsID)) {
//            if (channel != context.getChannel()) {
//                channel.write(new TextWebSocketFrame(message));
//            }

            channel.write(new TextWebSocketFrame(message));
        }
    }

}
