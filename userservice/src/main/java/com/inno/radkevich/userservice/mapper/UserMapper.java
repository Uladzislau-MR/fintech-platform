package com.inno.radkevich.userservice.mapper;
import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;



@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

        UserDto toDto(User user);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cards", ignore = true)
        User toEntity(UserDto userDto);
    }

