package com.inno.radkevich.userservice.service;

import com.inno.radkevich.userservice.dto.PaymentCardDto;
import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.entity.PaymentCard;
import com.inno.radkevich.userservice.entity.User;
import com.inno.radkevich.userservice.exception.CardNotFoundException;
import com.inno.radkevich.userservice.exception.UserNotFoundException;
import com.inno.radkevich.userservice.mapper.UserMapper;
import com.inno.radkevich.userservice.repository.UserRepository;
import com.inno.radkevich.userservice.spec.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return userMapper.toDto(user);
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(user -> userMapper.toDto(user)).toList();
    }


    public List<UserDto> findByBirthDate(LocalDate date) {
        List<User> usersList = userRepository.findByBirthDate(date);
        return usersList.stream().map(user -> userMapper.toDto(user)).toList();
    }

    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toDto(user);
    }

    public List<UserDto> findActiveUsers(Boolean active) {
        List<User> usersList = userRepository.findActiveUsers(active);
        return usersList.stream().map(user -> userMapper.toDto(user)).toList();
    }


    public Page<UserDto> findByFilters(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(user -> userMapper.toDto(user));
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
       userRepository.save(user);
        return userMapper.toDto(user);

    }


    //Dirty Checking
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {


        User updatedUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        updatedUser.setName(userDto.name());
        updatedUser.setSurname(userDto.surname());
        updatedUser.setBirthDate(userDto.birthDate());
        updatedUser.setEmail(userDto.email());
        updatedUser.setActive(userDto.active());

        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);

    }

    @Transactional
    public UserDto activateUser(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(()->  new UserNotFoundException("User with" + id + "not found"));
        user.setActive(true);
        return userMapper.toDto(user);

    }


    @Transactional
    public UserDto deactivateUser(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(()->  new UserNotFoundException("User with" + id + "not found"));
        user.setActive(false);
        return userMapper.toDto(user);

    }



}
