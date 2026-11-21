package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UserServiceRedisIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private UserDto testUser;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        userRepository.deleteAll();
        testUser = userService.createUser(new UserDto(
                null,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura@goldencalf.ru",
                true
        ));
    }

    @Test
    void findByIdCacheTest() {
        UserDto firstRead = userService.findById(testUser.id());
        UserDto secondRead = userService.findById(testUser.id());

        assertNotNull(firstRead);
        assertEquals(firstRead, secondRead);
        verify(userRepository, times(1)).findById(testUser.id());
    }

    @Test
    void findByEmailCacheTest() {
        UserDto firstRead = userService.findByEmail(testUser.email());
        UserDto secondRead = userService.findByEmail(testUser.email());

        assertNotNull(firstRead);
        assertEquals(firstRead, secondRead);
        verify(userRepository, times(1)).findByEmail(testUser.email());
    }

    @Test
    void findAllCacheTest() {
        List<UserDto> firstRead = userService.findAll();
        List<UserDto> secondRead = userService.findAll();

        assertEquals(1, firstRead.size());
        assertEquals(firstRead, secondRead);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findByFiltersCacheTest() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserDto> firstRead = userService.findByFilters("Shura", "Balaganov", pageable);
        Page<UserDto> secondRead = userService.findByFilters("Shura", "Balaganov", pageable);

        assertEquals(1, firstRead.getTotalElements());
        assertEquals(firstRead.getContent(), secondRead.getContent());
        verify(userRepository, times(1)).findAll(
                org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<User>>any(),
                org.mockito.ArgumentMatchers.eq(pageable)
        );
    }

    @Test
    void updateEvictCacheTest() {
        UserDto cached = userService.findById(testUser.id());
        assertEquals("Shura", cached.name());

        UserDto updatedDto = new UserDto(
                testUser.id(),
                "Alexander",
                "Balaganov",
                testUser.birthDate(),
                testUser.email(),
                testUser.active()
        );
        userService.updateUser(testUser.id(), updatedDto);

        UserDto afterUpdate = userService.findById(testUser.id());
        assertEquals("Alexander", afterUpdate.name());
        verify(userRepository, times(3)).findById(testUser.id());
    }

    @Test
    void deleteEvictCacheTest() {
        userService.findById(testUser.id());
        userService.deleteUser(testUser.id());

        assertThrows(UserNotFoundException.class, () -> userService.findById(testUser.id()));
    }
}