package com.vs.patchmanagement.apigateway.cache.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CachedToken implements CacheRecord<String, Map<String,String>> {
    public static final String KEY_PATTERN = "user:%s:%s";
    private String userAgent;
    private String username;
    private String token;
    private long expiredSeconds;

    public CachedToken(){
        this.username = "";
        this.userAgent = "";
        this.token = "";
        this.expiredSeconds = 0;
    }
    public CachedToken(String username, String userAgent, String token, long expiredSeconds) {
        this.userAgent = userAgent;
        this.username = username;
        this.token = token;
        this.expiredSeconds = expiredSeconds;
    }
    public static String getKey(String username, String token){
        return KEY_PATTERN.formatted(username,token);
    }
    @Override
    public String getKey() {
       return KEY_PATTERN.formatted(username,token);
    }


    @Override
    public Map<String, String> getValue() {
        Map<String, String> hash = new HashMap<>();
        hash.put("userAgent", userAgent);
        hash.put("expireSeconds", String.valueOf(expiredSeconds));
        return hash;
    }

    @Override
    public long getExpireSecond() {
       return expiredSeconds;
    }
}
