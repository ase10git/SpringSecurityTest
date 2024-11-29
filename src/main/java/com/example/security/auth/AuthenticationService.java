package com.example.security.auth;

import com.example.security.config.JwtService;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    // DB와 상호작용하는 사용자 repo
    private final UserRepository repository;
    // 비밀번호 인코더
    private final PasswordEncoder passwordEncoder;
    // jwt 서비스
    private final JwtService jwtService;
    // 사용자 신원 확인
    private final AuthenticationManager authenticationManager;

    // 회원가입
    @Transactional
    public ResponseEntity<AuthenticationResponse> register(RegisterRequest request) {
        // 요청으로부터 온 데이터로 사용자 객체 생성
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // 사용자 저장
        repository.save(user);

        // 토큰 생성 - 사용자 정보로 생성
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // 토큰을 db에 저장
        jwtService.saveUserToken(refreshToken, user);

        // cookie 생성
        HttpHeaders header = setCookieHeader(refreshToken);

        // 인증 응답 객체 생성
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();

        return new ResponseEntity<>(response, header, HttpStatus.OK);
    }

    // 인증 확인 - 로그인
    @Transactional
    public ResponseEntity<AuthenticationResponse> authenticate(
            AuthenticationRequest request
    ) {
        // 요청으로 들어온 사용자의 신원 확인
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // 위의 인증을 거친 사용자를 DB에 검색
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();

        // 토큰 생성 - 사용자 정보로 생성
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // 기존에 db에 저장된 사용자의 모든 Refresh Token 제거
        jwtService.removeAllUserToken(user);

        // 토큰을 db에 저장
        jwtService.saveUserToken(refreshToken, user);

        // cookie 생성
        HttpHeaders header = setCookieHeader(refreshToken);

        // 인증 응답 객체 생성
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();

        return new ResponseEntity<>(response, header, HttpStatus.OK);
    }

    // Access Token 재발급
    @Transactional
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request
            ) {
        // cookie 가져오기
        Cookie[] cookies = request.getCookies();

        // cookie가 없다면 인증 실패 응답 반환
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Refresh Token 저장 인스턴스
        String token = null;

        // cookie 들 중에서 이름이 refresh-token인 cookie의 값 가져오기
        for (Cookie cookie : cookies) {
            if ("refresh-token".equals(cookie.getName())) {
                token = cookie.getValue();
                break;
            }
        }

        // Refresh token이 cookie에 없다면 인증 실패 응답 반환
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // jwt로부터 사용자 이메일을 추출
        String userEmail = jwtService.extractUsername(token);

        // 검증 절차
        // 사용자 존재 여부
        User user = repository.findByEmail(userEmail)
                .orElseThrow(()->new UsernameNotFoundException("No user found"));

        // Refresh Token 유효성 검사
        if (jwtService.isRefreshTokenValid(token, user)) {
            // 유효할 경우 재발급 진행
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // 기존에 db에 저장된 Refresh Token 제거
            jwtService.removeUserToken(token, user);

            // 토큰을 db에 저장
            jwtService.saveUserToken(refreshToken, user);

            // cookie 생성
            HttpHeaders header = setCookieHeader(refreshToken);

            // 인증 응답 객체 생성
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .build();

            return new ResponseEntity<>(response, header, HttpStatus.OK);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // cookie 설정
    public HttpHeaders setCookieHeader(String refreshToken) {
        // Cookie 생성
        ResponseCookie cookie = ResponseCookie.from("refresh-token", refreshToken)
                .path("/") // cookie가 전송될 경로 설정
                .httpOnly(true) // 클라이언트에서 javascript로 접근 불가
                .secure(true) // https 적용
                .sameSite("Strict") // sameSite 적용
                .build();

        // Set-Cookie로 Header에 Cookie 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return headers;
    }
}
