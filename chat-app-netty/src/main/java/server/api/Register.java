package server.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;
import org.redisson.api.RMap;
import server.modules.LoginResponseDTO;
import server.modules.UserDTO;
import server.utilities.HttpHelper;
import server.utilities.RedissonHelper;

public class Register {

    public static void handleRegister(ChannelHandlerContext context, HttpRequest request, Logger logger) {
        ChannelBuffer buffer = request.getContent();
        String jsonPayload = buffer.toString(CharsetUtil.UTF_8);

        Gson gson = new GsonBuilder().serializeNulls().create();
        UserDTO user = gson.fromJson(jsonPayload, UserDTO.class);

        RMap<Object, Object> userDTORMap =  RedissonHelper.redisMap("USERS");

       // account not exists
        if (!userDTORMap.containsKey(user.getPhone())) {

            // save to redis
            userDTORMap.put(user.getPhone(), user);

            // response
            // getRBucket user from redis
            UserDTO userLogin = (UserDTO) userDTORMap.get(user.getPhone());

            LoginResponseDTO loginResponseDTO = new LoginResponseDTO("success", new UserDTO(
                    userLogin.getUserName(), userLogin.getPhone()));

            gson = new GsonBuilder().serializeNulls().create();
            String resContent = gson.toJson(loginResponseDTO);

            HttpHelper.sendHttpResponse(context, request, resContent);
            return;

        }

        // account is exists

        gson = new GsonBuilder().serializeNulls().create();
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO("isExists", null);
        String resContent = gson.toJson(loginResponseDTO);

        HttpHelper.sendHttpResponse(context, request, resContent);
        return;

    }
}
