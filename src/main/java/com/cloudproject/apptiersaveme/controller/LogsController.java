package com.cloudproject.apptiersaveme.controller;

import com.cloudproject.apptiersaveme.exception.ResourceNotFoundException;
import com.cloudproject.apptiersaveme.model.Logs;
import com.cloudproject.apptiersaveme.model.User;
import com.cloudproject.apptiersaveme.repository.LogsRepository;
import com.cloudproject.apptiersaveme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/logs")
public class LogsController {

    private final LogsRepository logsRepository;

    private final UserRepository userRepository;

    @Autowired
    public LogsController(LogsRepository logsRepository, UserRepository userRepository) {
        this.logsRepository = logsRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Logs> getAllUsers(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return logsRepository.findAll();
        }
        User user = userRepository.findUserById(userId);
        userValidation(user);
        return logsRepository.findLogsByUserOrderByLastUpdatedTimeStampDesc(user);
    }

    private void userValidation(User user) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found in the DB!");
        }
    }
}
