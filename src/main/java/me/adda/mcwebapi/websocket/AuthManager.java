package me.adda.mcwebapi.websocket;

import io.netty.channel.Channel;
import me.adda.mcwebapi.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AuthManager INSTANCE = new AuthManager();

    private final Map<Channel, Boolean> authenticatedChannels = new ConcurrentHashMap<>();
    private final Map<Channel, Long> channelTimeouts = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 30 * 60 * 1000; // 30 минут

    public static AuthManager getInstance() {
        return INSTANCE;
    }

    private AuthManager() {
        // private constructor for singleton
    }

    public boolean authenticate(Channel channel, String authKey) {
        String configKey = Config.SERVER.authKey.get();

        if (configKey.equals(authKey)) {
            authenticatedChannels.put(channel, true);
            channelTimeouts.put(channel, System.currentTimeMillis() + TIMEOUT_MS);
            LOGGER.info("Channel authenticated: {}", channel.remoteAddress());
            return true;
        } else {
            LOGGER.warn("Authentication failed for channel: {}", channel.remoteAddress());
            return false;
        }
    }

    public boolean isAuthenticated(Channel channel) {
        if (channel == null) {
            return false;
        }

        Boolean authenticated = authenticatedChannels.get(channel);
        if (authenticated == null || !authenticated) {
            return false;
        }

        Long timeout = channelTimeouts.get(channel);
        if (timeout != null && System.currentTimeMillis() > timeout) {
            LOGGER.debug("Channel timeout: {}", channel.remoteAddress());
            removeChannel(channel);
            return false;
        }

        channelTimeouts.put(channel, System.currentTimeMillis() + TIMEOUT_MS);
        return true;
    }

    public void removeChannel(Channel channel) {
        authenticatedChannels.remove(channel);
        channelTimeouts.remove(channel);
        LOGGER.debug("Channel removed from auth: {}", channel.remoteAddress());
    }

    public int getAuthenticatedCount() {
        return authenticatedChannels.size();
    }
}