package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import me.adda.mcwebapi.websocket.AuthManager;
import me.adda.mcwebapi.config.Config;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@ApiModule("auth")
public class AuthApiModule extends BaseApiModule {
    private static final Logger LOGGER = LogManager.getLogger();
    private final AuthManager authManager;
    private Channel currentChannel;

    public AuthApiModule(Channel channel) {
        this.discoverMethods();
        this.authManager = AuthManager.getInstance();
        this.currentChannel = channel;
    }

    @ApiMethod("authenticate")
    public Map<String, Object> authenticate(String authKey) {
        Map<String, Object> result = new HashMap<>();

        if (currentChannel == null) {
            LOGGER.warn("No channel set for authentication");
            result.put("success", false);
            result.put("message", "No channel available");
            return result;
        }

        boolean success = authManager.authenticate(currentChannel, authKey);
        result.put("success", success);
        result.put("message", success ? "Authentication successful" : "Authentication failed");

        LOGGER.info("Authentication attempt from {}: {}", currentChannel.remoteAddress(), success ? "SUCCESS" : "FAILED");

        return result;
    }

    @ApiMethod("getInfo")
    public Map<String, Object> getAuthInfo() {
        Map<String, Object> info = new HashMap<>();

        if (currentChannel != null) {
            info.put("authenticated", authManager.isAuthenticated(currentChannel));
            info.put("remoteAddress", currentChannel.remoteAddress().toString());
        } else {
            info.put("authenticated", false);
            info.put("remoteAddress", "unknown");
        }

        info.put("sslEnabled", Config.SERVER.enableSSL.get());
        info.put("authRequired", true);

        return info;
    }

    @ApiMethod("check")
    public Map<String, Object> checkAuth() {
        Map<String, Object> result = new HashMap<>();

        boolean authenticated = currentChannel != null && authManager.isAuthenticated(currentChannel);
        result.put("authenticated", authenticated);
        result.put("message", authenticated ? "Authenticated" : "Not authenticated");

        return result;
    }

    public void setChannel(Channel channel) {
        this.currentChannel = channel;
    }
}