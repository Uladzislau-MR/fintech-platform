package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceCacheTest {

    @Autowired
    private UserService userService;


    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").clear();
        }
        if (cacheManager.getCache("user-lists") != null) {
            cacheManager.getCache("user-lists").clear();
        }
        if (cacheManager.getCache("user-pages") != null) {
            cacheManager.getCache("user-pages").clear();
        }
    }

    @Test
    void findById_shouldUseCacheOnSecondCall() {
        Long userId = 1L;
        User mockUser = new User(
                userId,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura@goldencalf.ru",
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));


        UserDto firstCallResult = userService.findById(userId);
        assertNotNull(firstCallResult);


        UserDto secondCallResult = userService.findById(userId);
        assertNotNull(secondCallResult);
        verify(userRepository, times(1)).findById(userId);
    }
}