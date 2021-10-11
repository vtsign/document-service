package tech.vtsign.documentservice.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.vtsign.documentservice.model.LoginServerResponseDto;

import java.util.*;
import java.util.function.Function;

@Component
//@Slf4j
public class JwtUtil {
    @Value("${tech.vtsign.jwt.expired_token}")
    private long expiredToken;
    @Value("${tech.vtsign.jwt.expired_refresh_token}")
    private long expiredRefreshToken;
    @Value("${tech.vtsign.jwt.issuer}")
    private String issuer;
    @Value("${tech.vtsign.jwt.secret_key}")
    private String secretKey;


    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) throws SignatureException, ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build().parseClaimsJws(token).getBody();
    }

    //check if the token has expired
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //generate token for user
    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username, this.expiredToken);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username, this.expiredRefreshToken);
    }
    //test

    public LoginServerResponseDto getObjectFromToken(String token, String name) {
        return (LoginServerResponseDto) Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token).getBody().get(name);
    }

    public String generateAccessTokenObject(LoginServerResponseDto object) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user", object);
        return doGenerateToken(claims, object.getEmail(), this.expiredToken);
    }

    public String getObjectFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }


    private String doGenerateToken(Map<String, Object> claims, String subject, long expiredToken) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuer(this.issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredToken * 1000))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    //validate token
    public Boolean validateToken(String token, String username) {
        String usernameFromToken = getUsernameFromToken(token);
        return (usernameFromToken.equals(username) && !isTokenExpired(token));
    }
}


