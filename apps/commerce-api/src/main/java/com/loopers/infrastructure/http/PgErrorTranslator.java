package com.loopers.infrastructure.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.support.error.pg.PgBadRequestException;
import com.loopers.support.error.pg.PgTransientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PgErrorTranslator {

    private final ObjectMapper objectMapper;

    /**
     * FeignException → 도메인 예외로 변환
     * - HTTP status 에 따라 하위 예외 타입 매핑
     * - body 에서 result/errorCode/message 추출
     */
    public RuntimeException translate(feign.FeignException e) {
        int status = e.status();
        String raw = readBodySafe(e);
        Meta m = parseMetaOrFallback(raw);          // result/errorCode/message

        return switch (status) {
            case 400 -> new PgBadRequestException(m.result, m.errorCode, m.message, e);
            case 500 -> new PgTransientException(m.result, m.errorCode, m.message, e);
            default -> status >= 500
                    ? new PgTransientException(m.result, m.errorCode, m.message, e)
                    : new PgBadRequestException(m.result, m.errorCode, m.message, e);
        };
    }

    private String readBodySafe(feign.FeignException e) {
        try {
            // Feign 12+: Optional<ByteBuffer>
            return e.responseBody()
                    .map(bb -> {
                        var buf = bb.asReadOnlyBuffer();
                        byte[] bytes = new byte[buf.remaining()];
                        buf.get(bytes);
                        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    })
                    .orElseGet(() -> {
                        byte[] content = e.content();
                        return (content == null || content.length == 0)
                                ? ""
                                : new String(content, java.nio.charset.StandardCharsets.UTF_8);
                    });
        } catch (Throwable t) {
            byte[] content = e.content();
            return (content == null || content.length == 0)
                    ? ""
                    : new String(content, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private Meta parseMetaOrFallback(String raw) {
        if (raw == null || raw.isBlank()) return new Meta("UNKNOWN", null, "PG error");
        try {
            var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(raw);
            if (root.isArray() && root.size() > 0) root = root.get(0);
            var meta = root.hasNonNull("meta") ? root.get("meta") : root;

            String result = text(meta, "result", "status", "resultStatus");
            String errorCode = text(meta, "errorCode", "code", "resultCode");
            String message = text(meta, "message", "errorMessage", "msg", "detail");

            if (message == null || message.isBlank()) {
                message = raw.length() > 200 ? raw.substring(0, 200) + "…" : raw;
            }
            return new Meta(result != null ? result : "UNKNOWN", errorCode, message);
        } catch (Exception ignore) {
            String shortMsg = raw.length() > 200 ? raw.substring(0, 200) + "…" : raw;
            return new Meta("UNKNOWN", null, shortMsg);
        }
    }

    private String text(com.fasterxml.jackson.databind.JsonNode n, String... keys) {
        for (String k : keys) if (n.hasNonNull(k)) return n.get(k).asText();
        return null;
    }

    private record Meta(String result, String errorCode, String message) {
    }

}
