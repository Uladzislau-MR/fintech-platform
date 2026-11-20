package com.inno.radkevich.userservice.mapper;


import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class UserMapperTest {


    private UserMapper userMapper;

    private User mockUser;
    private UserDto mockUserDto;

    @BeforeEach

    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        mockUser = new User(
                1L,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                true
        );

        mockUserDto = new UserDto(
                1L,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                true
        );
    }

    @Test
    void userToDtoTest() {
        UserDto dto = userMapper.toDto(mockUser);

        assertNotNull(dto);
        assertEquals(mockUser.getId(), dto.id());
        assertEquals(mockUser.getName(), dto.name());
        assertEquals(mockUser.getSurname(), dto.surname());
        assertEquals(mockUser.getBirthDate(), dto.birthDate());
        assertEquals(mockUser.getEmail(), dto.email());
        assertEquals(mockUser.getActive(), dto.active());
    }

    @Test
    void userDtoToEntityTest() {
        User entity = userMapper.toEntity(mockUserDto);

        assertNotNull(entity);
        assertEquals(mockUserDto.id(), entity.getId());
        assertEquals(mockUserDto.name(), entity.getName());
        assertEquals(mockUserDto.surname(), entity.getSurname());
        assertEquals(mockUserDto.birthDate(), entity.getBirthDate());
        assertEquals(mockUserDto.email(), entity.getEmail());
        assertEquals(mockUserDto.active(), entity.getActive());
    }
}