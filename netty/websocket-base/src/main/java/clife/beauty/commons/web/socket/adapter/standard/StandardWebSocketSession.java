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

package clife.beauty.commons.web.socket.adapter.standard;

import clife.beauty.commons.web.socket.*;
import clife.beauty.commons.web.socket.adapter.AbstractWebSocketSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtension;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * A {@link WebSocketSession} for use with the standard WebSocket for Java API.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class StandardWebSocketSession extends AbstractWebSocketSession<Session> {

    private final String id;

    /*@Nullable*/
    private URI uri;

    private final HttpHeaders handshakeHeaders;

    /*@Nullable*/
    private String acceptedProtocol;

    /*@Nullable*/
    private Principal user;

    /*@Nullable*/
    private final InetSocketAddress localAddress;

    /*@Nullable*/
    private final InetSocketAddress remoteAddress;

    private final ChannelHandlerContext ctx;

    /**
     * Constructor for a standard WebSocket session.
     *
     * @param headers       the headers of the handshake request
     * @param attributes    attributes from the HTTP handshake to associate with the WebSocket
     *                      session; the provided attributes are copied, the original map is not used.
     * @param localAddress  the address on which the request was received
     * @param remoteAddress the address of the remote client
     */
    public StandardWebSocketSession(/*@Nullable*/ HttpHeaders headers, /*@Nullable*/ Map<String, Object> attributes,
            /*@Nullable*/ InetSocketAddress localAddress, /*@Nullable*/ InetSocketAddress remoteAddress, ChannelHandlerContext ctx) {

        this(headers, attributes, localAddress, remoteAddress, null, ctx);
    }

    /**
     * Constructor that associates a user with the WebSocket session.
     *
     * @param headers       the headers of the handshake request
     * @param attributes    attributes from the HTTP handshake to associate with the WebSocket session
     * @param localAddress  the address on which the request was received
     * @param remoteAddress the address of the remote client
     * @param user          the user associated with the session; if {@code null} we'll
     *                      fallback on the user available in the underlying WebSocket session
     */
    public StandardWebSocketSession(/*@Nullable*/ HttpHeaders headers, /*@Nullable*/ Map<String, Object> attributes,
            /*@Nullable*/ InetSocketAddress localAddress, /*@Nullable*/ InetSocketAddress remoteAddress,
            /*@Nullable*/ Principal user, ChannelHandlerContext ctx) {

        super(attributes);
        this.ctx = ctx;
        this.id = ctx.channel().id().asLongText();
//		headers = (headers != null ? headers : new ReadOnlyHttpHeaders());
//		this.handshakeHeaders = HttpHeaders.readOnlyHttpHeaders(headers);
        this.handshakeHeaders = headers;
        this.user = user;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }


    @Override
    public String getId() {
        return this.id;
    }

    @Override
    /*@Nullable*/
    public URI getUri() {
        checkNativeSessionInitialized();
        return this.uri;
    }

/*	@Override
	public HttpHeaders getHandshakeHeaders() {
		return this.handshakeHeaders;
	}*/

    @Override
    public String getAcceptedProtocol() {
        checkNativeSessionInitialized();
        return this.acceptedProtocol;
    }


    public Principal getPrincipal() {
        return this.user;
    }

    @Override
    /*@Nullable*/
    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override
    /*@Nullable*/
    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {
        checkNativeSessionInitialized();
        getNativeSession().setMaxTextMessageBufferSize(messageSizeLimit);
    }

    @Override
    public int getTextMessageSizeLimit() {
        checkNativeSessionInitialized();
        return getNativeSession().getMaxTextMessageBufferSize();
    }

    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {
        checkNativeSessionInitialized();
        getNativeSession().setMaxBinaryMessageBufferSize(messageSizeLimit);
    }

    @Override
    public int getBinaryMessageSizeLimit() {
        checkNativeSessionInitialized();
        return getNativeSession().getMaxBinaryMessageBufferSize();
    }

    @Override
    public List<WebSocketExtension> getExtensions() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return getNativeSession().isOpen();
    }

/*	@Override
	public void initializeNativeSession(Session session) {
		super.initializeNativeSession(session);

		this.uri = session.getRequestURI();
		this.acceptedProtocol = session.getNegotiatedSubprotocol();

		List<Extension> standardExtensions = getNativeSession().getNegotiatedExtensions();
*//*		if (!CollectionUtils.isEmpty(standardExtensions)) {
			this.extensions = new ArrayList<>(standardExtensions.size());
			for (Extension standardExtension : standardExtensions) {
				this.extensions.add(new StandardToWebSocketExtensionAdapter(standardExtension));
			}
			this.extensions = Collections.unmodifiableList(this.extensions);
		}
		else {
			this.extensions = Collections.emptyList();
		}*//*

		if (this.user == null) {
			this.user = session.getUserPrincipal();
		}
	}*/

    @Override
    protected void sendTextMessage(TextMessage message) throws IOException {
        getNativeSession().getBasicRemote().sendText(message.getPayload(), message.isLast());
    }

    @Override
    protected void sendBinaryMessage(BinaryMessage message) throws IOException {
        getNativeSession().getBasicRemote().sendBinary(message.getPayload(), message.isLast());
    }

    @Override
    protected void sendPingMessage(PingMessage message) throws IOException {
        getNativeSession().getBasicRemote().sendPing(message.getPayload());
    }

    @Override
    protected void sendPongMessage(PongMessage message) throws IOException {
        getNativeSession().getBasicRemote().sendPong(message.getPayload());
    }

    @Override
    protected void closeInternal(CloseStatus status) throws IOException {
        getNativeSession().close(new CloseReason(CloseCodes.getCloseCode(status.getCode()), status.getReason()));
    }

}
