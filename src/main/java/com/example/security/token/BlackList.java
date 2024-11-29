package com.example.security.token;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blacklist")
public class BlackList {

    @Id
    // ID는 자동 생성 전략 사용
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String accessToken;
}
