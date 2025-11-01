package me.adda.mcwebapi.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.adda.mcwebapi.api.ReflectiveApiDispatcher;
import me.adda.mcwebapi.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketServer {
    private static final Logger LOGGER = LogManager.getLogger();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private final ReflectiveApiDispatcher apiDispatcher;

    public WebSocketServer(ReflectiveApiDispatcher apiDispatcher) {
        this.apiDispatcher = apiDispatcher;
    }

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer(apiDispatcher));

            String host = Config.SERVER.host.get();
            int port = Config.SERVER.port.get();
            channel = bootstrap.bind(host, port).sync().channel();

            LOGGER.info("WebSocket API server started on {}:{}", host, port);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to start WebSocket server", e);
            stop();
        }
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        LOGGER.info("WebSocket API server stopped");
    }

    public boolean isRunning() {
        return channel != null && channel.isActive();
    }
}