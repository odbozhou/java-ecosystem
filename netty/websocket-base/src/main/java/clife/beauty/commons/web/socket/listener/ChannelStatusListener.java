package clife.beauty.commons.web.socket.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * channel status listener
 *
 * @author zhoubo
 * @create 2018-11-20 16:46
 */
public interface ChannelStatusListener {

    /**
     * handshake success
     *
     * @param ctx
     * @param req
     */
    void connect(ChannelHandlerContext ctx, FullHttpRequest req);

    /**
     * channel inactive
     *
     * @param ctx
     */
    void close(ChannelHandlerContext ctx);

}
