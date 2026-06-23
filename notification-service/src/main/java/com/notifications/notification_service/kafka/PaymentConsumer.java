package com.notifications.notification_service.kafka;

import com.notifications.notification_service.service.NotificationService;
import com.payment.commons.events.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);

    private final NotificationService notificationService;

    public PaymentConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${kafka.topics.payments}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PaymentEvent event) {
        log.info("Payment event received: {} - Status: {}", event.paymentId(), event.status());
    }
}