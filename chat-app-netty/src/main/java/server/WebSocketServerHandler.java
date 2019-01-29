package server;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import server.api.RouterAPI;
import server.socket.RouterSocket;
import server.utilities.HttpHelper;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handle web socket connection requests (handshakes) and web socket messages.
 * Message are broadcast to all connected users. A connection should register its
 * username before sending messages.
 */
public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
    static{
        init();
    }
    private final static Logger logger = Logger.getLogger(WebSocketServer.class);

    private static final String WEBSOCKET_PATH = "/websocket";
    private static final ChannelGroup channels = new DefaultChannelGroup();

    private WebSocketServerHandshaker handshaker;
    private Map<Integer, String> userNameMap = new HashMap<Integer, String>();

    private String roomid;

    // memcache message
    private static final RedissonClient redisson = Redisson.create();

    // channelGroupMap save Name conversation and all channels ID of users connect with conversation
    private static Map<String, ChannelGroup> channelGroupMap = new ConcurrentHashMap<String, ChannelGroup>();

//   private static RMap<String, ChannelGroup> channelGroupMap = RedissonHelper.getRedisson().getMap("CHANNELGROUPMAP");


   public WebSocketServerHandler() {

   }

    /**
     * Process an incoming message. This could be a connection request or a web socket message.
     * Handshakes come in as HTTP upgrade requests. Handle these as well as any stray HTTP requests.
     * Handle web socket requests as well.
     *
     * @param context   Message context
     * @param event		Contains message
     */
    @Override
    public void messageReceived(ChannelHandlerContext context, MessageEvent event) throws Exception {
        Object message = event.getMessage();
        if (message instanceof HttpRequest) {
//            logger.info("Entry connect HTTP request");
            handleHttpRequest(context, (HttpRequest) message);
        } else if (message instanceof WebSocketFrame) {

//            logger.info("Entry connect socket");
            handleWebSocketFrame(context, (WebSocketFrame) message);
        }
    }

    /**
     * Handle HTTP requests, these are handshake requests to connect to a web socket as well as
     * any stray HTTP requests.
     *
     * @param context 	Message context
     * @param request	HTTP request
     * @throws Exception
     */
    private void handleHttpRequest(ChannelHandlerContext context, HttpRequest request) throws Exception {


        RouterAPI.router(context, request, logger);


        /**
         *
         * message request
         *
         *  null
         *
         * message response
         *  (protobuf: MsgResponse.Conversations)
         *
         * [
         *     {
         *         conversationName: "Uyển Vy"
         *         userCreate: "mtSiniChi"
         *     },
         *     {
         *         conversationName: "Class 12A"
         *         userCreate: "mtSiniChi"
         *     }
         * ]
         *
         */


        /**
         *
         * message request
         *  (protobuf: MsgRequest.Conversations)
         *
         *  { conversationName: "Class 12A" }
         *
         * message response
         *  (protobuf: MsgResponse.ListMessages)
         *
         * [
         *     {
         *         content: "Không nó chạy qua chạu lại"
         *         time: "01989876554122"
         *         userSend: "Hoài Linh"
         *     },
         *     {
         *         content: "Không nó chạy qua chạu lại"
         *         time: "10989876554122"
         *         userSend: "Hoài Linh"
         *     }
         * ]
         *
         */

        // Only process HTTP GET requests, ignore everything else
        if (request.getMethod() != GET) {
            HttpHelper.sendHttpResponse(context, request, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if (request.getUri().equals("/websocket") || request.getUri().equals("/websocket/?EIO=3&transport=websocket")  ) {

//            logger.info("Require handshaker " + request.getUri());

            // This is a web socket handshake request
            WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(this.getWebSocketLocation(request), null, false);
            this.handshaker = handshakerFactory.newHandshaker(request);
            if (this.handshaker == null) {
            	// The communication protocol is not supported
                handshakerFactory.sendUnsupportedWebSocketVersionResponse(context.getChannel());
            } else {
            	// Keep track of all connections

            	// Respond to the handshake
                this.handshaker.handshake(context.getChannel(), request).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
            }
        } else {

//            logger.info("Undefine request " + request.getUri());

            // Ignore anything except web socket handshake requests
            HttpHelper.sendHttpResponse(context, request, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
        }
    }

    /**
     * Handle pings, close connection request, and regular text message requests.
     *
     * @param context Message context
     * @param frame	  Web socket message frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext context, WebSocketFrame frame) {

//        logger.info("Handle websocket");

        if (frame instanceof CloseWebSocketFrame) {
        	// Close the connection
        	String username = getUsername(context.getChannel().getId());
        	logger.info("Disconnected: " + username);
        	this.handshaker.close(context.getChannel(), (CloseWebSocketFrame) frame);
//            broadcast(context, this.roomid,"[Server] " + username + " Disconnected");
        } else if (frame instanceof PingWebSocketFrame) {
        	// Pings are primarily used for keep alive
            context.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
        } else if (frame instanceof TextWebSocketFrame) {
        	// Process text based message
            handleWebSocketMessage(context, ((TextWebSocketFrame) frame).getText());
        } else {
            throw new UnsupportedOperationException(String.format("%s frame type not supported!", frame.getClass().getName()));
        }
    }

    /**
     * Dispatch the message according to its logcial type. The logical type can be register to
     * register the username, or message to send a message.
     *
     * @param context Message context
     * @param message Message text
     */
    private void handleWebSocketMessage(ChannelHandlerContext context, String message) {

//        System.out.println("xxxxxxxxx");
//        System.out.println(message);
//
//        Gson gson = new GsonBuilder().serializeNulls().create();
//        SocketDTO socketRequestDTO = gson.fromJson(message, SocketDTO.class);
//
//        System.out.println(socketRequestDTO.getMessageType());

        RouterSocket.router(context, message, logger);

//
//
//        // Dispatch message according to logical message type
//    	if (message.startsWith("register:")) {
//
//        	// Handle a registration message
//            String[] data = message.split(":");
//
//            String username = data[1];
//            this.roomid = data[2];
//
//            logger.info("Register " + username + ":" + roomid);
//
//            // if roomid not exist
//            if (!channelGroupMap.containsKey(this.roomid)) {
//                channelGroupMap.put(this.roomid, new DefaultChannelGroup());
//            }
//
//            channelGroupMap.get(this.roomid).add(context.getChannel());
//
//            userNameMap.put(context.getChannel().getId(), username);
//
//    		logger.info("Register Event: " + username + " :" + context.getChannel().getId());
//
//    		context.getChannel().write(new TextWebSocketFrame("[Server] Welcome " + username + " :" + context.getChannel().getId()));
//
//    		broadcast(context, this.roomid, "[Server] " + username + " Connected");
//
//    	} else if (message.startsWith("message:")) {
//            // Send the message to all channels, the sending channel gets an echo
//    		String username = getUsername(context.getChannel().getId());
//    		String userMessage = message.substring(message.indexOf(':') + 1).trim();
//
//            logger.info("Message: " + username + " : " + this.roomid + " : "+ userMessage);
//
//            // save message to redis
//
//            // lock only thread write data at this time
//            RReadWriteLock rwlock = redisson.getReadWriteLock("anyRWLock");
//
//            rwlock.writeLock().lock();
//
//            RMapCache<String, String> mapCache = redisson.getMapCache(this.roomid);
//            mapCache.setMaxSize(5);
//            mapCache.put(username + ":" + System.currentTimeMillis(), userMessage);
//
//            rwlock.writeLock().unlock();
//
//
//            // broadcast others channel except me (current channel)
//            broadcast(context, this.roomid,   "messageOther:" + username + ":" + this.roomid + ":" + userMessage);
//            context.getChannel().write(new TextWebSocketFrame("[Me] " + userMessage));
//    	} else {
//    		// Unknown logical message type
//    		logger.info("Unknown Message Type: " + message);
//            context.getChannel().write(new TextWebSocketFrame("[Server] Unknown message type!"));
//    	}



    }

    /**
     * Broadcast the specified message to all registered channels except the sender.
     *
     * @param context Message context
     * @param message Message to broadcast
     */

    private void broadcast(ChannelHandlerContext context, String roomid, String message) {

//        logger.info("Broadcast message in conversation " + roomid );

        for (Channel channel: channelGroupMap.get(roomid)) {
            if (channel != context.getChannel()) {
                channel.write(new TextWebSocketFrame(message));
            }
        }
    }

    /**
     * Lookup the username in the username map. Return Unregistered User if username not found.
     *
     * @param channelId
     * @return Username or Unregistered User of username not found
     */
    private String getUsername(Integer channelId) {
    	String username = userNameMap.get(channelId);
    	if (username == null) {
    		// User not registered
    		username = "Unregistered User";
    	}

    	return username;
    }

    /**
     * Send HTTP response. In our case this is an error response so send it and close the communication channel.
     *
     * @param context  Message context
     * @param request  HTTP request
     * @param response HTTP response
     */
//    private void sendHttpResponse(ChannelHandlerContext context, HttpRequest request, HttpResponse response) {
//        // Generate an error response if the response status code is not OK (200)
//        if (response.getStatus().getCode() != 200) {
//            response.setContent(ChannelBuffers.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8));
//            setContentLength(response, response.getContent().readableBytes());
//        }
//
//        /*
//         *  Send the response (channel.write) and close the connection if this is an error response.
//         *  Use a future because write is an asynchronous call.
//         */
//        ChannelFuture future = context.getChannel().write(response);
//        if (!isKeepAlive(request) || response.getStatus().getCode() != 200) {
//            future.addListener(ChannelFutureListener.CLOSE);
//        }
//    }

    /**
     * There was a fatal exception, log it and close the connection.
     *
     * @param context
     * @param exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent exception) throws Exception {
        exception.getCause().printStackTrace();
        userNameMap.remove(exception.getChannel().getId());
        exception.getChannel().close();
    }

    /**
     * Return the location string for this web socket.
     *
     * @param request
     * @return Location string for this web socket
     */
    private String getWebSocketLocation(HttpRequest request) {
        return "ws://" + request.getHeaders(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
    }

//    private void setOrigin(final HttpResponse response) {
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
//        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
//    }

    /**
     * method to init log4j configurations
     */
    private static void init() {
        URL u = WebSocketServer.class.getClassLoader().getResource("./log4j.xml");
        DOMConfigurator.configure(u);
    }

}
