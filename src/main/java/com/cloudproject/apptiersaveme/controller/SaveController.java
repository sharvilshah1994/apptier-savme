package com.cloudproject.apptiersaveme.controller;

import com.cloudproject.apptiersaveme.exception.BadRequestException;
import com.cloudproject.apptiersaveme.exception.ResourceNotFoundException;
import com.cloudproject.apptiersaveme.model.User;
import com.cloudproject.apptiersaveme.repository.UserRepository;
import com.cloudproject.apptiersaveme.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/saveme")
public class SaveController {

    private final UserRepository userRepository;

    @Autowired
    public SaveController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> saveMeRequest(@RequestParam(value = "userId") Long userId) {
        if (userId == null) {
            throw new BadRequestException("Valid `userId` is required.");
        }
        User userInDanger = userRepository.findUserById(userId);
        if (userInDanger == null) {
            throw new ResourceNotFoundException("User with userId: " + userId + " not found in the DB");
        }
        return getAllDoctorsInUserRadius(userInDanger);
    }

    private List<User> getAllDoctorsInUserRadius(User userInDanger) {
        String userLocation = userInDanger.getLocation();
        List<User> docList = new ArrayList<>();
        String[] locations = userLocation.split(" ");
        Double clientLatitude = Double.valueOf(locations[0]);
        Double clientLongitude = Double.valueOf(locations[1]);
        List<User> doctorsList = userRepository.findAllByUserType(Constants.DOCTOR_KEYWORD);
        for (User user: doctorsList) {
            String[] docLocation = user.getLocation().split(" ");
            Double docLatitude = Double.valueOf(docLocation[0]);
            Double docLongitude = Double.valueOf(docLocation[1]);
            Double distance = distance(clientLatitude, clientLongitude, docLatitude, docLongitude);
            if (distance <= 3 && !user.getId().equals(userInDanger.getId())) {
                docList.add(user);
            }
        }
        return docList;
    }

    private static Double distance(Double startLat, Double startLong,
                                   Double endLat, Double endLong) {

        Double dLat  = Math.toRadians((endLat - startLat));
        Double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        Double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Constants.EARTH_RADIUS * c;
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
