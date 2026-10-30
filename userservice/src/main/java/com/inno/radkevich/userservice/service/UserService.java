package com.inno.radkevich.userservice.service;

import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.entity.User;
import com.inno.radkevich.userservice.exception.UserNotFoundException;
import com.inno.radkevich.userservice.mapper.UserMapper;
import com.inno.radkevich.userservice.repository.UserRepository;
import com.inno.radkevich.userservice.spec.UserSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
public class UserService {
    final private UserRepository userRepository;
    final private UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        return   userMapper.toDto(user);
    }

    public List<UserDto> findAll() {
                return userRepository.findAll().stream().map(user -> userMapper.toDto(user)).toList();
    }



    public List<UserDto> findByFilters(String name, String surname, String email, LocalDate birthDate, Boolean isActive) {
        List<User> userList = userRepository.findAll(Specification.where(UserSpecification.hasName(name)).
                and(UserSpecification.hasSurname(surname)).and(UserSpecification.hasEmail(email))
                        .and(UserSpecification.hasBirthDate(birthDate))
                        .and(UserSpecification.isActive(isActive)));
        return userList.stream().map(user -> userMapper.toDto(user)).toList();
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
       User createdUser =  userRepository.save(user);
       return userMapper.toDto(createdUser);

    }


    //Dirty Checking
    @Transactional
    public UserDto updateUser(Long id, UserDto  userDto) {


        User updatedUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        updatedUser.setName(userDto.name());
        updatedUser.setSurname(userDto.surname());
        updatedUser.setBirthDate(userDto.birthDate());
        updatedUser.setEmail(userDto.email());
        updatedUser.setActive(userDto.active());

       return  userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
      userRepository.delete(user);

    }

}
