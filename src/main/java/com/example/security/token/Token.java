package com.example.security.token;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity // Entity임을 명시
@Builder // for Object building
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "token") // DB에 테이블 이름 지정
public class Token {

    @Id
    private String refreshToken;
    private String email;
}
