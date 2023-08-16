package com.vs.patchmanagement.apigateway.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vs.patchmanagement.apigateway.cache.handlers.TokenCacheHandling;
import com.vs.patchmanagement.apigateway.cache.handlers.UserRoleCacheHandling;
import com.vs.patchmanagement.apigateway.cache.models.CachedToken;
import com.vs.patchmanagement.apigateway.cache.models.CachedUserRole;
import com.vs.patchmanagement.apigateway.configurations.RouterValidator;
import com.vs.patchmanagement.apigateway.constants.Role;
import com.vs.patchmanagement.apigateway.dtos.ResponseObject;
import com.vs.patchmanagement.apigateway.utils.JWTUtil;
import com.vs.patchmanagement.apigateway.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

@RefreshScope
@Component
public class OnePerRequestFilter implements GlobalFilter {

    private final RouterValidator routerValidator;
    private final TokenCacheHandling tokenCacheHandling;
    private final UserRoleCacheHandling userRoleCacheHandling;
    private final JsonUtil jsonUtil;
    @Autowired
    OnePerRequestFilter(RouterValidator routerValidator, TokenCacheHandling tokenCacheHandling, UserRoleCacheHandling userRoleCacheHandling, JsonUtil jsonUtil){
        this.routerValidator = routerValidator;
        this.tokenCacheHandling = tokenCacheHandling;
        this.userRoleCacheHandling = userRoleCacheHandling;
        this.jsonUtil = jsonUtil;
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if (!routerValidator.isMatchingUrl(request, Role.GUEST)) {
            if (this.isAuthMissing(request))
                return onError(response, "Authorization header is missing", HttpStatus.BAD_REQUEST);
            final String bearToken = this.getAuthHeader(request);
            if(!bearToken.contains("Bearer"))
                return onError(response, "Bearer Token is empty", HttpStatus.BAD_REQUEST);
            String token = bearToken.substring("Bearer".length()+1);
            String username = JWTUtil.getInstance().getSubject(token);
            if(username.isEmpty())
                return onError(response, "Token subject is missing", HttpStatus.BAD_REQUEST);
            Optional<CachedToken> cachedTokenOptional = tokenCacheHandling.getToken(username, token);
            if (cachedTokenOptional.isEmpty())
                return onError(response, "Token is invalid", HttpStatus.BAD_REQUEST);
            CachedToken cachedToken = cachedTokenOptional.get();
            if(!token.equals(cachedToken.getToken())){
                return onError(response, "Token doest not match with the user", HttpStatus.BAD_REQUEST);
            }
            String requestUserAgent = Objects.requireNonNullElse(request.getHeaders().get("User-Agent"),"").toString();
            if(!cachedToken.getUserAgent().equals(requestUserAgent))
                return onError(response,"User Agent does not match with token", HttpStatus.CONFLICT);

            Optional<CachedUserRole> cachedUserRoleOptional = userRoleCacheHandling.getUserRole(username);
            if(cachedUserRoleOptional.isEmpty())
                return onError(response, "Cannot retrieve the role, please login again",HttpStatus.INTERNAL_SERVER_ERROR);

            CachedUserRole cachedUserRole = cachedUserRoleOptional.get();
            Role role = Role.getRoleFromId(cachedUserRole.getRoleId());
            if(!hasPermissionUrl(request, role)){
                return onError(response, "Don't have permission to get resource", HttpStatus.UNAUTHORIZED);
            }
            this.populatePrincipalHeaders(request, cachedToken.getUsername(), cachedUserRole.getRoleId());
        }
        return chain.filter(exchange);
    }
    private Mono<Void> onError(ServerHttpResponse response, String err, HttpStatus httpStatus) {
        response.setStatusCode(httpStatus);
        HttpHeaders headers = response.getHeaders();
        String responseJson;
        try {
            headers.setContentType(MediaType.APPLICATION_JSON);
            responseJson = jsonUtil.toJson(new ResponseObject(err,null));
        } catch (JsonProcessingException | RuntimeException jsonProcessingException){
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            headers.setContentType(MediaType.TEXT_PLAIN);
            responseJson = jsonProcessingException.getMessage();
        }
        DataBuffer buffer = response.bufferFactory().wrap(responseJson.getBytes());
        return response.writeWith(Flux.just(buffer));
    }
    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    private void populatePrincipalHeaders(ServerHttpRequest request, String username,short roleId) {
        request.mutate().header("username", username)
                .header("role", String.valueOf(roleId))
                        .build();
    }
    private boolean hasPermissionUrl(ServerHttpRequest request, Role role){
        return routerValidator.isMatchingUrl(request, role);
    }

}
