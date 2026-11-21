package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<PaymentCardDto> createPaymentCard(
            @PathVariable Long userId,
            @Valid @RequestBody PaymentCardDto paymentCardDto
    ) {
        PaymentCardDto created = paymentCardService.createPaymentCard(userId, paymentCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<PaymentCardDto> findPaymentCardById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.findPaymentCardById(id));
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<PaymentCardDto>> getAllPaymentCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {
        return ResponseEntity.ok(paymentCardService.getAllPaymentCards(name, surname, pageable));
    }

    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<PaymentCardDto>> findPaymentCardByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.findPaymentCardByUserId(userId));
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<PaymentCardDto> updatePaymentCard(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCardDto paymentCardDto
    ) {
        return ResponseEntity.ok(paymentCardService.updatePaymentCard(id, paymentCardDto));
    }

    @PatchMapping("/cards/{id}/activate")
    public ResponseEntity<PaymentCardDto> activatePaymentCard(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.activatePaymentCard(id));
    }

    @PatchMapping("/cards/{id}/deactivate")
    public ResponseEntity<PaymentCardDto> deactivatePaymentCard(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.deactivatePaymentCard(id));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}