package com.inno.radkevich.userservice.spec;

import com.inno.radkevich.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class UserSpecification {
    private UserSpecification() {
    }

    public static Specification<User> hasName(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank()) ? null :
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, cb) ->
                (surname == null || surname.isBlank()) ? null :
                        cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) ->
                (email == null || email.isBlank()) ? null :
                        cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<User> hasBirthDate(LocalDate birthDate) {
        return (root, query, cb) ->
                (birthDate == null) ? null :
                        cb.equal(root.get("birthDate"), birthDate);
    }


    public static Specification<User> isActive(Boolean status) {
        return (root, query, cb) ->
                (status == null) ? null :
                        cb.equal(root.get("active"), status);
    }
}
