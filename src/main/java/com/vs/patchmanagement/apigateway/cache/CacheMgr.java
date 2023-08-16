package com.vs.patchmanagement.apigateway.cache;


import com.vs.patchmanagement.apigateway.cache.models.CacheRecord;
import com.vs.patchmanagement.apigateway.constants.CacheDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CacheMgr {
    Environment environment;
    private final JedisPooled jedisPool;

    @Autowired
    CacheMgr(Environment environment){
        this.environment = environment;
        jedisPool = new JedisPooled(environment.getProperty("spring.cache.redis.host")
                , Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.cache.redis.port")))
                , environment.getProperty("spring.cache.redis.username")
                , environment.getProperty("spring.cache.redis.password"));
    }

    public Map<String, String> getHashesValue(String key){
        return jedisPool.hgetAll(key);
    }
    public boolean setHashesValue(CacheRecord<String, Map<String,String>> hashesRecord){
        try{
            jedisPool.hset(hashesRecord.getKey(), hashesRecord.getValue());
            if(hashesRecord.getExpireSecond() > 0)
                jedisPool.expire(hashesRecord.getKey(), hashesRecord.getExpireSecond());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public boolean setHashesKey(String key, String field, String value){
        try {
            jedisPool.hset(key, field, value);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public boolean setStringValue(CacheRecord<String,String> stringCacheRecord){
        try{
            jedisPool.set(stringCacheRecord.getKey(), stringCacheRecord.getValue() );
            if(stringCacheRecord.getExpireSecond() > 0)
                jedisPool.expire(stringCacheRecord.getKey(), stringCacheRecord.getExpireSecond());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public String getStringValue(String key){
        return jedisPool.get(key);
    }

    public int countKeys(String pattern, CacheDataType type, int count){
        return getKeys(pattern, type, count).size();
    }
    public List<String> getKeys(String pattern, CacheDataType type, int count){
        ScanParams scanParams = new ScanParams();
        scanParams.count(count == 0 ? 10 : count);
        scanParams.match(pattern);
        ScanResult<String> result = jedisPool.scan(String.valueOf(0), scanParams, type.toString());
        return result.getResult();
    }
    public boolean existKey(String key){
        return jedisPool.exists(key);
    }
    public void closePool(){
        jedisPool.close();
    }
}
