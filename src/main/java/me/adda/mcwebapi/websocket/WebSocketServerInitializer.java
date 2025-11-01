package me.adda.mcwebapi.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import me.adda.mcwebapi.api.ReflectiveApiDispatcher;
import me.adda.mcwebapi.config.Config;

import java.util.concurrent.TimeUnit;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final String WEBSOCKET_PATH = "/";
    private final ReflectiveApiDispatcher apiDispatcher;

    public WebSocketServerInitializer(ReflectiveApiDispatcher apiDispatcher) {
        this.apiDispatcher = apiDispatcher;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Логирование (только для отладки)
        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));

        // HTTP кодек для обработки HTTP запросов
        pipeline.addLast(new HttpServerCodec());

        // Аггрегатор для обработки полных HTTP сообщений
        pipeline.addLast(new HttpObjectAggregator(65536));

        // Обработчик idle соединений
        pipeline.addLast(new IdleStateHandler(300, 0, 0, TimeUnit.SECONDS));

        // Сжатие WebSocket
        pipeline.addLast(new WebSocketServerCompressionHandler());

        // Обработчик WebSocket протокола с корневым путем
        pipeline.addLast(new WebSocketServerProtocolHandler(
                WEBSOCKET_PATH,
                null,
                true,
                65536,
                false,
                true,
                Config.SERVER.timeout.get() * 1000
        ));

        // Обработчик WebSocket фреймов
        pipeline.addLast(new WebSocketServerHandler(apiDispatcher));
    }
}