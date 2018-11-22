/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package clife.beauty.commons.web.socket.config.annotation;

/**
 * Configuration support for WebSocket request handling.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class WebSocketConfigurationSupport {

    /*@Nullable*/
    private NettyWebSocketHandlerRegistry handlerRegistry;

    public WebSocketConfigurationSupport() {
        initHandlerRegistry();
    }

//	@Bean
/*	public HandlerMapping webSocketHandlerMapping() {
		NettyWebSocketHandlerRegistry registry = initHandlerRegistry();
		if (registry.requiresTaskScheduler()) {
			TaskScheduler scheduler = defaultSockJsTaskScheduler();
			Assert.notNull(scheduler, "Expected default TaskScheduler bean");
			registry.setTaskScheduler(scheduler);
		}
		return registry.getHandlerMapping();
	}*/

    private NettyWebSocketHandlerRegistry initHandlerRegistry() {
        if (this.handlerRegistry == null) {
            this.handlerRegistry = new NettyWebSocketHandlerRegistry();
            registerWebSocketHandlers(this.handlerRegistry);
        }
        return this.handlerRegistry;
    }

    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    }
}
