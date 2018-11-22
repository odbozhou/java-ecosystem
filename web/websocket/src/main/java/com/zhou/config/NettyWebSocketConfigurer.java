package com.zhou.config;

import clife.beauty.commons.web.socket.config.annotation.WebSocketConfigurer;
import clife.beauty.commons.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.zhou.handler.NettyWebSocketHandler;

/**
 * @author : zhoubo
 * @Project: java-framework
 * @Package com.zhou.config
 * @Description: NettyWebSocketConfigurer
 * @date Date : 2018-11-22 下午11:58
 */
public class NettyWebSocketConfigurer implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(nettyWebSocketHandler(), 9090, "/handshake");
    }

    public NettyWebSocketHandler nettyWebSocketHandler() {
        return new NettyWebSocketHandler();
    }
}
