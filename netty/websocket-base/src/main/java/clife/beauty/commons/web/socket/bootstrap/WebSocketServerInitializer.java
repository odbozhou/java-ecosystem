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

import clife.beauty.commons.web.socket.WebSocketHandler;
import clife.beauty.commons.web.socket.config.WebSocketConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    private WebSocketConfig webSocketConfig;

    private WebSocketHandler webSocketHandler;

    public WebSocketServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    public WebSocketServerInitializer(SslContext sslCtx, WebSocketConfig webSocketConfig, WebSocketHandler webSocketHandler) {
        this.sslCtx = sslCtx;
        this.webSocketConfig = webSocketConfig;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerHandler(webSocketConfig, webSocketHandler));
    }
}
