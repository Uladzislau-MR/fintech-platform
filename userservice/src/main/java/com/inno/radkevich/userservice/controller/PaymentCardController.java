package com.inno.radkevich.userservice.controller;

import com.inno.radkevich.userservice.dto.PaymentCardDto;
import com.inno.radkevich.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentCardDto createPaymentCard(@Valid @RequestBody PaymentCardDto paymentCardDto, @PathVariable Long userId) {
        return paymentCardService.createPaymentCard(paymentCardDto, userId);
    }

    @GetMapping("/{id}")
    public PaymentCardDto findPaymentCardById(@PathVariable Long id) {
        return paymentCardService.findPaymentCardById(id);
    }

    @GetMapping()

    public Page<PaymentCardDto> getAllPaymentCards(@RequestParam(required = false) String name,
                                                   @RequestParam(required = false)  String surname,
                                                   Pageable pageable) {
        return paymentCardService.getAllPaymentCards(name, surname, pageable);
    }

    @GetMapping("/userId/{id}")
    public List<PaymentCardDto> findPaymentCardByUserId(@PathVariable Long id) {
        return paymentCardService.findPaymentCardByUserId(id);
    }

    @PutMapping("/{id}")
    public PaymentCardDto updatePaymentCard( @PathVariable Long id, @Valid @RequestBody PaymentCardDto paymentCardDto) {
        return paymentCardService.updatePaymentCard(id, paymentCardDto);
    }

    @PatchMapping("/{id}/activate")
    public PaymentCardDto activatePaymentCard(@PathVariable Long id) {
        return paymentCardService.activatePaymentCard(id);
    }

    @PatchMapping("/{id}/deactivate")
    public PaymentCardDto deactivatePaymentCard(@PathVariable Long id) {
        return paymentCardService.deactivatePaymentCard(id);
    }


}
