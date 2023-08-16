package com.vs.patchmanagement.apigateway.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Payload;
import com.vs.patchmanagement.apigateway.constants.TokenType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class JWTUtil {
    private  static final long expiredAccessToken = (long) 1000 * 60 * 60;
    private  static final long expiredRefreshToken = 1000*60*60*24L;
    private static JWTUtil jwtUtil = null;
    private final Algorithm algorithm;

    public JWTUtil(){
        String secretKey = "patchtool";
        algorithm = Algorithm.HMAC256(secretKey.getBytes());
    }
    public static JWTUtil getInstance(){
        return (jwtUtil == null)?new JWTUtil():jwtUtil;
    }

    public String generateToken(TokenType tokenType, String username, Integer role){
        Date expiredDate;
        Map<String, Object> headers = new HashMap<>();
        if(tokenType == TokenType.ACCESS){
            headers.put("tokenType", TokenType.ACCESS.getValue());
            expiredDate = new Date(System.currentTimeMillis() + expiredAccessToken);
        }else{
            headers.put("tokenType", TokenType.ACCESS.getValue());
            expiredDate = new Date(System.currentTimeMillis() + expiredRefreshToken);
        }

        return JWT.create().withHeader(headers).withSubject(username + "," + role)
                .withIssuer("/auth/login").withExpiresAt(expiredDate)
                .sign(algorithm);
    }

    public DecodedJWT decodeTokenWithVerify(String token){
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        return jwtVerifier.verify(token);
    }
    public Optional<DecodedJWT> decodedJWT(String token){
        try{
            return Optional.of(JWT.decode(token));
        } catch (JWTDecodeException e){
            return Optional.empty();
        }
    }
    public String getSubject(String token){
        Optional<DecodedJWT> decodedJWTOptional = decodedJWT(token);
        return decodedJWTOptional.map(Payload::getSubject).orElse("");
    }
    public boolean verifyToken(String token){
        try{
            System.out.println("Expire: " + decodeTokenWithVerify(token).getExpiresAt());
            decodeTokenWithVerify(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public Claim decodePayload(String token, String claimName){
        DecodedJWT decodedJWT = decodeTokenWithVerify(token);
        return decodedJWT.getClaim(claimName);
    }
}
