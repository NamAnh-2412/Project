package btl.vexemphim.booking.service;

import btl.vexemphim.common.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

@Component
public class PayOsClient {

    private final String clientId;
    private final String apiKey;
    private final String checksumKey;
    private final String returnUrl;
    private final String cancelUrl;
    private final ObjectMapper objectMapper;
    private final RestClient http = RestClient.create();

    public PayOsClient(@Value("${payos.client-id:}") String clientId,
                       @Value("${payos.api-key:}") String apiKey,
                       @Value("${payos.checksum-key:}") String checksumKey,
                       @Value("${payos.return-url:}") String returnUrl,
                       @Value("${payos.cancel-url:}") String cancelUrl,
                       ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.apiKey = apiKey;
        this.checksumKey = checksumKey;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.objectMapper = objectMapper;
    }

    public boolean configured() {
        return !clientId.isBlank() && !apiKey.isBlank() && !checksumKey.isBlank();
    }

    public Checkout createLink(long orderCode, long amount, String description) {
        if (!configured()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_NOT_CONFIGURED",
                    "Cổng PayOS chưa được cấu hình");
        }
        String raw = "amount=" + amount
                + "&cancelUrl=" + cancelUrl
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + returnUrl;
        String signature = hmac(raw);
        Map<String, Object> body = Map.of(
                "orderCode", orderCode,
                "amount", amount,
                "description", description,
                "cancelUrl", cancelUrl,
                "returnUrl", returnUrl,
                "signature", signature);
        String json = http.post()
                .uri("https://api-merchant.payos.vn/v2/payment-requests")
                .header("x-client-id", clientId)
                .header("x-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(json == null ? "{}" : json);
            if (!"00".equals(root.path("code").asText())) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY",
                        root.path("desc").asText("Cổng thanh toán từ chối"));
            }
            JsonNode data = root.path("data");
            return new Checkout(data.path("checkoutUrl").asText(), data.path("qrCode").asText());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY", "Không đọc được phản hồi PayOS");
        }
    }

    public boolean validWebhook(JsonNode data, String signature) {
        if (!configured() || signature == null || data == null || !data.isObject()) {
            return false;
        }
        TreeMap<String, String> fields = new TreeMap<>();
        data.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            if (value == null || value.isNull()) {
                fields.put(entry.getKey(), "");
            } else if (value.isValueNode()) {
                fields.put(entry.getKey(), value.asText());
            }
        });
        String raw = fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
        return hmac(raw).equalsIgnoreCase(signature);
    }

    private String hmac(String raw) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_GATEWAY", "Không ký được yêu cầu thanh toán");
        }
    }

    public record Checkout(String checkoutUrl, String qrCode) {
    }
}
