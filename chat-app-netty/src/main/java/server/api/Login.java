package server.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import protobuf.MsgRequest;
import protobuf.MsgResponse;
import server.modules.LoginResponseDTO;
import server.modules.UserDTO;
import server.utilities.HttpHelper;
import server.utilities.RedissonHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Login {

    private static final RedissonClient redisson = Redisson.create();


    public static void getListConversations(ChannelHandlerContext context, HttpRequest request, Logger logger) {
        logger.info("Handle api " + request.getUri());

        // create response
        HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
        res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHelper.setOrigin(res);

        // create demo list conversations
        ArrayList<MsgResponse.Conversations> staticListConverstation = new ArrayList<>();

        MsgResponse.Conversations conversation = MsgResponse.Conversations.newBuilder()
                .setConversationName("Uyển Vy")
                .setUserCreate("mtSiniChi")
                .build();
        staticListConverstation.add(conversation);

        conversation = MsgResponse.Conversations.newBuilder()
                .setConversationName("YogaClub")
                .setUserCreate("mtSiniChi")
                .build();
        staticListConverstation.add(conversation);

        conversation = MsgResponse.Conversations.newBuilder()
                .setConversationName("Tạ Hà Vy")
                .setUserCreate("mtSiniChi")
                .build();
        staticListConverstation.add(conversation);

        conversation = MsgResponse.Conversations.newBuilder()
                .setConversationName("Overnight party")
                .setUserCreate("mtSiniChi")
                .build();
        staticListConverstation.add(conversation);


        MsgResponse.ListConversations.Builder listConversationsSend = MsgResponse.ListConversations.newBuilder();
        listConversationsSend.addAllListConversations(staticListConverstation);


        ChannelBuffer bufferRe1 =  ChannelBuffers.copiedBuffer(listConversationsSend.build().toByteArray());
        setContentLength(res, bufferRe1.readableBytes());
        res.setContent(bufferRe1);

//            sendHttpResponse(context, request, res);
        HttpHelper.sendHttpResponse(context, request, res);
        return;
    }

    public static void getListMessagesByConversations(ChannelHandlerContext context, HttpRequest request, Logger logger) {

        logger.info("Handle api " + request.getUri());

        // convert to protobuf
        ChannelBuffer buffer = request.getContent();
        byte[] bytes = new byte[buffer.readableBytes()];
        while (buffer.readable()) {
            buffer.readBytes(bytes);
        }
        MsgRequest.Conversations conversation = null;
        try {
            conversation = MsgRequest.Conversations.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        String conversationName = conversation.getConversationName();

        //  create response
        HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
        res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHelper.setOrigin(res);

        // read data from redis
        ArrayList<MsgResponse.Messages> listMessages = new ArrayList<>();
        RMapCache<String, String> map = redisson.getMapCache(conversationName);
        Set<Map.Entry<String, String>> allEntries = map.readAllEntrySet();

        for (Map.Entry<String, String> entry : allEntries) {
            String sender = entry.getKey();
            String content = entry.getValue();

            String[] data = sender.split(":", 2);

            MsgResponse.Messages ms = MsgResponse.Messages.newBuilder()
                    .setContent(content)
                    .setUserSend(data[0])
                    .setTime(data[1])
                    .build();
            listMessages.add(ms);
        }

        // convert to protopuf
        MsgResponse.ListMessages.Builder listConversationsRes = MsgResponse.ListMessages.newBuilder();
        listConversationsRes.addAllListMessages(listMessages);

        ChannelBuffer bufferRes =  ChannelBuffers.copiedBuffer(listConversationsRes.build().toByteArray());
        setContentLength(res, bufferRes.readableBytes());
        res.setContent(bufferRes);

        HttpHelper.sendHttpResponse(context, request, res);
        return;
    }

    class mtSiniChi {
        private String name;
        private String id;
    }

    /**
     *
     *
     * {
     * 	"userName":"mtSiniChi",
     * 	"passworld": "1234567",
     * 	"phone": "0999888987"
     * }
     *
     * @param context
     * @param request
     * @param logger
     */


    public static void handleLogin(ChannelHandlerContext context, HttpRequest request, Logger logger) {
        ChannelBuffer buffer = request.getContent();

        String jsonPayload = buffer.toString(CharsetUtil.UTF_8);

        Gson gson = new Gson();
        UserDTO user = gson.fromJson(jsonPayload, UserDTO.class);

//        System.out.println(user.getPassword());
//        System.out.println(user.getPhone());
//        System.out.println(user.getUserName());

//        RedissonHelper.set("0999888771", new UserDTO("mtSiniChi", "123456", "0999888771"));

        // handle fail login
        if (!RedissonHelper.isExists(user.getPhone())){

            // build gson
            gson = new GsonBuilder().serializeNulls().create();
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO("fail", null);
            String resContent = gson.toJson(loginResponseDTO);

            // response to client
            HttpHelper.sendHttpResponse(context, request, resContent);
            return;
        }

        // handle success login
        UserDTO userLogin = (UserDTO) RedissonHelper.get(user.getPhone());

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO("success", userLogin);
        String resContent = gson.toJson(loginResponseDTO);

        // response to client
        HttpHelper.sendHttpResponse(context, request, resContent);
        return;

//        System.out.println("xxxxxxxxxxxqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
//
//        UserDTO ob = (UserDTO) RedissonHelper.get("0999888771");
//        System.out.println(ob.getPassword());
//        System.out.println(ob.getPhone());
//        System.out.println(ob.getUserName());

//        System.out.println(jsonPayload);





//        //  create response
//        HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
//
//        res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
//        HttpHelper.setOrigin(res);
//        String ns = "okay";
//        ChannelBuffer bufferRes =  ChannelBuffers.copiedBuffer(ns.getBytes());
//        setContentLength(res, bufferRes.readableBytes());
//        res.setContent(bufferRes);
//
//        HttpHelper.sendHttpResponse(context, request, res);

    }
}
