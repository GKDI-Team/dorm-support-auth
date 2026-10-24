package com.dormsupport.auth.service;

import com.dormsupport.auth.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final Pattern SUBJECT = Pattern.compile("\\\"sub\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern EXPIRATION = Pattern.compile("\\\"exp\\\"\\s*:\\s*(\\d+)");

    private final byte[] secret;
    private final Duration tokenTtl;

    public JwtService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.ttl:24h}") Duration tokenTtl) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.tokenTtl = tokenTtl;
    }

    public String createToken(User user) {
        long now = Instant.now().getEpochSecond();
        long expiration = Instant.now().plus(tokenTtl).getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + user.getId() + "\",\"role\":\""
                + user.getRole().name() + "\",\"iat\":" + now + ",\"exp\":" + expiration + "}");
        String content = header + "." + payload;
        return content + "." + ENCODER.encodeToString(sign(content));
    }

    public UUID getUserId(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), DECODER.decode(parts[2]))) {
                throw new InvalidTokenException();
            }

            String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher subject = SUBJECT.matcher(payload);
            Matcher expiration = EXPIRATION.matcher(payload);
            if (!subject.find() || !expiration.find()
                    || Long.parseLong(expiration.group(1)) <= Instant.now().getEpochSecond()) {
                throw new InvalidTokenException();
            }
            return UUID.fromString(subject.group(1));
        } catch (RuntimeException exception) {
            throw new InvalidTokenException();
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }

    private String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
