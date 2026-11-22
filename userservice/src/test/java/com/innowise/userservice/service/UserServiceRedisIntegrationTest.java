package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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
                cache.invalidate();
            }
        });
        userRepository.deleteAll();
        testUser = userService.createUser(new UserDto(
                null,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura@goldencalf.ru",
                true,
                List.of()
        ));
    }

    @Test
    void createUserReturnsFilledDtoWithGeneratedIdTest() {
        assertNotNull(testUser.id());
        assertEquals("Shura", testUser.name());
        assertEquals("Balaganov", testUser.surname());
        assertEquals("shura@goldencalf.ru", testUser.email());
        assertEquals(true, testUser.active());
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
    void findByFiltersCacheTest() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserDto> firstRead = userService.findByFilters("Shura", "Balaganov", pageable);
        Page<UserDto> secondRead = userService.findByFilters("Shura", "Balaganov", pageable);

        assertEquals(1, firstRead.getTotalElements());
        assertEquals(firstRead.getContent(), secondRead.getContent());
        verify(userRepository, times(1)).findAll(
                ArgumentMatchers.<Specification<User>>any(),
                eq(pageable)
        );
    }

    @Test
    void findByFiltersCacheKeyIncludesSortTest() {
        Pageable byName = PageRequest.of(0, 10, Sort.by("name"));
        Pageable bySurname = PageRequest.of(0, 10, Sort.by("surname"));

        userService.findByFilters(null, null, byName);
        userService.findByFilters(null, null, bySurname);

        verify(userRepository, times(1)).findAll(ArgumentMatchers.<Specification<User>>any(), eq(byName));
        verify(userRepository, times(1)).findAll(ArgumentMatchers.<Specification<User>>any(), eq(bySurname));
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
                testUser.active(),
                List.of()
        );
        userService.updateUser(testUser.id(), updatedDto);

        UserDto afterUpdate = userService.findById(testUser.id());
        assertEquals("Alexander", afterUpdate.name());
    }

    @Test
    void updateStatusEvictCacheTest() {
        assertEquals(true, userService.findById(testUser.id()).active());

        userService.updateUserStatus(testUser.id(), false);

        assertEquals(false, userService.findById(testUser.id()).active());
    }

    @Test
    void deleteEvictCacheTest() {
        userService.findById(testUser.id());
        userService.deleteUser(testUser.id());

        assertThrows(UserNotFoundException.class, () -> userService.findById(testUser.id()));
    }
}
