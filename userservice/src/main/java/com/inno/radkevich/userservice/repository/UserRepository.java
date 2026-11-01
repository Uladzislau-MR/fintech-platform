package com.inno.radkevich.userservice.repository;

import com.inno.radkevich.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

    List<User> findByBirthDate(LocalDate localDate);

    @Query("SELECT u FROM User u WHERE  u.email = :email")
    Optional<User> findByEmail(@Param("email")  String email);

    @Query(value = "SELECT * FROM users WHERE active = :active", nativeQuery = true)
    List<User> findActiveUsers (@Param("active") Boolean active);

}
