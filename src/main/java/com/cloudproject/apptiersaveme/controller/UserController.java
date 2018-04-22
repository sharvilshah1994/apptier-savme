package com.cloudproject.apptiersaveme.controller;

import com.cloudproject.apptiersaveme.exception.ResourceNotFoundException;
import com.cloudproject.apptiersaveme.model.User;
import com.cloudproject.apptiersaveme.repository.UserRepository;
import com.cloudproject.apptiersaveme.exception.BadRequestException;
import com.cloudproject.apptiersaveme.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getAllUsers(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return userRepository.findAll();
        }
        User userFromDb = userRepository.findUserById(userId);
        if (userFromDb == null) {
            throw new ResourceNotFoundException("User with userId: " + userId + " not found in the DB");
        }
        return Collections.singletonList(userFromDb);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public User addUser(@RequestBody User user) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found in the DB");
        }
        String userType = user.getUserType();
        if (!Constants.CLIENT_KEYWORD.equals(userType) && !Constants.DOCTOR_KEYWORD.equals(userType)) {
            throw new BadRequestException("User type should be `doctor` or `client`.");
        }
        return userRepository.save(user);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public User modifyUser(@RequestBody User user) {
        if (user.getId() == null) {
            throw new BadRequestException("`id` is required in json");
        }
        Long userId = user.getId();
        User userFromDb = userRepository.findUserById(userId);
        if (userFromDb == null) {
            throw new ResourceNotFoundException("User not found in the DB");
        }
        if (user.getAddress() != null) {
            userFromDb.setAddress(user.getAddress());
        }
        if (user.getAge() != null) {
            userFromDb.setAge(user.getAge());
        }
        if (user.getEmergencyNumber() != null) {
            userFromDb.setEmergencyNumber(user.getEmergencyNumber());
        }
        if (user.getFirstName() != null) {
            userFromDb.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            userFromDb.setLastName(user.getLastName());
        }
        if (user.getUserType() != null) {
            userFromDb.setUserType(user.getUserType());
        }
        if (user.getPhoneNumber() != null) {
            userFromDb.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getLocation() != null) {
            userFromDb.setLocation(user.getLocation());
        }
        if (user.getCurrentlyAvailable() != null) {
            userFromDb.setCurrentlyAvailable(user.getCurrentlyAvailable());
        }
        return userRepository.save(userFromDb);
    }
}
