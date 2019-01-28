package server.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.redisson.api.RMap;
import server.modules.UserDTO;
import server.modules.UserResponseDTO;
import server.utilities.HttpHelper;
import server.utilities.RedissonHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class LoadFriends {
    public static void handleLoadFriends(ChannelHandlerContext context, HttpRequest request, Logger logger) {

//        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        Gson gson = new GsonBuilder().serializeNulls().create();

        ArrayList<UserResponseDTO> listUsers = new ArrayList<>();
        RMap<Object, Object> userDTORMap =  RedissonHelper.redisMap("USERS");

        Set<Map.Entry<Object, Object>> allEntries = userDTORMap.readAllEntrySet();

        for (Map.Entry<Object, Object> entry : allEntries) {
            String key = (String) entry.getKey();
            UserDTO user = (UserDTO) entry.getValue();

            listUsers.add(new UserResponseDTO(user.getUserName(), user.getPhone()));
        }

        String resContent = gson.toJson(listUsers);
        HttpHelper.sendHttpResponse(context, request, resContent);
        return;


    }
}
