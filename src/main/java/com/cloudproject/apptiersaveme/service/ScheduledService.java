package com.cloudproject.apptiersaveme.service;

import com.cloudproject.apptiersaveme.model.Logs;
import com.cloudproject.apptiersaveme.model.Save;
import com.cloudproject.apptiersaveme.model.User;
import com.cloudproject.apptiersaveme.repository.LogsRepository;
import com.cloudproject.apptiersaveme.repository.SaveRepository;
import com.cloudproject.apptiersaveme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ScheduledService {

    private final Logger LOGGER = Logger.getLogger(ScheduledService.class.getName());

    private final UserRepository userRepository;

    private final SaveRepository saveRepository;

    private final LogsRepository logsRepository;

    @Autowired
    public ScheduledService(UserRepository userRepository, SaveRepository saveRepository,
                            LogsRepository logsRepository) {
        this.userRepository = userRepository;
        this.saveRepository = saveRepository;
        this.logsRepository = logsRepository;
    }

    @Scheduled(fixedRate = 86400000)
    public void completeServiceIfNotResponded() {
        LOGGER.info("Scheduled service started");
        List<Save> saveList = saveRepository.findAll();
        for (Save save: saveList) {
            Timestamp time = save.getLastUpdatedTimeStamp();
            Date currDate = new Date();
            Long timeDiff = currDate.getTime() -  time.getTime();
            if (timeDiff > 1000) {
                Logs logs = logsRepository.findByRequestId(save.getId());
                logs.setCompleted(true);
                logsRepository.save(logs);
                User user = save.getUser();
                user.setCurrentlyAvailable(true);
                userRepository.save(user);
                saveRepository.deleteById(save.getId());
            }
        }
    }
}
