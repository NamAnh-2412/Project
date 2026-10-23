package btl.vexemphim.booking.config;

import btl.vexemphim.booking.event.SeatChangedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@Configuration
public class SeatSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-cineve")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173");
    }

    @Component
    static class SeatFanout {
        private final SimpMessagingTemplate template;

        SeatFanout(SimpMessagingTemplate template) {
            this.template = template;
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onSeatChanged(SeatChangedEvent event) {
            template.convertAndSend("/topic/showtimes/" + event.showtimeId(), event);
        }
    }
}
