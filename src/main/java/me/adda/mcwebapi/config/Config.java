package me.adda.mcwebapi.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final ServerConfig SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        final Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig {
        public final ModConfigSpec.IntValue port;
        public final ModConfigSpec.ConfigValue<String> authKey;
        public final ModConfigSpec.BooleanValue enableSSL;
        public final ModConfigSpec.IntValue timeout;
        public final ModConfigSpec.ConfigValue<String> allowedOrigins;

        public ServerConfig(ModConfigSpec.Builder builder) {
            builder.push("websocket");

            this.port = builder
                    .comment("WebSocket server port")
                    .defineInRange("port", 8765, 1000, 65535);

            this.authKey = builder
                    .comment("Authentication key for binary protocol")
                    .define("authKey", "default-secret-key-change-me");

            this.enableSSL = builder
                    .comment("Enable TLS/SSL encryption")
                    .define("enableSSL", false);

            this.timeout = builder
                    .comment("Request timeout in seconds")
                    .defineInRange("timeout", 30, 1, 300);

            this.allowedOrigins = builder
                    .comment("Allowed origins for CORS")
                    .define("allowedOrigins", "*");

            builder.pop();
        }
    }
}