package com.cloudproject.apptiersaveme.repository;

import com.cloudproject.apptiersaveme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserById(Long id);

    List<User> findAllByUserTypeAndCurrentlyAvailable(String userType, Boolean available);

    User findByEmailAndPassword(String email, String password);
}
