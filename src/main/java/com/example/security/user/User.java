package com.example.security.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Entity // Entity임을 명시
@Builder // for Object building
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user") // DB에 테이블 이름 지정
public class User implements UserDetails {
    // Spring Security의 UserDetails

    @Id // id로 지정
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING) // Role이 Enum임을 명시
    // EnumType.STRING은 String value 순으로 정렬
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 역할 List를 반환
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    // 계정 만료 및 잠김에 대해선 테스트를 위해 true로 설정
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // UserDetails에 있는 getPassword()를 오버라이드
    // 현재 클래스의 password를 반환하도록 설정
    @Override
    public String getPassword() {
        return password;
    }
}
