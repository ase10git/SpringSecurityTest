package com.example.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                // session stateless로 인해 꺼 둠  
                .csrf((auth)->auth.disable())
                .authorizeRequests()
                .requestMatchers("/api/v1/auth/**") // 나열된 요청들은  
                .permitAll() // 모두 허용  
                .anyRequest() // 그 외의 모든 요청은  
                .authenticated() // 인증 필요  
                .and()
                .sessionManagement((session)->
                        session // session state는 저장되면 안되므로 stateless로 설정  
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class); // jwt 필터 가동  

        // cors 설정  
        http
                .cors((corsConfigurer)->
                        corsConfigurer.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // React client Origin을 허용
        corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
        // React client로부터 오는 모든 메소드 허용
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        // React client로부터 오는 credential(cookie) 허용
        corsConfiguration.setAllowCredentials(true);
        // React client로부터 오는 모든 헤더를 허용
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setMaxAge(3600L);
        // 클라이언트에 노출될 Authorization 헤더 설정
        corsConfiguration.setExposedHeaders(Collections.singletonList("Authorization"));

        // cors 설정을 URL에 매핑
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 CORS 설정 적용
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}