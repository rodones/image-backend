package com.image.backend.config.security;

import com.image.backend.enums.EClientId;
import com.image.backend.enums.EExpiration;
import com.image.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Logger logger;
    private final EClientId clientIds;
    private final Map<String, Long> clientAndExpirations;

    public JwtUtil(Logger logger, EExpiration expiration, EClientId clientIds) {
        this.logger = logger;
        this.clientIds = clientIds;
        clientAndExpirations = Map.of(
                clientIds.getWebClientId(), expiration.getJwtExpirationWeb(),
                clientIds.getMobileClientId(), expiration.getJwtExpirationMobile()
        );
    }

    @Value("${soilhm.jwt.secret}")
    private String jwtSecret;
    

    public String generateAccessToken(@NotNull User user, String clientId) {
        return generateAccessToken(user, clientAndExpirations.get(clientId),
                Map.of(
                        "roleId", user.getRole()
                )
        );
    }

    private String generateAccessToken(@NotNull User user, long expirationMs, Map<String, Object> claims) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMs);

        return Jwts.builder()
                .setId(String.valueOf(user.getId()))
                .setSubject(user.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }


    public boolean validateToken(String token) {
        try { //If token matches with our secret
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            logger.error("Invalid JWT Token - {}", ex.getMessage());
        }
        return false;
    }
}
