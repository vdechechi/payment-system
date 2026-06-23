package com.payment.payment_service.aws;

import com.payment.commons.events.PaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class FraudCheckProducer {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckProducer.class);

    @Value("${aws.sqs.fraud-check-queue}")
    private String queueName;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    public FraudCheckProducer(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public void sendForFraudCheck(PaymentEvent event) {
        try {
            String queueUrl = sqsClient.getQueueUrl(builder -> builder.queueName(queueName))
                    .queueUrl();

            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(request);
            log.info("Payment sent to fraud-check queue: {}", event.paymentId());
        } catch (Exception e) {
            log.error("Failed to send payment to fraud-check queue: {}", event.paymentId(), e);
        }
    }
}