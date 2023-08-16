package com.vs.patchmanagement.apigateway.cache.handlers;

import com.vs.patchmanagement.apigateway.cache.CacheMgr;
import com.vs.patchmanagement.apigateway.cache.models.CachedToken;
import com.vs.patchmanagement.apigateway.constants.CacheDataType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TokenCacheHandling {
    private final CacheMgr cacheMgr;
    public static final long ACCESS_TOKEN_EXPIRE_SECONDS = 60*60;
    @Value(value = "${server.security.token-amount}")
    public int maximumAmountToken;

    TokenCacheHandling(CacheMgr cacheMgr){
        this.cacheMgr = cacheMgr;
    }
    public boolean storeToken(CachedToken cachedToken){
        return cacheMgr.setHashesValue(cachedToken);
    }
    public Optional<CachedToken> getToken(String username, String token){
        String key = CachedToken.getKey(username, token);
        Map<String, String> hashValue = cacheMgr.getHashesValue(key);
        if(!hashValue.isEmpty()){
            try{
                CachedToken cachedToken = new CachedToken();
                cachedToken.setUsername(username);
                cachedToken.setToken(token);
                cachedToken.setUserAgent(hashValue.get("userAgent"));
                cachedToken.setExpiredSeconds(Long.parseLong(hashValue.get("expireSeconds")));
                return Optional.of(cachedToken);
            }catch (Exception e){
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    public List<String> getAllTokens(String username){
        String keyPattern = CachedToken.getKey(username, "*");
        return cacheMgr.getKeys(keyPattern, CacheDataType.HASH, 100);
    }
    public int countTokens(String username){
        return getAllTokens(username).size();
    }
    public boolean existToken(String key){
        return cacheMgr.existKey(key);
    }
}
