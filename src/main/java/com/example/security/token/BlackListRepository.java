package com.example.security.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlackListRepository extends JpaRepository<BlackList, Integer> {

    // Access Token으로 검색
    Optional<BlackList> findByAccessToken(String token);
}
