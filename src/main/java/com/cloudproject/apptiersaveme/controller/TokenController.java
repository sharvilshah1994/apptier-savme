package com.cloudproject.apptiersaveme.controller;

import com.cloudproject.apptiersaveme.exception.BadRequestException;
import com.cloudproject.apptiersaveme.exception.ResourceNotFoundException;
import com.cloudproject.apptiersaveme.model.Token;
import com.cloudproject.apptiersaveme.model.User;
import com.cloudproject.apptiersaveme.repository.TokenRepository;
import com.cloudproject.apptiersaveme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
public class TokenController {

    private final TokenRepository tokenRepository;

    private final UserRepository userRepository;

    @Autowired
    public TokenController(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Token getUserToken(@RequestParam(value = "userId") Long userId) {
        if (userId == null) {
            throw new BadRequestException("User Id is required to access this endpoint");
        }
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User with `userId`: " + userId + " not found in the DB");
        }
        Token token = tokenRepository.findTokenByUser(user);
        if (token == null) {
            throw new ResourceNotFoundException("Token for user with `userId`: " + userId + " not found. Please generate the token & try again!");
        }
        return token;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Token postNewToken(@RequestParam(value = "userId") Long userId, @RequestBody Token token) {
        if (token == null || userId == null) {
            throw new BadRequestException("User Id & Token details are required to access this endpoint");
        }
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User with `userId`: " + userId + " not found in the DB");
        }
        if (token.getDeviceToken() == null) {
            throw new BadRequestException("Please pass a valid device token, it cannot be null");
        }
        token.setUser(user);
        return tokenRepository.save(token);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Token updateExistingToken(@RequestParam(value = "userId") Long userId, @RequestBody Token token) {
        if (token == null || userId == null) {
            throw new BadRequestException("User Id & Token details are required to access this endpoint");
        }
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User with `userId`: " + userId + " not found in the DB");
        }
        Long tokenId = token.getId();
        String deviceToken = token.getDeviceToken();
        if (tokenId == null || deviceToken == null) {
            throw new BadRequestException("Please pass a valid device token & valid token id, it cannot be null");
        }
        Token tokenFromDb = tokenRepository.findTokenById(tokenId);
        tokenFromDb.setDeviceToken(deviceToken);
        return tokenRepository.save(token);
    }
}
