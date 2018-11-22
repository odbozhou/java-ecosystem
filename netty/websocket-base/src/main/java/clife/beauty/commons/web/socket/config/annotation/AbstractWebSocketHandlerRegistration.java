
package clife.beauty.commons.web.socket.config.annotation;

import clife.beauty.commons.web.socket.WebSocketHandler;
import clife.beauty.commons.web.socket.server.HandshakeInterceptor;
import clife.beauty.commons.web.socket.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for {@link WebSocketHandlerRegistration WebSocketHandlerRegistrations} that gathers all the configuration
 * options but allows sub-classes to put together the actual HTTP request mappings.
 *
 * @param <M> the mappings type
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @since 4.0
 */
public abstract class AbstractWebSocketHandlerRegistration<M> implements WebSocketHandlerRegistration {

    private final MultiValueMap<WebSocketHandler, String> handlerMap = new LinkedMultiValueMap<>();

    /*@Nullable*/
//	private HandshakeHandler handshakeHandler;

    private final List<HandshakeInterceptor> interceptors = new ArrayList<>();

    private final List<String> allowedOrigins = new ArrayList<>();

    /*@Nullable*/
//	private SockJsServiceRegistration sockJsServiceRegistration;


    @Override
    public WebSocketHandlerRegistration addHandler(WebSocketHandler handler, String... paths) {
        Assert.notNull(handler, "WebSocketHandler must not be null");
//		Assert.notEmpty(paths, "Paths must not be empty");
        this.handlerMap.put(handler, Arrays.asList(paths));
        return this;
    }

/*	@Override
	public WebSocketHandlerRegistration setHandshakeHandler(*//*@Nullable*//* HandshakeHandler handshakeHandler) {
		this.handshakeHandler = handshakeHandler;
		return this;
	}*/

    /*@Nullable*/
/*	protected HandshakeHandler getHandshakeHandler() {
		return this.handshakeHandler;
	}*/

/*	@Override
	public WebSocketHandlerRegistration addInterceptors(HandshakeInterceptor... interceptors) {
		if (!ObjectUtils.isEmpty(interceptors)) {
			this.interceptors.addAll(Arrays.asList(interceptors));
		}
		return this;
	}*/

    @Override
    public WebSocketHandlerRegistration setAllowedOrigins(String... allowedOrigins) {
        this.allowedOrigins.clear();
        if (!ObjectUtils.isEmpty(allowedOrigins)) {
            this.allowedOrigins.addAll(Arrays.asList(allowedOrigins));
        }
        return this;
    }


/*	protected HandshakeInterceptor[] getInterceptors() {
		List<HandshakeInterceptor> interceptors = new ArrayList<>(this.interceptors.size() + 1);
		interceptors.addAll(this.interceptors);
		interceptors.add(new OriginHandshakeInterceptor(this.allowedOrigins));
		return interceptors.toArray(new HandshakeInterceptor[0]);
	}*/


//	protected abstract M createMappings();

/*	protected abstract void addWebSocketHandlerMapping(M mappings, WebSocketHandler wsHandler,
			HandshakeHandler handshakeHandler, HandshakeInterceptor[] interceptors, String path);*/

}
