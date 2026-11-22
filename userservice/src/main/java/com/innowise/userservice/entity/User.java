package com.innowise.userservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users", indexes = {@Index(name = "idx_users_email", columnList = "email")})
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "surname")
    private String surname;

    @Column(nullable = false, name = "birth_date")
    private LocalDate birthDate;

    @Column(nullable = false, name = "email", unique = true)
    private String email;

    @Column(nullable = false, name = "active")
    private Boolean active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentCard> cards = new ArrayList<>();

    public User() {
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