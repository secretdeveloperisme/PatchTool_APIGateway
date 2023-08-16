package com.vs.patchmanagement.apigateway.cache.handlers;

import com.vs.patchmanagement.apigateway.cache.CacheMgr;
import com.vs.patchmanagement.apigateway.cache.models.CachedUserRole;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRoleCacheHandling {
    private final CacheMgr cacheMgr;
    public static final long USER_ROLE_EXPIRE_SECONDS = 60*60;
    UserRoleCacheHandling(CacheMgr cacheMgr){
        this.cacheMgr = cacheMgr;
    }
    public boolean storeToken(CachedUserRole cachedToken){
        return cacheMgr.setStringValue(cachedToken);
    }
    public Optional<CachedUserRole> getUserRole(String username){
        String key = CachedUserRole.getKey(username);
        String stringValue = cacheMgr.getStringValue(key);
        if(!stringValue.isEmpty()){
            try{
                CachedUserRole cachedUserRole = new CachedUserRole(username, Short.parseShort(stringValue), USER_ROLE_EXPIRE_SECONDS);
                return Optional.of(cachedUserRole);
            }catch (Exception e){
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    public boolean existToken(String username){
        return cacheMgr.existKey(CachedUserRole.getKey(username));
    }
}
