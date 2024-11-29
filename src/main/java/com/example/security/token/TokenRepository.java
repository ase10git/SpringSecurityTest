package com.example.security.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

    // Refresh Token으로 검색
    Optional<Token> findByRefreshToken(String token);

    // 사용자로 Refresh Token 검색
    List<Token> findAllByEmail(String email);
}
