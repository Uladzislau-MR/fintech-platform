package com.inno.radkevich.userservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "users", indexes = {@Index(name = "idx_email", columnList = "email")})
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;
    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false)
    private String surname;
    @Column(nullable = false, name = "birth_date")
    private LocalDate birthDate;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private Boolean active;

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

