package clife.beauty.commons.web.socket.intercept;

import java.util.Comparator;

/**
 * 拦截器排序
 *
 * @author zhoubo
 * @create 2018-11-17 11:30
 */
public class OrderComparator implements Comparator<AbstractWebSocketInterceptor> {
    @Override
    public int compare(AbstractWebSocketInterceptor o1, AbstractWebSocketInterceptor o2) {
        if (o1.getOrder() >= o2.getOrder()) {
            return 1;
        } else if (o1.getOrder() <= o2.getOrder()) {
            return -1;
        }
        return 0;
    }
}
