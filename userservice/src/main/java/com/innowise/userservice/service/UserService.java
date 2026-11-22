package com.innowise.userservice.service;

import com.innowise.userservice.config.EvictAllCaches;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.spec.UserSpecification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.innowise.userservice.config.CacheNames.USERS;
import static com.innowise.userservice.config.CacheNames.USER_PAGES;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = USERS, key = "#id")
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with " + id + " not found"));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = USER_PAGES,
            key = "#name + '_' + #surname + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()"
    )
    public Page<UserDto> findByFilters(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));
        return userRepository.findAll(spec, pageable).map(userMapper::toDto);
    }

    @Transactional
    @EvictAllCaches
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        if (user.getActive() == null) {
            user.setActive(true);
        }
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    @EvictAllCaches
    public UserDto updateUser(Long id, UserDto userDto) {
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with " + id + " not found"));
        updatedUser.setName(userDto.name());
        updatedUser.setSurname(userDto.surname());
        updatedUser.setBirthDate(userDto.birthDate());
        updatedUser.setEmail(userDto.email());
        if (userDto.active() != null) {
            updatedUser.setActive(userDto.active());
        }
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    @EvictAllCaches
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with " + id + " not found"));
        userRepository.delete(user);
    }

    @Transactional
    @EvictAllCaches
    public UserDto updateUserStatus(Long id, boolean active) {
        if (userRepository.updateActiveById(id, active) == 0) {
            throw new UserNotFoundException("User with " + id + " not found");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with " + id + " not found"));
        return userMapper.toDto(user);
    }
}
