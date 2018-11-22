package com.zhou.handler;

import clife.beauty.commons.web.socket.BinaryMessage;
import clife.beauty.commons.web.socket.CloseStatus;
import clife.beauty.commons.web.socket.WebSocketSession;
import clife.beauty.commons.web.socket.handler.AbstractWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : zhoubo
 * @Project: java-framework
 * @Package com.zhou.handler
 * @Description: NettyWebSocketHandler
 * @date Date : 2018-11-22 下午11:59
 */
public class NettyWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("afterConnectionEstablished");
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        logger.info("handleBinaryMessage");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("handleBinaryMessage");
    }
}
