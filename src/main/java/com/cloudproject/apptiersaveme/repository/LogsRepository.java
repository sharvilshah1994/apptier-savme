package com.cloudproject.apptiersaveme.repository;

import com.cloudproject.apptiersaveme.model.Logs;
import com.cloudproject.apptiersaveme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogsRepository extends JpaRepository<Logs, Long>{

    Logs findByRequestId(Long requestId);

    List<Logs> findLogsByUserOrderByLastUpdatedTimeStampDesc(User user);
}
