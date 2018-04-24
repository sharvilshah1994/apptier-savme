package com.cloudproject.apptiersaveme.repository;

import com.cloudproject.apptiersaveme.model.Token;
import com.cloudproject.apptiersaveme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long>{

    Token findTokenByUser(User user);

    Token findTokenById(Long tokenId);
}
