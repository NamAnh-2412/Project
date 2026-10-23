package btl.vexemphim.booking.service;

import btl.vexemphim.common.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MomoClient {

    private final String endpoint;
    private final String partnerCode;
    private final String accessKey;
    private final String secretKey;
    private final String redirectUrl;
    private final String ipnUrl;
    private final ObjectMapper objectMapper;
    private final RestClient http = RestClient.create();

    public MomoClient(@Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}") String endpoint,
                      @Value("${momo.partner-code:}") String partnerCode,
                      @Value("${momo.access-key:}") String accessKey,
                      @Value("${momo.secret-key:}") String secretKey,
                      @Value("${momo.redirect-url:}") String redirectUrl,
                      @Value("${momo.ipn-url:}") String ipnUrl,
                      ObjectMapper objectMapper) {
        this.endpoint = endpoint;
        this.partnerCode = partnerCode;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.redirectUrl = redirectUrl;
        this.ipnUrl = ipnUrl;
        this.objectMapper = objectMapper;
    }

    public boolean configured() {
        return !partnerCode.isBlank() && !accessKey.isBlank() && !secretKey.isBlank()
                && !redirectUrl.isBlank() && !ipnUrl.isBlank();
    }

    public Session create(String orderId, long amount, String orderInfo, String extraData, String requestId) {
        if (!configured()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_NOT_CONFIGURED",
                    "Cổng MoMo chưa được cấu hình");
        }
        String requestType = "captureWallet";
        String raw = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", hmac(raw));
        body.put("lang", "vi");
        String json;
        try {
            json = http.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY", "Không kết nối được MoMo");
        }
        try {
            JsonNode root = objectMapper.readTree(json == null ? "{}" : json);
            if (!"0".equals(root.path("resultCode").asText())) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY",
                        root.path("message").asText("MoMo từ chối tạo thanh toán"));
            }
            String payUrl = root.path("payUrl").asText("");
            if (payUrl.isBlank()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY", "MoMo không trả liên kết thanh toán");
            }
            return new Session(payUrl, root.path("qrCodeUrl").asText(""));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "PAYMENT_GATEWAY", "Không đọc được phản hồi MoMo");
        }
    }

    public boolean validResult(Map<String, String> payload) {
        if (!configured() || payload == null) {
            return false;
        }
        String signature = payload.get("signature");
        if (signature == null || signature.isBlank()) {
            return false;
        }
        String raw = "accessKey=" + accessKey
                + "&amount=" + text(payload, "amount")
                + "&extraData=" + text(payload, "extraData")
                + "&message=" + text(payload, "message")
                + "&orderId=" + text(payload, "orderId")
                + "&orderInfo=" + text(payload, "orderInfo")
                + "&orderType=" + text(payload, "orderType")
                + "&partnerCode=" + text(payload, "partnerCode")
                + "&payType=" + text(payload, "payType")
                + "&requestId=" + text(payload, "requestId")
                + "&responseTime=" + text(payload, "responseTime")
                + "&resultCode=" + text(payload, "resultCode")
                + "&transId=" + text(payload, "transId");
        return MessageDigest.isEqual(
                hmac(raw).getBytes(StandardCharsets.UTF_8),
                signature.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private static String text(Map<String, String> payload, String key) {
        String value = payload.get(key);
        return value == null ? "" : value;
    }

    private String hmac(String raw) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_GATEWAY", "Không ký được yêu cầu MoMo");
        }
    }

    public record Session(String payUrl, String qrCodeUrl) {
    }
}
