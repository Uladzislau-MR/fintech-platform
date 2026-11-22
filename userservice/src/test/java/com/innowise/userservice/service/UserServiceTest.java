package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private Long userId;
    private User mockUser;
    private UserDto expectedDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        mockUser = new User(
                userId,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                true
        );

        expectedDto = new UserDto(
                userId,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                true,
                List.of()
        );
    }

    @Test
    void findByIdTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actualDto = userService.findById(userId);

        assertEquals(expectedDto, actualDto);
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void findByIdTestNegative() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void findByFiltersTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(mockUser), pageable, 1);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        Page<UserDto> actual = userService.findByFilters("Shura", "Balaganov", pageable);

        assertEquals(1, actual.getTotalElements());
        assertEquals(expectedDto, actual.getContent().get(0));
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void createUserTest() {
        when(userMapper.toEntity(expectedDto)).thenReturn(mockUser);
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actualDto = userService.createUser(expectedDto);

        assertEquals(expectedDto, actualDto);
        verify(userMapper).toEntity(expectedDto);
        verify(userRepository).save(mockUser);
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void createUserDefaultsActiveToTrueTest() {
        User withoutActive = new User(
                null,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                null
        );
        UserDto dtoWithoutActive = new UserDto(
                null,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                null,
                null
        );
        when(userMapper.toEntity(dtoWithoutActive)).thenReturn(withoutActive);
        when(userRepository.save(withoutActive)).thenReturn(withoutActive);
        when(userMapper.toDto(withoutActive)).thenReturn(expectedDto);

        userService.createUser(dtoWithoutActive);

        assertTrue(withoutActive.getActive());
    }

    @Test
    void updateUserTest() {
        UserDto update = new UserDto(
                userId,
                "Alexander",
                "Ostap",
                LocalDate.of(1900, 1, 1),
                "new@goldencalf.ru",
                false,
                List.of()
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(update);

        UserDto actual = userService.updateUser(userId, update);

        assertEquals(update, actual);
        assertEquals("Alexander", mockUser.getName());
        assertEquals("Ostap", mockUser.getSurname());
        assertEquals(LocalDate.of(1900, 1, 1), mockUser.getBirthDate());
        assertEquals("new@goldencalf.ru", mockUser.getEmail());
        assertEquals(false, mockUser.getActive());
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUserKeepsActiveWhenNotProvidedTest() {
        UserDto update = new UserDto(
                userId,
                "Alexander",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                null,
                null
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        userService.updateUser(userId, update);

        assertTrue(mockUser.getActive());
    }

    @Test
    void updateUserTestNegative() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, expectedDto));
        verify(userRepository).findById(userId);
    }

    @Test
    void deleteUserTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(mockUser);
    }

    @Test
    void deleteUserTestNegative() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateUserStatusTest(boolean active) {
        when(userRepository.updateActiveById(userId, active)).thenReturn(1);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actual = userService.updateUserStatus(userId, active);

        assertEquals(expectedDto, actual);
        verify(userRepository).updateActiveById(userId, active);
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUserStatusTestNegative() {
        when(userRepository.updateActiveById(userId, true)).thenReturn(0);

        assertThrows(UserNotFoundException.class, () -> userService.updateUserStatus(userId, true));
        verify(userRepository, never()).findById(userId);
    }
}
