package com.inno.radkevich.userservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_cards", indexes = {@Index(name = "idx_user", columnList = "user_id")})
@Getter
@Setter
public class PaymentCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false, name = "number")
    private String number;
    @Column(nullable = false, name = "holder")
    private String holder;
    @Column(nullable = false, name = "expiration_date")
    private String expirationDate;
    @Column(nullable = false, name = "active")
    private Boolean active;

    public PaymentCard() {
    }

    public PaymentCard(Long id, User user, String number, String holder, String expirationDate, Boolean active) {
        this.id = id;
        this.user = user;
        this.number = number;
        this.holder = holder;
        this.expirationDate = expirationDate;
        this.active = active;
    }
}


