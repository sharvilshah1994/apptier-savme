package com.cloudproject.apptiersaveme.repository;

import com.cloudproject.apptiersaveme.model.Save;
import com.cloudproject.apptiersaveme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaveRepository extends JpaRepository<Save, Long> {

    Save findByUser(User user);
}
