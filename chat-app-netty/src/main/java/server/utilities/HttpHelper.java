package server.utilities;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHelper {

    /**
     * Send HTTP response. In our case this is an error response so send it and close the communication channel.
     *
     * @param context  Message context
     * @param request  HTTP request
     * @param response HTTP response
     */
    public static void sendHttpResponse(ChannelHandlerContext context, HttpRequest request, HttpResponse response) {
        // Generate an error response if the response status code is not OK (200)
        if (response.getStatus().getCode() != 200) {
            response.setContent(ChannelBuffers.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(response, response.getContent().readableBytes());
        }

        /*
         *  Send the response (channel.write) and close the connection if this is an error response.
         *  Use a future because write is an asynchronous call.
         */
        ChannelFuture future = context.getChannel().write(response);
        if (!isKeepAlive(request) || response.getStatus().getCode() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void sendHttpResponse(ChannelHandlerContext context, HttpRequest request, String resContent) {
        //  create response
        HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);

        res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHelper.setOrigin(res);

        ChannelBuffer bufferRes =  ChannelBuffers.copiedBuffer(resContent.getBytes());
        setContentLength(res, bufferRes.readableBytes());
        res.setContent(bufferRes);

        HttpHelper.sendHttpResponse(context, request, res);
    }

    public static void setOrigin(final HttpResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
    }

}
