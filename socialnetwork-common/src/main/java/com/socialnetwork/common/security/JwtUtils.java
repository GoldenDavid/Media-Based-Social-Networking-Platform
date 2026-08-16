package com.socialnetwork.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${app.jwt.secret:defaultSecretKeyWithAtLeast256BitsLengthForHmacSha256Algorithm}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs:86400000}")
    private int jwtExpirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                // Ensure default is base64 encoded string of 256 bits or more if no property is provided. 
                jwtSecret.equals("defaultSecretKeyWithAtLeast256BitsLengthForHmacSha256Algorithm") ? 
                "ZGVmYXVsdFNlY3JldEtleVdpdGhBdExlYXN0MjU2Qml0c0xlbmd0aEZvckhtYWNTaGEyNTZBbGdvcml0aG0=" : jwtSecret
        ));
    }

    public String generateJwtToken(UserPrincipal userPrincipal) {
        return Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .claim("id", userPrincipal.getId().toString())
                .claim("name", userPrincipal.getName())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String getUserIdFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().get("id", String.class);
    }
    
    public String getNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().get("name", String.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
