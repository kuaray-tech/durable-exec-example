package com.example.payment.controller;

import com.example.payment.entity.Payment;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing endpoints to inspect payments.  This service does
 * not provide endpoints to create payments because payments are created as
 * part of Activities executed by Temporal.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentRepository paymentRepository;

    /**
     * Returns all payments recorded in the system.
     *
     * @return list of payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> listPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }
}