package com.inno.radkevich.userservice.service;

import com.inno.radkevich.userservice.entity.User;
import com.inno.radkevich.userservice.repository.UserRepository;
import com.inno.radkevich.userservice.spec.UserSpecification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    final private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findByFilters(String name, String surname, String email) {
                return userRepository.findAll( UserSpecification.hasName(name).
                and(UserSpecification.hasSurname(surname).and(UserSpecification.hasEmail(email))));
    }
}
