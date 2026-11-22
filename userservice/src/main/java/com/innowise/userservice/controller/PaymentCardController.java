package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
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

    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<PaymentCardDto>> findPaymentCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.findPaymentCardByUserId(userId));
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<PaymentCardDto> findPaymentCardById(@PathVariable Long cardId) {
        return ResponseEntity.ok(paymentCardService.findPaymentCardById(cardId));
    }

    @PutMapping("/cards/{cardId}")
    public ResponseEntity<PaymentCardDto> updatePaymentCard(
            @PathVariable Long cardId,
            @Valid @RequestBody PaymentCardDto paymentCardDto
    ) {
        return ResponseEntity.ok(paymentCardService.updatePaymentCard(cardId, paymentCardDto));
    }

    @PatchMapping("/cards/{cardId}/activate")
    public ResponseEntity<PaymentCardDto> activatePaymentCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(paymentCardService.activatePaymentCard(cardId));
    }

    @PatchMapping("/cards/{cardId}/deactivate")
    public ResponseEntity<PaymentCardDto> deactivatePaymentCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(paymentCardService.deactivatePaymentCard(cardId));
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long cardId) {
        paymentCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
