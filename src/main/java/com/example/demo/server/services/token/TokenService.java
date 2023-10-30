package com.example.demo.server.services.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.demo.common.models.User;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Date;
import java.util.Map;


public class TokenService {
    private static TokenService INSTANCE = null;
    private final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private TokenService() {
    }
    public static TokenService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TokenService();
        }
        return INSTANCE;
    }
    public String createToken(User user,  String tokenSecret, long tokenExpiration){
        logger.debug("Creando token para el usuario: " + user.username());
       Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
         return JWT.create()
                .withClaim("userid", user.id())
                .withClaim("username", user.username())
                .withClaim("rol", user.role().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration))
                .sign(algorithm);
    }
    public boolean verifyToken(String token, String tokenSecret, User user) {
        logger.debug("Verifying token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verified");
            return decodedJWT.getClaim("userid").asLong() == user.id() &&
                    decodedJWT.getClaim("username").asString().equals(user.username()) &&
                    decodedJWT.getClaim("rol").asString().equals(user.role().toString());
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }
    public boolean verifyToken(String token, String tokenSecret) {
        logger.debug("Verifying token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verified");
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }
    public Map<String, Claim> getClaims(String token, String tokenSecret) {
        logger.debug("Getting claims from token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verified");
            return decodedJWT.getClaims();
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return null;
        }
    }
}
