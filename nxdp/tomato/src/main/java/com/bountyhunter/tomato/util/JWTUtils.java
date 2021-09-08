package com.bountyhunter.tomato.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.beans.BeanMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JWTUtils {

    public static <T extends Claims> String generateJWT(T claims, SecretKey secretKey) {
        try {
            Map<String, Object> claimsMap = new HashMap<>();
            BeanMap beanMap = BeanMap.create(claims);
            for (Object k : beanMap.keySet()) {
                claimsMap.put((String) k, beanMap.get(k));
            }
            return Jwts.builder()
                    .setClaims(claimsMap)
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static io.jsonwebtoken.Claims verify(SecretKey secretKey, String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public static void main(String[] args) throws JsonProcessingException {
        String key = "secret";
        Claims claims = new Claims("A", "B", "C", null, null, null, UUID.randomUUID().toString());
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), 0, key.getBytes().length, "AES");
        String token = JWTUtils.generateJWT(claims, secretKey);
        System.out.println(token);
        io.jsonwebtoken.Claims body = JWTUtils.verify(secretKey, token);
        System.out.println(new ObjectMapper().writeValueAsString(body));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Claims {
        private String issuer;
        private String subject;
        private String audience;
        private Date expiration;
        private Date notBefore;
        private Date issuedAt;
        private String id;
    }
}
