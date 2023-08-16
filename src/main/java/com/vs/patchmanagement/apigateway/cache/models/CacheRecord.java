package com.vs.patchmanagement.apigateway.cache.models;

public interface CacheRecord<Key,Value>{
    Key getKey();
    Value getValue();
    long getExpireSecond();
}
