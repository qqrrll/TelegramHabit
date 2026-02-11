package org.example.telegramhabit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Verifies Telegram WebApp initData signature and parses user payload. */
@Component
@RequiredArgsConstructor
public class TelegramInitDataValidator {

    private final ObjectMapper objectMapper;

    @Value("${app.telegram.bot-token}")
    private String botToken;

    public TelegramUserData validateAndExtract(String initData) {
        Map<String, String> values = parse(initData);
        String hash = values.get("hash");
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Telegram hash is missing");
        }

        String dataCheckString = values.entrySet().stream()
                .filter(e -> !"hash".equals(e.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));

        String expectedHash = hmacSha256Hex(sha256("WebAppData", botToken), dataCheckString);
        if (!MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid Telegram signature");
        }

        long authDate = Long.parseLong(values.getOrDefault("auth_date", "0"));
        long now = Instant.now().getEpochSecond();
        if (authDate == 0 || now - authDate > 86_400) {
            throw new IllegalArgumentException("Telegram auth data is expired");
        }

        String userJson = values.get("user");
        if (userJson == null || userJson.isBlank()) {
            throw new IllegalArgumentException("Telegram user payload is missing");
        }

        try {
            JsonNode node = objectMapper.readTree(userJson);
            return new TelegramUserData(
                    node.get("id").asLong(),
                    asText(node, "username"),
                    asText(node, "first_name"),
                    asText(node, "last_name"),
                    asText(node, "photo_url"),
                    normalizeLanguage(asText(node, "language_code"))
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Telegram user payload");
        }
    }

    // Parses query-string style initData into key/value map.
    private Map<String, String> parse(String initData) {
        Map<String, String> values = new HashMap<>();
        String[] pairs = initData.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                values.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return values;
    }

    private byte[] sha256(String prefix, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(prefix.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot prepare Telegram secret key");
        }
    }

    private String hmacSha256Hex(byte[] secret, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] result = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot verify Telegram hash");
        }
    }

    private String asText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String normalizeLanguage(String raw) {
        if (raw == null || raw.isBlank()) {
            return "en";
        }
        String lang = raw.toLowerCase();
        return switch (lang) {
            case "ru", "en" -> lang;
            default -> "en";
        };
    }

    public record TelegramUserData(
            Long telegramId,
            String username,
            String firstName,
            String lastName,
            String photoUrl,
            String language
    ) {
    }
}
