package com.cloudproject.apptiersaveme.controller;

import com.cloudproject.apptiersaveme.exception.BadRequestException;
import com.cloudproject.apptiersaveme.exception.ResourceNotFoundException;
import com.cloudproject.apptiersaveme.model.Logs;
import com.cloudproject.apptiersaveme.model.Save;
import com.cloudproject.apptiersaveme.model.User;
import com.cloudproject.apptiersaveme.model.vo.StatusVO;
import com.cloudproject.apptiersaveme.repository.LogsRepository;
import com.cloudproject.apptiersaveme.repository.SaveRepository;
import com.cloudproject.apptiersaveme.repository.UserRepository;
import com.cloudproject.apptiersaveme.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/saveme")
public class SaveController {

    private final UserRepository userRepository;

    private final SaveRepository saveRepository;

    private final LogsRepository logsRepository;

    @Autowired
    public SaveController(UserRepository userRepository, SaveRepository saveRepository, LogsRepository logsRepository) {
        this.userRepository = userRepository;
        this.saveRepository = saveRepository;
        this.logsRepository = logsRepository;
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
        Save saveRecord = saveRepository.findByUser(userInDanger);
        if (saveRecord != null) {
            throw new BadRequestException("You need to complete or cancel previous request");
        }
        createRecordSaveTable(userInDanger);
        return getAllDoctorsInUserRadius(userInDanger);
    }

    @RequestMapping(value = "/docresponse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Save responseToSaveMe(@RequestParam(value = "clientId") Long clientId, @RequestParam(value = "doctorId") Long doctorId) {
        if (clientId == null || doctorId == null) {
            throw new BadRequestException("`clientId` & `doctorId` params are required");
        }
        User userInDanger = userRepository.findUserById(clientId);
        userValidation(userInDanger);
        User doctor = userRepository.findUserById(doctorId);
        userValidation(doctor);
        Save saveRecord = saveRepository.findByUser(userInDanger);
        if (saveRecord == null) {
            throw new BadRequestException("Client has either completed the request or not requested at all");
        }
        if (saveRecord.getDoctorId() != null) {
            throw new BadRequestException("Another doctor is already serving this client");
        }
        validateDocDistance(userInDanger, doctor);
        Logs logRecord = logsRepository.findByRequestId(saveRecord.getId());
        saveRecord.setDoctorId(doctorId);
        logRecord.setDoctorId(doctorId);
        doctor.setCurrentlyAvailable(false);
        userRepository.save(doctor);
        logsRepository.save(logRecord);
        return saveRepository.save(saveRecord);
    }

    private void validateDocDistance(User userInDanger, User doctor) {
        String[] locations = userInDanger.getLocation().split(" ");
        Double clientLatitude = Double.valueOf(locations[0]);
        Double clientLongitude = Double.valueOf(locations[1]);
        String[] docLocation = doctor.getLocation().split(" ");
        Double docLatitude = Double.valueOf(docLocation[0]);
        Double docLongitude = Double.valueOf(docLocation[1]);
        Double distance = distance(clientLatitude, clientLongitude, docLatitude, docLongitude);
        if (distance > 3) {
            throw new BadRequestException("Doctor out of range!");
        }
    }

    private void userValidation(User user) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found in the DB!");
        }
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public StatusVO cancelSaveMeRequest(@RequestParam(value = "clientId") Long clientId,
                                        @RequestParam(value = "doctorId", required = false) Long doctorId,
                                        @RequestParam(value = "requester") String requester) {
        if (clientId == null) {
            throw new BadRequestException("`clientId` is required for this endpoint");
        }
        validateRequester(requester);
        User userInDanger = userRepository.findUserById(clientId);
        userValidation(userInDanger);
        Save saveRecord = saveRepository.findByUser(userInDanger);
        if (saveRecord == null) {
            throw new BadRequestException("Client has either completed the request or not requested at all");
        }
        Logs logs = logsRepository.findByRequestId(saveRecord.getId());
        if (Constants.CLIENT_KEYWORD.equals(requester)) {
            logs.setCompleted(true);
            if (logs.getDoctorId() != null) {
                User doctor = userRepository.findUserById(logs.getDoctorId());
                userValidation(doctor);
                updateDoctorStatus(doctor);
            }
            saveRepository.deleteById(saveRecord.getId());
        } else {
            if (doctorId == null) {
                throw new BadRequestException("`doctorId` is required for `doctor` requester");
            }
            User doctor = userRepository.findUserById(doctorId);
            userValidation(doctor);
            saveRecord.setDoctorId(null);
            logs.setDoctorId(null);
            saveRepository.save(saveRecord);
            updateDoctorStatus(doctor);
        }
        logsRepository.save(logs);
        return new StatusVO("Request cancelled");
    }

    private void validateRequester(String requester) {
        if (!Constants.CLIENT_KEYWORD.equals(requester) && !Constants.DOCTOR_KEYWORD.equals(requester)) {
            throw new BadRequestException("User type should be `doctor` or `client`.");
        }
    }

    @RequestMapping(value = "/complete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public StatusVO completeSaveMeRequest(@RequestParam(value = "clientId") Long clientId,
                                          @RequestParam(value = "doctorId", required = false) Long doctorId,
                                          @RequestParam(value = "requester") String requester) {
        if (clientId == null) {
            throw new BadRequestException("`clientId` is required for this endpoint");
        }
        validateRequester(requester);
        User userInDanger = userRepository.findUserById(clientId);
        userValidation(userInDanger);
        Save saveRecord = saveRepository.findByUser(userInDanger);
        if (saveRecord == null) {
            throw new BadRequestException("Client has either completed the request or not requested at all");
        }
        Logs logs = logsRepository.findByRequestId(saveRecord.getId());
        if (Constants.CLIENT_KEYWORD.equals(requester)) {
            logs.setCompleted(true);
            if (logs.getDoctorId() != null) {
                User doctor = userRepository.findUserById(logs.getDoctorId());
                userValidation(doctor);
                updateDoctorStatus(doctor);
            }
        } else {
            if (doctorId == null) {
                throw new BadRequestException("`doctorId` is required for `doctor` requester");
            }
            User doctor = userRepository.findUserById(doctorId);
            userValidation(doctor);
            logs.setCompleted(true);
            updateDoctorStatus(doctor);
        }
        saveRepository.delete(saveRecord);
        logsRepository.save(logs);
        return new StatusVO("Request completed");
    }

    private void updateDoctorStatus(User doctor) {
        doctor.setCurrentlyAvailable(true);
        userRepository.save(doctor);
    }

    private void createRecordSaveTable(User userInDanger) {
        Save saveRecord = new Save();
        saveRecord.setUser(userInDanger);
        saveRecord = saveRepository.save(saveRecord);
        Logs logs = new Logs();
        logs.setRequestId(saveRecord.getId());
        logs.setUser(userInDanger);
        logsRepository.save(logs);
    }

    private List<User> getAllDoctorsInUserRadius(User userInDanger) {
        String userLocation = userInDanger.getLocation();
        List<User> docList = new ArrayList<>();
        String[] locations = userLocation.split(" ");
        Double clientLatitude = Double.valueOf(locations[0]);
        Double clientLongitude = Double.valueOf(locations[1]);
        List<User> doctorsList = userRepository.findAllByUserTypeAndCurrentlyAvailable(Constants.DOCTOR_KEYWORD, true);
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
