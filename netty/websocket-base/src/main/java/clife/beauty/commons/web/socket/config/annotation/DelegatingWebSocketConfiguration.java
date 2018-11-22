package clife.beauty.commons.web.socket.config.annotation;


import clife.beauty.commons.web.socket.bootstrap.WebSocketServer;
import clife.beauty.commons.web.socket.config.WebSocketConfig;
import clife.beauty.commons.web.socket.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A variation of {@link WebSocketConfigurationSupport} that detects implementations of
 * {@link WebSocketConfigurer} in Spring configuration and invokes them in order to
 * configure WebSocket request handling.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
//@Configuration
public class DelegatingWebSocketConfiguration extends WebSocketConfigurationSupport {

    private final List<WebSocketConfigurer> configurers = new ArrayList<>();

    private NettyWebSocketHandlerRegistry handlerRegistry;

    //	@Autowired(required = false)
    public void setConfigurers(List<WebSocketConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.configurers.addAll(configurers);
        }
        initHandlerRegistry();
    }

    private NettyWebSocketHandlerRegistry initHandlerRegistry() {

        WebSocketServer webSocketServer = new WebSocketServer();
        webSocketServer.asyncInit();
        WebSocketConfig webSocketConfig = WebSocketConfig.getInstance();
        webSocketServer.setWebSocketConfig(webSocketConfig);

        if (this.handlerRegistry == null) {
            this.handlerRegistry = new NettyWebSocketHandlerRegistry(webSocketServer);
            registerWebSocketHandlers(this.handlerRegistry);
        }
        return this.handlerRegistry;
    }


    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        for (WebSocketConfigurer configurer : this.configurers) {
			configurer.registerWebSocketHandlers(registry);
        }
    }

}
