package server;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * An HTTP server which serves web socket requests at http://localhost:8080/websocket
 */
public class WebSocketServer {
    private final int port;
    static{
        init();
    }
    private final static Logger logger = Logger.getLogger(WebSocketServer.class);

    public WebSocketServer(int port) {
        this.port = port;
    }

    public void run() {
        // Bootstrap Netty
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline
        bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory());

        // Start processing messages
        bootstrap.bind(new InetSocketAddress(port));

        logger.info("Netty web socket server started at port " + port + '!');
    }

    public static void main(String[] args) {
        new WebSocketServer(6898).run();
    }

    /**
     * method to init log4j configurations
     */
    private static void init() {
           URL u = WebSocketServer.class.getClassLoader().getResource("./log4j.xml");
           DOMConfigurator.configure(u);
    }

}
