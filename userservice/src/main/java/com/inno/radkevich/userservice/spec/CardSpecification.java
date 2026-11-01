package com.inno.radkevich.userservice.spec;

import com.inno.radkevich.userservice.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecification {


    public static Specification<PaymentCard> hasName(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank()) ? null :
                        cb.like(cb.lower(root.get("user").get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<PaymentCard> hasSurname(String surname) {
        return (root, query, cb) ->
                (surname == null || surname.isBlank()) ? null :
                        cb.like(cb.lower(root.get("user").get("surname")), "%" + surname.toLowerCase() + "%");

    }
}