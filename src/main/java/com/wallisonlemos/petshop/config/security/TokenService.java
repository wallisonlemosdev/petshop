package com.wallisonlemos.petshop.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${petshop.jwt.secret}")
    private String secret;

    @Value("${petshop.jwt.expiration}")
    private long expirationMillis;

    public String generateToken(Usuario usuario){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("auth")
                    .withSubject(usuario.getCpf())
                    .withExpiresAt(getExpirationDate())
                    .sign(algorithm);


        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro durante a geração do token", exception);
        }
    }

    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer("auth")
                    .build()
                    .verify(token)
                    .getSubject();
        }

        catch (JWTVerificationException exception) {
            return "";
        }
    }

    private Instant getExpirationDate(){
        return LocalDateTime
                .now()
                .plusSeconds(expirationMillis / 1000)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
