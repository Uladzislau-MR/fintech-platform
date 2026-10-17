package com.inno.radkevich.userservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_cards")
@Getter
@Setter
public class PaymentCard extends  BaseEntity

        {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
@ManyToOne
@JoinColumn(name="user_id")
   private User user;
   private String number;
   private String holder;
   private String expirationDate;
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


