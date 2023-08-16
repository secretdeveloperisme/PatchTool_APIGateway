package com.vs.patchmanagement.apigateway.cache.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CachedUserRole implements CacheRecord<String,String> {
    public static final String KEY_PATTERN = "user:%s:role";
    private String username;
    private short roleId;
    private long expiredSeconds;

    @Override
    public String getKey() {
        return KEY_PATTERN.formatted(username);
    }
    public static String getKey(String username){
        return KEY_PATTERN.formatted(username);
    }
    @Override
    public String getValue() {
        return String.valueOf(roleId);
    }

    @Override
    public long getExpireSecond() {
        return expiredSeconds;
    }
}
