package com.notifications.notification_service.service;

import com.payment.commons.events.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void processPaymentNotification(PaymentEvent event) {
        switch (event.status()) {
            case PENDING -> log.info("Payment {} is pending. Notifying payer {}",
                    event.paymentId(), event.payerId());
            case APPROVED -> log.info("Payment {} approved! Notifying payer {} and payee {}",
                    event.paymentId(), event.payerId(), event.payeeId());
            case REJECTED -> log.info("Payment {} rejected. Notifying payer {}",
                    event.paymentId(), event.payerId());
            case CANCELLED -> log.info("Payment {} cancelled. Notifying payer {}",
                    event.paymentId(), event.payerId());
        }
    }
}