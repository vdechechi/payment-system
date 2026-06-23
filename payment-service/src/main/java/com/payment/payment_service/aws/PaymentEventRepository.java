package com.payment.payment_service.aws;

import com.payment.commons.events.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventRepository {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventRepository.class);

    @Value("${aws.dynamodb.payment-events-table}")
    private String tableName;

    private final DynamoDbClient dynamoDbClient;

    public PaymentEventRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void save(PaymentEvent event) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("paymentId", AttributeValue.fromS(event.paymentId().toString()));
            item.put("createdAt", AttributeValue.fromS(event.createdAt().toString()));
            item.put("payerId", AttributeValue.fromS(event.payerId().toString()));
            item.put("payeeId", AttributeValue.fromS(event.payeeId().toString()));
            item.put("amount", AttributeValue.fromN(event.amount().toString()));
            item.put("type", AttributeValue.fromS(event.type().toString()));
            item.put("status", AttributeValue.fromS(event.status().toString()));

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(request);
            log.info("Payment event saved to DynamoDB: {} - {}", event.paymentId(), event.status());
        } catch (Exception e) {
            log.error("Failed to save payment event to DynamoDB: {}", event.paymentId(), e);
        }
    }
}