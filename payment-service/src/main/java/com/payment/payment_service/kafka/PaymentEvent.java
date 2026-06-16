package com.payment.payment_service.kafka;

import com.payment.payment_service.model.enums.PaymentStatus;
import com.payment.payment_service.model.enums.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentEvent(
        UUID paymentId,
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        PaymentType type,
        PaymentStatus status,
        LocalDateTime createdAt
) {}