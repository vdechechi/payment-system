package com.payment.payment_service.controller;

import com.payment.payment_service.dto.payment.PaymentRequestDTO;
import com.payment.payment_service.dto.payment.PaymentResponseDTO;
import com.payment.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> create(@RequestBody @Valid PaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PaymentResponseDTO> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.approvePayment(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.cancelPayment(id));
    }
}