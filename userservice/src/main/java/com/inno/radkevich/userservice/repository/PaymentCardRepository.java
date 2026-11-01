package com.inno.radkevich.userservice.repository;

import com.inno.radkevich.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard,Long>, JpaSpecificationExecutor<PaymentCard> {

    PaymentCard findPaymentCardByNumber(String number);

    @Query("SELECT p from PaymentCard p WHERE p.holder =:holder")
    List<PaymentCard> findPaymentCardByHolder(@Param("holder") String holder);

@Query(value = "SELECT * from payment_cards where active =:active", nativeQuery = true)
List<PaymentCard> findActivePaymentCards (Boolean active);

    List<PaymentCard> findByUserId(Long userId);

}
