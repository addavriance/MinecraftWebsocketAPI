package me.adda.mcwebapi.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import me.adda.mcwebapi.api.Message;
import me.adda.mcwebapi.api.ReflectiveApiDispatcher;
import me.adda.mcwebapi.api.modules.AuthApiModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final AuthManager authManager;
    private final ReflectiveApiDispatcher apiDispatcher;
    private AuthApiModule authModule;

    public WebSocketServerHandler(ReflectiveApiDispatcher apiDispatcher) {
        this.authManager = AuthManager.getInstance();
        this.apiDispatcher = apiDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("New client connection: {}", ctx.channel().remoteAddress());

        this.authModule = new AuthApiModule(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        try {
            if (frame instanceof TextWebSocketFrame) {
                handleTextFrame(ctx, (TextWebSocketFrame) frame);
            } else if (frame instanceof CloseWebSocketFrame) {
                handleCloseFrame(ctx, (CloseWebSocketFrame) frame);
            } else if (frame instanceof PingWebSocketFrame) {
                handlePingFrame(ctx, (PingWebSocketFrame) frame);
            } else {
                LOGGER.warn("Unsupported WebSocket frame type: {}", frame.getClass().getSimpleName());
            }
        } catch (Exception e) {
            LOGGER.error("Error processing WebSocket frame", e);
            sendError(ctx, null, "PROCESSING_ERROR", e.getMessage());
        }
    }

    private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String requestText = frame.text();
        LOGGER.debug("Received WebSocket text frame: {}", requestText);

        try {
            Message request = BinaryCodec.decode(requestText);

            LOGGER.debug("Decoded message: type={}, module={}, method={}, requestId={}",
                    request.getType(), request.getModule(), request.getMethod(), request.getRequestId());

            if ("auth".equals(request.getModule())) {
                Message response = apiDispatcher.dispatchWithModule(request, authModule);
                sendResponse(ctx, response);
                return;
            }

            if (!authManager.isAuthenticated(ctx.channel())) {
                LOGGER.warn("Unauthorized access attempt from {}: {}.{}",
                        ctx.channel().remoteAddress(), request.getModule(), request.getMethod());
                sendError(ctx, request, "NOT_AUTHENTICATED", "Authentication required. Use auth.authenticate(key) first.");
                return;
            }

            Message response = apiDispatcher.dispatch(request);
            sendResponse(ctx, response);

        } catch (Exception e) {
            LOGGER.error("Error processing WebSocket message", e);
            sendError(ctx, null, "PROCESSING_ERROR", e.getMessage());
        }
    }

    private void handleCloseFrame(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
        LOGGER.info("WebSocket connection closed: {}", ctx.channel().remoteAddress());
        authManager.removeChannel(ctx.channel());
        ctx.close();
    }

    private void handlePingFrame(ChannelHandlerContext ctx, PingWebSocketFrame frame) {
        LOGGER.debug("Received ping, sending pong");
        ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete handshake =
                    (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            LOGGER.info("WebSocket handshake completed: {}", handshake.requestUri());
            LOGGER.info("Client connected: {}", ctx.channel().remoteAddress());
        }
        super.userEventTriggered(ctx, evt);
    }

    private void sendResponse(ChannelHandlerContext ctx, Message response) {
        try {
            String responseText = BinaryCodec.encode(response);
            ctx.writeAndFlush(new TextWebSocketFrame(responseText));
            LOGGER.debug("Sent response: requestId={}", response.getRequestId());
        } catch (Exception e) {
            LOGGER.error("Error sending response", e);
        }
    }

    private void sendError(ChannelHandlerContext ctx, Message request, String code, String message) {
        try {
            Message errorResponse = new Message("ERROR", request != null ? request.getRequestId() : "000");
            errorResponse.setStatus("ERROR");

            java.util.Map<String, Object> errorData = new java.util.HashMap<>();
            errorData.put("code", code);
            errorData.put("message", message);
            errorResponse.setData(errorData);

            String errorText = BinaryCodec.encode(errorResponse);
            ctx.writeAndFlush(new TextWebSocketFrame(errorText));
            LOGGER.debug("Sent error: code={}, message={}", code, message);
        } catch (Exception e) {
            LOGGER.error("Error sending error response", e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        authManager.removeChannel(ctx.channel());
        LOGGER.info("Client disconnected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("WebSocket error from {}: {}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }
}