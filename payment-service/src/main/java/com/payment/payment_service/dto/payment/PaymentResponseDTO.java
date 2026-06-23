package com.payment.payment_service.dto.payment;

import com.payment.commons.enums.PaymentStatus;
import com.payment.commons.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponseDTO(
        UUID id,
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        PaymentType type,
        PaymentStatus status,
        LocalDateTime createdAt
) {}