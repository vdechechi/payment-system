package com.payment.payment_service.dto.payment;

import com.payment.payment_service.model.enums.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestDTO(
        UUID payerId,
        UUID payeeId,
        BigDecimal amount,
        PaymentType type
) {}
