package me.adda.mcwebapi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.adda.mcwebapi.api.Message;
import me.adda.mcwebapi.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.Key;

public class BinaryCodec {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // Для простоты используем фиксированный ключ из конфига
    // В продакшене нужно генерировать случайный ключ при первом запуске
    private static Key getEncryptionKey() {
        String authKey = Config.SERVER.authKey.get();
        // Дополняем или обрезаем ключ до 16 байт для AES
        byte[] keyBytes = new byte[16];
        byte[] authBytes = authKey.getBytes();
        System.arraycopy(authBytes, 0, keyBytes, 0, Math.min(authBytes.length, 16));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static String encode(Message message) {
        try {
            String json = mapper.writeValueAsString(message);

            if (Config.SERVER.enableSSL.get()) {
                return encrypt(json);
            } else {
                return Base64.getEncoder().encodeToString(json.getBytes());
            }
        } catch (Exception e) {
            LOGGER.error("Error encoding message", e);
            throw new RuntimeException("Encoding failed", e);
        }
    }

    public static Message decode(String data) {
        try {
            String json;

            if (Config.SERVER.enableSSL.get()) {
                json = decrypt(data);
            } else {
                byte[] decodedBytes = Base64.getDecoder().decode(data);
                json = new String(decodedBytes);
            }

            return mapper.readValue(json, Message.class);
        } catch (Exception e) {
            LOGGER.error("Error decoding message", e);
            throw new RuntimeException("Decoding failed", e);
        }
    }

    private static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey());
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey());
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    public static boolean isEncryptionAvailable() {
        try {
            Cipher.getInstance(TRANSFORMATION);
            return true;
        } catch (Exception e) {
            LOGGER.warn("Encryption not available: {}", e.getMessage());
            return false;
        }
    }
}
