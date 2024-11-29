package com.example.security.config;

import com.example.security.token.BlackList;
import com.example.security.token.BlackListRepository;
import com.example.security.token.Token;
import com.example.security.token.TokenRepository;
import com.example.security.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    // DB와 상호작용하는 token repo
    private final TokenRepository tokenRepository;
    // blacklist
    private final BlackListRepository blackListRepository;

    @Value("${app.security.jwt.secret-key}")
    private String secretKey;

    // Access Token 만료기한
    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // Refresh Token 만료기한
    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // DB에 토큰 저장
    public void saveUserToken(String refreshToken, User user) {
        Token token = new Token(refreshToken, user.getEmail());
        tokenRepository.save(token);
    }

    // DB에서 토큰 제거
    public void removeUserToken(String refreshToken, User user) {
        Token token = new Token(refreshToken, user.getEmail());
        tokenRepository.delete(token);
    }

    // DB에 저장된 사용자의 모든 토큰 제거
    public void removeAllUserToken(User user) {
        List<Token> list = tokenRepository.findAllByEmail(user.getEmail());

        if (list != null && !list.isEmpty()) {
            list.forEach(tokenRepository::delete);
        }
    }

    // 토큰에서 사용자 이름 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 클레임 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Access 토큰 생성 - UserDetail로만 생성
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    // Access 토큰 생성
    public String generateAccessToken(
            Map<String, Object> extraClaims, // 토큰에 보낼 정보
            UserDetails userDetails
    ) {

        return generateToken(extraClaims, userDetails, accessTokenExpiration);
    }

    // Refresh 토큰 생성 - UserDetail로만 생성
    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    // Refresh 토큰 생성
    public String generateRefreshToken(
            Map<String, Object> extraClaims, // 토큰에 보낼 정보
            UserDetails userDetails
    ) {
        return generateToken(extraClaims, userDetails, refreshTokenExpiration);
    }

    // 토큰 생성
    private String generateToken(
            Map<String, Object> extraClaims, // 토큰에 보낼 정보
            UserDetails userDetails,
            long expireTime
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims) // 클레임 추가
                .setSubject(userDetails.getUsername()) // subject 추가
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰 발행일
                .setExpiration(new Date(System.currentTimeMillis() + expireTime)) // 만료기한
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Access Token 유효성 검사
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        // Access Token이 BlackList에 있는지 조회
        BlackList blackList = blackListRepository.findByAccessToken(token).orElse(null);
        boolean isBlackListToken = (blackList != null);

        // 토큰의 사용자 정보가 DB의 정보와 일치 여부 + 만료 기한 확인
        // DB에 사용자 정보가 없다면 여기서 false를 반환하여 유효하지 않음을 확인
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && !isBlackListToken;
    }

    // Refresh Token 유효성 검사
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // DB에 저장된 토큰 정보 가져오기
        Token dbToken = tokenRepository.findByRefreshToken(token).orElse(null);

        // 요청에 들어온 토큰 정보 유효성
        // DB 사용자와 토큰의 사용자 정보 일치 여부, 토큰 만료 여부
        boolean isValidRequestToken = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);

        // DB에 저장된 토큰 정보 유효성
        // DB에 토큰 존재 여부
        // 요청 사용자와 DB에 저장된 토큰의 사용자 정보 일치 여부
        boolean isValidDbToken = (dbToken != null)
                && username.equals(dbToken.getEmail())
                && userDetails.getUsername().equals(dbToken.getEmail());

        return isValidRequestToken && isValidDbToken;
    }

    // 토큰 만료 확인
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 토큰에서 만료 기한 가져오기
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // jwt에서 모든 클레임 추출
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // jwt 서명에 사용하는 비밀 키 생성
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
