package clife.beauty.commons.web.socket.config;

/**
 * websocket 配置信息
 *
 * @author zhoubo
 * @create 2018-11-17 14:37
 */
public class WebSocketConfig {

    private Integer port;

    private String packagename;

    private String websocketPath;

    private static WebSocketConfig webSocketConfig;

    private WebSocketConfig() {
    }

    public static WebSocketConfig getInstance() {
        if (null == webSocketConfig) {
            synchronized (WebSocketConfig.class) {
                if (null == webSocketConfig) {
                    webSocketConfig = new WebSocketConfig();
                    return webSocketConfig;
                }
            }
        }
        return webSocketConfig;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public void setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
    }
}
