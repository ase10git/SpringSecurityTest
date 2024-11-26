package com.example.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // 요청이 들어왔을 때 처리할 작업
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, // 요청
            @NonNull HttpServletResponse response, // 응답
            @NonNull FilterChain filterChain // 필터들
    ) throws ServletException, IOException {
        // 요청으로부터 온 header의 내용 추출
        // org.springframework.http.HttpHeaders의 HttpHeaders.AUTHORIZATION도 가능
        final String authHeader = request.getHeader("Authorization");
        // jwt
        final String jwt;
        // 사용자 이메일
        final String userEmail;

        // jwt가 없으면 요청을 이후 필터로 전달
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // jwt는 Authorization header에 Bearer schema를 사용한다.
            filterChain.doFilter(request, response);
            return;
        }

        // token 추출
        jwt = authHeader.substring(7); // "Bearer "는 7글자
        // jwt로부터 사용자 이메일을 추출
        userEmail = jwtService.extractUsername(jwt);

        // 검증 절차
        // 사용자가 존재하고, 아직 인증을 진행하지 않아 SecurityContextHolder에 저장되지 않았을 때
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // DB에서 해당 사용자 검색
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            // jwt 유효성 확인
            if (jwtService.isAccessTokenValid(jwt, userDetails)) {
                // Spring SecurityContext에 업데이트에 필요한 객체
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 항상 작업이 끝나면 다음 필터로 넘겨줘야 함
        filterChain.doFilter(request, response);
    }
}
