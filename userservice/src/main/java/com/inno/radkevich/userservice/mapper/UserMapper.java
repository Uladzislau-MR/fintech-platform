package com.inno.radkevich.userservice.mapper;
import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;



@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

        UserDto toDto(User user);

        User toEntity(UserDto userDto);
    }

