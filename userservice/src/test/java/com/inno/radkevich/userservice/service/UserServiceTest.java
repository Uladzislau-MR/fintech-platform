package com.inno.radkevich.userservice.service;

import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.entity.User;
import com.inno.radkevich.userservice.exception.UserNotFoundException;
import com.inno.radkevich.userservice.mapper.UserMapper;
import com.inno.radkevich.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                true
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
        assertThrows(UserNotFoundException.class, () -> {
            userService.findById(userId);
        });
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
        assertEquals(actualDtoList,dtoList);
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
}
