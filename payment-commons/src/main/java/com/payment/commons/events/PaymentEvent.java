package com.payment.commons.events;

import com.payment.commons.enums.PaymentStatus;
import com.payment.commons.enums.PaymentType;
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