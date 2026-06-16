package com.payment.payment_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topics.payments}")
    private String paymentsTopic;

    @Bean
    public NewTopic paymentsTopic() {
        return TopicBuilder
                .name(paymentsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}