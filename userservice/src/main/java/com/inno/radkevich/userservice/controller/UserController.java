package com.inno.radkevich.userservice.controller;

import com.inno.radkevich.userservice.dto.UserDto;
import com.inno.radkevich.userservice.service.UserService;
import jakarta.validation.Valid;
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
    public UserDto getUser(@PathVariable Long id){
        return userService.findById(id);
    }

    @GetMapping()
    public List<UserDto> getAllUsers(@RequestParam (required = false) String name,
                                     @RequestParam (required = false) String surname,
                                     @RequestParam (required = false) LocalDate birthDate,
                                     @RequestParam (required = false) String email,
                                     @RequestParam (required = false)  Boolean active
                                     ){
        return userService.findByFilters(name,surname, email, birthDate,active);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@Valid@RequestBody UserDto userDto, @PathVariable Long id) {
        return userService.updateUser(id,userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }


}

