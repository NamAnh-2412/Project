package btl.vexemphim.booking.controller;

import btl.vexemphim.booking.service.OnlinePaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final OnlinePaymentService onlinePaymentService;
    private final String webBaseUrl;

    public PaymentWebhookController(OnlinePaymentService onlinePaymentService,
                                    @Value("${app.web-base-url:http://localhost:5173}") String webBaseUrl) {
        this.onlinePaymentService = onlinePaymentService;
        this.webBaseUrl = webBaseUrl.endsWith("/") ? webBaseUrl.substring(0, webBaseUrl.length() - 1) : webBaseUrl;
    }

    @GetMapping("/options")
    public Map<String, Boolean> options() {
        return Map.of("momo", onlinePaymentService.momoConfigured());
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody JsonNode body) {
        onlinePaymentService.handleWebhook(body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/momo/ipn")
    public Map<String, String> momoIpn(@RequestBody(required = false) JsonNode body) {
        onlinePaymentService.handleMomo(asMap(body));
        return Map.of("message", "Received");
    }

    @GetMapping("/momo/return")
    public void momoReturn(@RequestParam Map<String, String> params, jakarta.servlet.http.HttpServletResponse response)
            throws IOException {
        OnlinePaymentService.MomoOutcome outcome = onlinePaymentService.handleMomo(params);
        String target = outcome.reservationId() == null
                ? webBaseUrl + "/tickets?momo=" + outcome.code()
                : webBaseUrl + "/checkout/" + outcome.reservationId() + "?momo=" + outcome.code();
        response.sendRedirect(target);
    }

    private static Map<String, String> asMap(JsonNode body) {
        Map<String, String> values = new LinkedHashMap<>();
        if (body == null || !body.isObject()) {
            return values;
        }
        body.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            values.put(entry.getKey(), value == null || value.isNull() ? "" : value.asText());
        });
        return values;
    }
}
