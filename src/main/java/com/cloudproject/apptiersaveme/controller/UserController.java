package com.cloudproject.apptiersaveme.controller;

import com.cloudproject.apptiersaveme.model.User;
import com.cloudproject.apptiersaveme.repository.UserRepository;
import com.cloudproject.apptiersaveme.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public User addUser(@RequestBody User user) {
        if (user == null) {
            throw new BadRequestException("Empty json passed");
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
            throw new BadRequestException("User not found");
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
        return userRepository.save(userFromDb);
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUserById(@PathVariable("id") Long userId) {
        if (userId == null) {
            throw new BadRequestException("userId cannot be null");
        }
        User userFromDb = userRepository.findUserById(userId);
        if (userFromDb == null) {
            throw new BadRequestException("User not found");
        }
        return userFromDb;
    }
}
