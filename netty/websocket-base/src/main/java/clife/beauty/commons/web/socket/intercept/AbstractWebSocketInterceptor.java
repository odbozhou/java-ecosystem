package clife.beauty.commons.web.socket.intercept;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * websocket 拦截器
 *
 * @author zhoubo
 * @create 2018-11-16 10:29
 */
public abstract class AbstractWebSocketInterceptor {

    private Logger logger = LoggerFactory.getLogger(AbstractWebSocketInterceptor.class);

    private int order;

    protected void intercept(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg) {
            logger.warn("msg is null");
            return;
        }
        if (msg instanceof FullHttpRequest) {
            doIntercept(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            doIntercept(ctx, (WebSocketFrame) msg);
        } else {
            logger.warn("unsupport msg type");
        }
    }

    protected void doIntercept(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        logger.info("FullHttpRequest doIntercept");
    }

    protected void doIntercept(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        logger.info("WebSocketFrame doIntercept");
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
