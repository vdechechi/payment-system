package com.payment.payment_service.service;

import com.payment.commons.enums.PaymentStatus;
import com.payment.commons.events.PaymentEvent;
import com.payment.payment_service.aws.FraudCheckProducer;
import com.payment.payment_service.aws.PaymentEventRepository;
import com.payment.payment_service.dto.payment.PaymentRequestDTO;
import com.payment.payment_service.dto.payment.PaymentResponseDTO;
import com.payment.payment_service.exception.BusinessException;
import com.payment.payment_service.exception.ResourceNotFoundException;
import com.payment.payment_service.kafka.PaymentProducer;
import com.payment.payment_service.model.Payment;
import com.payment.payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;
    private final FraudCheckProducer fraudCheckProducer;
    private final PaymentEventRepository paymentEventRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentProducer paymentProducer,
                          FraudCheckProducer fraudCheckProducer,
                          PaymentEventRepository paymentEventRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentProducer = paymentProducer;
        this.fraudCheckProducer = fraudCheckProducer;
        this.paymentEventRepository = paymentEventRepository;
    }

    public PaymentResponseDTO create(PaymentRequestDTO dto) {
        if (dto.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor do pagamento deve ser maior que 0");
        }

        Payment payment = Payment.builder()
                .payeeId(dto.payeeId())
                .payerId(dto.payerId())
                .amount(dto.amount())
                .type(dto.type())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        PaymentEvent event = toPaymentEvent(saved);
        paymentProducer.sendPaymentEvent(event);
        fraudCheckProducer.sendForFraudCheck(event);
        paymentEventRepository.save(event);

        return toResponseDto(saved);
    }

    public PaymentResponseDTO findById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado para o id: " + id));
        return toResponseDto(payment);
    }

    public List<PaymentResponseDTO> findAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public PaymentResponseDTO approvePayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado para o id: " + id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only PENDING payments can be approved");
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updated = paymentRepository.save(payment);
        PaymentEvent event = toPaymentEvent(updated);
        paymentProducer.sendPaymentEvent(event);
        paymentEventRepository.save(event);
        return toResponseDto(updated);
    }

    public PaymentResponseDTO cancelPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado para o id: " + id));

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new BusinessException("Approved payments cannot be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updated = paymentRepository.save(payment);
        PaymentEvent event = toPaymentEvent(updated);
        paymentProducer.sendPaymentEvent(event);
        paymentEventRepository.save(event);
        return toResponseDto(updated);
    }

    private PaymentEvent toPaymentEvent(Payment payment) {
        return new PaymentEvent(
                payment.getId(),
                payment.getPayerId(),
                payment.getPayeeId(),
                payment.getAmount(),
                payment.getType(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }

    private PaymentResponseDTO toResponseDto(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getPayerId(),
                payment.getPayeeId(),
                payment.getAmount(),
                payment.getType(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}