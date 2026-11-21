package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

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
                1L,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                true
        );

        expectedDto = new UserDto(
                1L,
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
    }

    @Test
    void findAll() {
        List<User> userList = new ArrayList<>();
        List<UserDto> dtoList = new ArrayList<>();
        userList.add(mockUser);
        dtoList.add(expectedDto);

        when(userRepository.findAll()).thenReturn(userList);
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        List<UserDto> actualDtoList = userService.findAll();

        assertEquals(dtoList, actualDtoList);
        verify(userRepository).findAll();
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void createUserTest() {
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toEntity(expectedDto)).thenReturn(mockUser);
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actualDto = userService.createUser(expectedDto);

        assertEquals(expectedDto, actualDto);
        verify(userMapper).toDto(mockUser);
        verify(userMapper).toEntity(expectedDto);
        verify(userRepository).save(mockUser);
    }

    @Test
    void findByBirthDateTest() {
        LocalDate birthDate = LocalDate.of(1902, 5, 15);
        when(userRepository.findByBirthDate(birthDate)).thenReturn(List.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        List<UserDto> actual = userService.findByBirthDate(birthDate);

        assertEquals(List.of(expectedDto), actual);
        verify(userRepository).findByBirthDate(birthDate);
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void findByEmailTest() {
        String email = "shura.malochnyi@goldencalf.ru";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actual = userService.findByEmail(email);

        assertEquals(expectedDto, actual);
        verify(userRepository).findByEmail(email);
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void findByEmailTestNegative() {
        String email = "missing@goldencalf.ru";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findByEmail(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findActiveUsersTest() {
        when(userRepository.findActiveUsers(true)).thenReturn(List.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        List<UserDto> actual = userService.findActiveUsers(true);

        assertEquals(List.of(expectedDto), actual);
        verify(userRepository).findActiveUsers(true);
        verify(userMapper).toDto(mockUser);
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
    void updateUserTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actual = userService.updateUser(userId, expectedDto);

        assertEquals(expectedDto, actual);
        assertEquals(expectedDto.name(), mockUser.getName());
        assertEquals(expectedDto.surname(), mockUser.getSurname());
        assertEquals(expectedDto.birthDate(), mockUser.getBirthDate());
        assertEquals(expectedDto.email(), mockUser.getEmail());
        assertEquals(expectedDto.active(), mockUser.getActive());
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(mockUser);
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
    }

    @Test
    void activateUserTest() {
        mockUser.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actual = userService.activateUser(userId);

        assertEquals(expectedDto, actual);
        assertEquals(true, mockUser.getActive());
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void activateUserTestNegative() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.activateUser(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void deactivateUserTest() {
        mockUser.setActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toDto(mockUser)).thenReturn(expectedDto);

        UserDto actual = userService.deactivateUser(userId);

        assertEquals(expectedDto, actual);
        assertEquals(false, mockUser.getActive());
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(mockUser);
    }

    @Test
    void deactivateUserTestNegative() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(userId));
        verify(userRepository).findById(userId);
    }
}