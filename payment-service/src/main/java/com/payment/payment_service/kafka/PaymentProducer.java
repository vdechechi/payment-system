package com.payment.payment_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentProducer.class);
    @Value("${kafka.topics.payments}")
    private String paymentsTopic;

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentCreated(PaymentEvent event) {
        kafkaTemplate.send(paymentsTopic, event.paymentId().toString(), event);
        log.info("Payment event sent to Kafka: {}", event.paymentId());
    }
}