package com.inno.radkevich.userservice.controller;

import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping()
    public Page<UserDto> getAllUsers(@RequestParam(required = false) String name,
                                     @RequestParam(required = false) String surname,
                                     Pageable pageable
    ) {
        return userService.findByFilters(name, surname, pageable);
    }

    @GetMapping(params = "birthDate")
    public List<UserDto> getUsersByBirthDate(@RequestParam LocalDate birthDate) {
        return userService.findByBirthDate(birthDate);
    }

    @GetMapping(value = "/email")
    public UserDto getUsersBySurname(@RequestParam String email) {
        return userService.findByEmail(email);
    }

    @GetMapping("/active")
    public List<UserDto> getActiveUsers(@RequestParam Boolean active) {
        return userService.findActiveUsers(active);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@Valid @RequestBody UserDto userDto, @PathVariable Long id) {
        return userService.updateUser(id, userDto);
    }


    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}/activate")
    public UserDto activateUser(@PathVariable Long id) {
        return userService.activateUser(id);
    }
    @PatchMapping("/{id}/deactivate")
    public UserDto deactivateUser(@PathVariable Long id) {
        return userService.deactivateUser(id);
    }
}

