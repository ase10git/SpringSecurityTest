package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    // Optional : 값이 있을수도 없을 수도 있는 null로 인한
    // NullPointerException을 방지할 수 있는 Java 8 클래스
    Optional<User> findByEmail(String email);
}
