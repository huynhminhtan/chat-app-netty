package server.api;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

import static server.api.LoadFriends.handleLoadFriends;
import static server.api.Messages.loadMessagesByConversationID;
import static server.api.Login.*;
import static server.api.Register.handleRegister;

public class RouterAPI {

    public static void router(ChannelHandlerContext context, HttpRequest request, Logger logger){

        String uriAPI = request.getUri();
        System.out.println(uriAPI);


        switch (uriAPI) {
            case  "/getListConversations":
                getListConversations(context, request, logger);
                break;
            case "/getListMessagesByConversations":
                getListMessagesByConversations(context, request, logger);
                break;
            case "/login":
                handleLogin(context, request, logger);
                break;
            case "/register":
                handleRegister(context, request, logger);
                break;
            case "/loadFriends":
                handleLoadFriends(context, request, logger);
                break;
            case "/loadMessagesByConversationID":
                loadMessagesByConversationID(context, request);
                break;
        }

    }
}

