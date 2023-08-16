package com.vs.patchmanagement.apigateway.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vs.patchmanagement.apigateway.cache.handlers.TokenCacheHandling;
import com.vs.patchmanagement.apigateway.cache.handlers.UserRoleCacheHandling;
import com.vs.patchmanagement.apigateway.cache.models.CachedToken;
import com.vs.patchmanagement.apigateway.cache.models.CachedUserRole;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;

@Component
public class AuthenticationFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config>  {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory;
    private final TokenCacheHandling tokenCacheHandling;
    private final UserRoleCacheHandling userRoleCacheHandling;

    public AuthenticationFilterFactory(TokenCacheHandling tokenCacheHandling, ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilterFactory, UserRoleCacheHandling userRoleCacheHandling) {
        super(Config.class);
        this.tokenCacheHandling = tokenCacheHandling;
        this.modifyResponseBodyFilterFactory = modifyResponseBodyFilterFactory;
        this.userRoleCacheHandling = userRoleCacheHandling;
    }

    public GatewayFilter newFilter(){
        Config config = new Config();
        return apply(config);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return modifyResponseBodyFilterFactory
                .apply(c -> c.setRewriteFunction(String.class, String.class, new RewriteHandler(tokenCacheHandling, userRoleCacheHandling)));
    }

    public static class Config {
    }

    public static class RewriteHandler implements RewriteFunction<String,String> {
        private final TokenCacheHandling tokenCacheHandling;
        private final UserRoleCacheHandling userRoleCacheHandling;

        public RewriteHandler(TokenCacheHandling tokenCacheHandling, UserRoleCacheHandling userRoleCacheHandling) {
            this.tokenCacheHandling = tokenCacheHandling;
            this.userRoleCacheHandling = userRoleCacheHandling;
        }

        @Override
        public Publisher<String> apply(ServerWebExchange t, String responseStr) {
            ServerHttpResponse response = t.getResponse();
            ServerHttpRequest request = t.getRequest();
            System.out.println(responseStr);
            if(response.getStatusCode() == HttpStatus.OK){
                ObjectMapper objectMapper = new ObjectMapper();
                if(responseStr != null){
                    try {
                        JsonNode jsonNode = objectMapper.readValue(responseStr.getBytes(), JsonNode.class);
                        JsonNode dataNode = jsonNode.get("data");
                        String accessToken = dataNode.get("accessToken").textValue();
                        String username = dataNode.get("username").asText();
                        short role = dataNode.get("role").shortValue();
                        String userAgent = Objects.requireNonNullElse(request.getHeaders().get("User-Agent"),"").toString();
                        System.out.println("userAgent: " + userAgent);
                        CachedToken cachedToken = new CachedToken(username, userAgent, accessToken, TokenCacheHandling.ACCESS_TOKEN_EXPIRE_SECONDS);
                        tokenCacheHandling.storeToken(cachedToken);
                        CachedUserRole cachedUserRole = new CachedUserRole(username, role, UserRoleCacheHandling.USER_ROLE_EXPIRE_SECONDS);
                        userRoleCacheHandling.storeToken(cachedUserRole);
                    } catch (IOException e) {
                       return Mono.error(new RuntimeException("Cannot convert response to json"));
                    }

                }
            }
            return Mono.justOrEmpty(responseStr);
        }

    }
}