package com.payment.payment_service.service;

import com.payment.payment_service.dto.payment.PaymentRequestDTO;
import com.payment.payment_service.dto.payment.PaymentResponseDTO;
import com.payment.payment_service.exception.BusinessException;
import com.payment.payment_service.exception.ResourceNotFoundException;
import com.payment.payment_service.kafka.PaymentEvent;
import com.payment.payment_service.kafka.PaymentProducer;
import com.payment.payment_service.model.Payment;
import com.payment.payment_service.model.enums.PaymentStatus;
import com.payment.payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    public PaymentService(PaymentRepository paymentRepository, PaymentProducer paymentProducer){
        this.paymentRepository = paymentRepository;
        this.paymentProducer = paymentProducer;
    }

    public PaymentResponseDTO create(PaymentRequestDTO dto){
        if(dto.amount().compareTo(java.math.BigDecimal.ZERO) <= 0){
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

        PaymentEvent event = new PaymentEvent(
                saved.getId(),
                saved.getPayerId(),
                saved.getPayeeId(),
                saved.getAmount(),
                saved.getType(),
                saved.getStatus(),
                saved.getCreatedAt()
        );

        paymentProducer.sendPaymentCreated(event);

        return toResponseDto(saved);
    }

    public PaymentResponseDTO findById(UUID id){

        Payment payment = paymentRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado para o id: " + id));

        return toResponseDto(payment);

    }

    public List<PaymentResponseDTO> findAll(){

        List<Payment> payments = paymentRepository.findAll();

        return payments
                .stream()
                .map(this::toResponseDto)
                .toList();

    }

    public PaymentResponseDTO approvePayment(UUID id){
        Payment payment = paymentRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado para o id: " + id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only PENDING payments can be approved");
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);
        return toResponseDto(updatedPayment);

    }

    public PaymentResponseDTO cancelPayment(UUID id){

        Payment payment = paymentRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Pagamento nao encontrado para o id: " + id));

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new BusinessException("Approved payments cannot be cancelled");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);
        return toResponseDto(updatedPayment);

    }

    private PaymentResponseDTO toResponseDto(Payment payment){
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
