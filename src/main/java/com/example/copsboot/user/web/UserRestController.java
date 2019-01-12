package com.example.copsboot.user.web;

import com.example.copsboot.infrastructure.security.config.ApplicationUserDetails;
import com.example.copsboot.user.User;
import com.example.copsboot.user.UserNotFoundException;
import com.example.copsboot.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService service;

    @Autowired
    public UserRestController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UserDto currentUser(@AuthenticationPrincipal ApplicationUserDetails
                                       userDetails) {
        User user = service.getUser(userDetails.getUserId())
                .orElseThrow(() -> new UserNotFoundException(userDetails
                        .getUserId()));
        return UserDto.fromUser(user);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createOfficer(@Valid @RequestBody CreateOfficerParameters parameters) {
        User officer = service.createOfficer(parameters.getEmail(), parameters.getPassword());
        return UserDto.fromUser(officer);
    }
}