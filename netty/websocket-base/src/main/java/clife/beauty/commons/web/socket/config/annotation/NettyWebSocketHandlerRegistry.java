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


import clife.beauty.commons.web.socket.WebSocketHandler;

import java.util.*;

/**
 * {@link WebSocketHandlerRegistry} with Spring MVC handler mappings for the
 * handshake requests.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class NettyWebSocketHandlerRegistry implements WebSocketHandlerRegistry {

    private final List<NettyWebSocketHandlerRegistration> registrations = new ArrayList<>(4);

    private int order = 1;

    /*@Nullable*/
//    private UrlPathHelper urlPathHelper;


    public NettyWebSocketHandlerRegistry() {
    }


    @Override
    public WebSocketHandlerRegistration addHandler(WebSocketHandler handler, String... paths) {
        NettyWebSocketHandlerRegistration registration = new NettyWebSocketHandlerRegistration();
        registration.addHandler(handler, paths);
        this.registrations.add(registration);
        return registration;
    }

    /**
     * Set the order for the resulting {@link SimpleUrlHandlerMapping} relative to
     * other handler mappings configured in Spring MVC.
     * <p>The default value is 1.
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    /**
     * Set the UrlPathHelper to configure on the {@code SimpleUrlHandlerMapping}
     * used to map handshake requests.
     */
/*
    public void setUrlPathHelper(*/
    /*@Nullable*//*
 UrlPathHelper urlPathHelper) {
        this.urlPathHelper = urlPathHelper;
    }

    */
    /*@Nullable*//*

    public UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }
*/
}
