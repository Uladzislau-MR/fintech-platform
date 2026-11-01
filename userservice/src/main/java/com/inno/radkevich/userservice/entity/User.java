package com.inno.radkevich.userservice.entity;

import com.inno.radkevich.userservice.exception.CardNotFoundException;
import com.inno.radkevich.userservice.exception.CardsLimitException;
import com.inno.radkevich.userservice.exception.NoCardsException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter

@Table(name = "users", indexes = {@Index(name = "idx_email", columnList = "email")})
public class User extends BaseEntity {
    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;
    @Column(nullable = false, name = "name")
    @Setter
    private String name;
    @Column(nullable = false)
    @Setter
    private String surname;
    @Column(nullable = false, name = "birth_date")
    @Setter
    private LocalDate birthDate;
    @Setter
    @Column(nullable = false)
    private String email;
    @Setter
    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentCard> cards = new ArrayList<>();

    public User() {
    }

    public void addCard(PaymentCard paymentCard) {
        if(cards.size()<5) {
            paymentCard.setUser(this);
            cards.add(paymentCard);
        }else {
            throw new  CardsLimitException("Maximum limit of 5 payment cards reached");
        }
    }

    public void deleteCardFromUser(PaymentCard paymentCard) {

        if (cards.isEmpty()) {
            throw new NoCardsException("User does not have any payment card");
        }

        boolean removed = cards.removeIf(card -> {
            if (card.getId().equals(paymentCard.getId())) {
                card.setUser(null);
                return true;
            }
            return false;
        });

        if (!removed) {
            throw new CardNotFoundException("Payment card not found");
        }
    }




    public User(Long id, String name, String surname, LocalDate birthDate, String email, Boolean active) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.email = email;
        this.active = active;
    }
}

