package com.example.security.config;

import com.example.security.token.BlackList;
import com.example.security.token.BlackListRepository;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    // DB와 상호작용하는 사용자 repo
    private final UserRepository userRepository;
    // Token Blacklist
    private final BlackListRepository blackListRepository;
    // jwt 서비스
    private final JwtService jwtService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
    {
        // 요청에서 Header 가져오기
        final String authHeader = request.getHeader("Authorization");

        // Authorization Header가 없으면 Access Token이 없으므로 failed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        // Access Token 추출
        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUsername(token);

        // token 내 이메일 확인
        if (userEmail != null) {
            // 사용자 검색
            User user = userRepository.findByEmail(userEmail).orElse(null);

            // token 유효성 검사
            if (user != null && jwtService.isAccessTokenValid(token, user)) {
                // Access Token을 BlackList에 추가
                BlackList blackList = new BlackList();
                blackList.setAccessToken(token);

                // BlackList 저장
                blackListRepository.save(blackList);

                // 기존에 db에 저장된 사용자의 모든 Refresh Token 제거
                jwtService.removeAllUserToken(user);
            }

        }
    }
}
