/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package clife.beauty.commons.web.socket.bootstrap;

import clife.beauty.commons.web.socket.*;
import clife.beauty.commons.web.socket.adapter.standard.StandardWebSocketSession;
import clife.beauty.commons.web.socket.config.WebSocketConfig;
import clife.beauty.commons.web.socket.intercept.InterceptProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;

    private InterceptProcess interceptProcess = InterceptProcess.getInstance();

    private WebSocketConfig webSocketConfig;

    private WebSocketHandler webSocketHandler;

    private WebSocketSession webSocketSession;

    public WebSocketServerHandler(WebSocketConfig webSocketConfig, WebSocketHandler webSocketHandler) {
        this.webSocketConfig = webSocketConfig;
        this.webSocketHandler = webSocketHandler;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        InterceptProcess.loadIntercept(webSocketConfig.getPackagename());
        interceptProcess.intercept(ctx, msg);
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        logger.info("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        logger.info("channelUnregistered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("channelActive");
        webSocketHandler.afterConnectionClosed(webSocketSession, CloseStatus.NORMAL);
        WebSocketServer.channelStatusManager.close(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("channelInactive");
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        //handle query params
        logger.info("uri: {}", req.uri());
        String uriStr = req.uri();
        String uri = null;
        String queryStr = null;
        Map<String, Object> queryMap = null;
        if (StringUtil.isNullOrEmpty(uriStr)) {
            if (uriStr.contains("?")) {
                final String[] uriArr = uriStr.split("\\?");
                if (2 == uriArr.length && !StringUtil.isNullOrEmpty(uriArr[1])) {
                    uri = uriArr[0];
                    queryMap.put("uri", uri);
                    queryStr = uriArr[1];
                    queryMap = convertValue(queryStr);
                }
            }
        }


        // Send the demo page and favicon.ico
        if ("/".equals(req.uri())) {
            ByteBuf content = WebSocketServerBenchmarkPage.getContent(getWebSocketLocation(req, webSocketConfig.getWebsocketPath()));
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpUtil.setContentLength(res, content.readableBytes());

            sendHttpResponse(ctx, req, res);
            return;
        }
        if ("/favicon.ico".equals(req.uri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req, webSocketConfig.getWebsocketPath()), null, true, 5 * 1024 * 1024);
        handshaker = wsFactory.newHandshaker(req);
        ChannelFuture channelFuture;
        if (handshaker == null) {
            channelFuture = WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            channelFuture = handshaker.handshake(ctx.channel(), req);
        }
        if (channelFuture.isSuccess()) {
            //握手成功
            logger.info("handshake success!");
            webSocketSession = new StandardWebSocketSession(req.headers(), null, null, null, ctx);
            logger.info("thread name {}, webSocketSession {}", Thread.currentThread().getName(), webSocketSession);
            try {
                webSocketHandler.afterConnectionEstablished(webSocketSession);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            WebSocketServer.channelStatusManager.connect(ctx, req);
        }
    }

    /**
     * 将uri中带的参数转成
     *
     * @param queryStr
     * @return
     */
    private Map<String, Object> convertValue(String queryStr) {
        Map<String, Object> queryMap = new HashMap<>(16);
        if (!StringUtil.isNullOrEmpty(queryStr)) {
            final String[] params = queryStr.split("&");
            if (null != params && params.length > 0) {
                for (String kv : params) {
                    final String[] kvArr = kv.split("=");
                    if (null != kvArr) {
                        if (kvArr.length == 2) {
                            queryMap.put(kvArr[0], kvArr[1]);
                        } else {
                            logger.warn("param error {}", kv);
                        }
                    }
                }
            }
        }
        return queryMap;
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            // TODO 是否关闭通道时调用
            logger.info("handshaker close");
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            logger.info("Receive message: {}", ((PingWebSocketFrame) frame).content().toString());
            WebSocketMessage webSocketMessage = new PingMessage((((PingWebSocketFrame) frame).content()).nioBuffer());
            try {
                webSocketHandler.handleMessage(webSocketSession, webSocketMessage);
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            }
//            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            // Echo the frame
            logger.info("Receive message: {}", ((TextWebSocketFrame) frame).text());
            WebSocketMessage webSocketMessage = new TextMessage((((TextWebSocketFrame) frame).text()).getBytes(CharsetUtil.UTF_8));
            try {
                webSocketHandler.handleMessage(webSocketSession, webSocketMessage);
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            }
//            ctx.write(frame.retain());
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            // Echo the frame
            WebSocketMessage webSocketMessage = new BinaryMessage(((BinaryWebSocketFrame) frame).content().nioBuffer());
            try {
                webSocketHandler.handleMessage(webSocketSession, webSocketMessage);
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            }
//            ctx.write(frame.retain());
        }
    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req, String websocketPath) {
        String location = req.headers().get(HttpHeaderNames.HOST) + websocketPath;
        if (WebSocketServer.SSL) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
}
