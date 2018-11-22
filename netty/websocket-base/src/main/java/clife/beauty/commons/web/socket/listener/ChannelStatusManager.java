package clife.beauty.commons.web.socket.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * channel status manager
 *
 * @author zhoubo
 * @create 2018-11-20 16:59
 */
public class ChannelStatusManager {

    private List<ChannelStatusListener> channelStatusListeners = new ArrayList<>(8);

    public void addListener(ChannelStatusListener channelStatusListener) {
        channelStatusListeners.add(channelStatusListener);
    }

    public void removeListener(ChannelStatusListener channelStatusListener) {
        channelStatusListeners.remove(channelStatusListener);
    }

    public void connect(ChannelHandlerContext ctx, FullHttpRequest req) {
        for (ChannelStatusListener channelStatusListener : channelStatusListeners) {
            channelStatusListener.connect(ctx, req);
        }
    }

    public void close(ChannelHandlerContext ctx) {
        for (ChannelStatusListener channelStatusListener : channelStatusListeners) {
            channelStatusListener.close(ctx);
        }
    }

}
