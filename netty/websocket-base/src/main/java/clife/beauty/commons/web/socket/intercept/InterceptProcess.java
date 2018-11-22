package clife.beauty.commons.web.socket.intercept;

import clife.beauty.commons.web.socket.reflect.ClassScanner;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * websocket建立链接握手连接器
 *
 * @author zhoubo
 */
public class InterceptProcess {

    private static final Logger logger = LoggerFactory.getLogger(InterceptProcess.class);

    private InterceptProcess() {
    }

    private volatile static InterceptProcess interceptProcess;

    public static List<AbstractWebSocketInterceptor> interceptors;

    public static InterceptProcess getInstance() {
        if (null == interceptProcess) {
            synchronized (InterceptProcess.class) {
                if (null == interceptProcess) {
                    interceptProcess = new InterceptProcess();
                    return interceptProcess;
                }
            }
        }
        return interceptProcess;
    }

    public static void loadIntercept(String packageName) {
        if (null != interceptors) {
            return;
        }

        if (StringUtil.isNullOrEmpty(packageName)) {
            return;
        }

        interceptors = new ArrayList<>(16);

        Map<Class<?>, Integer> handshakeInterceptor = null;
        try {
            handshakeInterceptor = ClassScanner.getHandshakeInterceptor(packageName);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        if (null != handshakeInterceptor) {
            for (Map.Entry<Class<?>, Integer> entry : handshakeInterceptor.entrySet()) {
                final Class<?> key = entry.getKey();
                final Integer value = entry.getValue();
                try {
                    AbstractWebSocketInterceptor interceptor = (AbstractWebSocketInterceptor) key.newInstance();
                    interceptor.setOrder(value);
                    interceptors.add(interceptor);
                } catch (InstantiationException e) {
                    logger.warn("HandshakeInterceptor instance exception", e);
                } catch (IllegalAccessException e) {
                    logger.warn("HandshakeInterceptor IllegalAccessException exception", e);
                }
            }
            if (null != interceptors && !interceptors.isEmpty()) {
                Collections.sort(interceptors, new OrderComparator());
            }
        }
    }

    public void intercept(ChannelHandlerContext ctx, Object msg) {
        long startTime = System.nanoTime();
        if (null != interceptors && !interceptors.isEmpty()) {
            for (AbstractWebSocketInterceptor abstractWebSocketInterceptor : interceptors) {
                try {
                    abstractWebSocketInterceptor.intercept(ctx, msg);
                } catch (Exception e) {
                    logger.warn(abstractWebSocketInterceptor + " interceptor exec exception", e);
                }
            }
        }
        long endTime = System.nanoTime();
        logger.info("intercept exec cost {} ms", (endTime - startTime) / 1000000.0);
    }
}
