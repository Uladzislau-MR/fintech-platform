package com.inno.radkevich.userservice.repository;

import com.inno.radkevich.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCardRepository extends JpaRepository<PaymentCard,Long> {
}
