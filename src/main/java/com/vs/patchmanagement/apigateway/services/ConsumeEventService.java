package com.vs.patchmanagement.apigateway.services;

import com.vs.patchmanagement.apigateway.cache.handlers.UserRoleCacheHandling;
import com.vs.patchmanagement.apigateway.cache.models.CachedUserRole;
import com.vs.patchmanagement.apigateway.configurations.kafka.KafkaTopicConfig;
import com.vs.patchmanagement.apigateway.dtos.messages.ChangeRoleMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ConsumeEventService {

    private final UserRoleCacheHandling userRoleCacheHandling;

    public ConsumeEventService(UserRoleCacheHandling userRoleCacheHandling) {
        this.userRoleCacheHandling = userRoleCacheHandling;
    }

    @KafkaListener(topics = KafkaTopicConfig.CHANGE_ROLE_TOPIC, groupId = KafkaTopicConfig.CHANGE_ROLE_TOPIC)
    public void changeRoleEvent(@Payload ChangeRoleMessage changeRoleMessage){
        if(userRoleCacheHandling.existToken(changeRoleMessage.getUsername())){
            userRoleCacheHandling.storeToken(new CachedUserRole(changeRoleMessage.getUsername(), changeRoleMessage.getRole(),UserRoleCacheHandling.USER_ROLE_EXPIRE_SECONDS));
        }
    }
}
