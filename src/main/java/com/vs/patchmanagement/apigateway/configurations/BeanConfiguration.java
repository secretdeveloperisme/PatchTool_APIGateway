package com.vs.patchmanagement.apigateway.configurations;


import com.vs.patchmanagement.apigateway.filters.AuthenticationFilterFactory;
import com.vs.patchmanagement.apigateway.filters.RequestGenerateTokenFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    private final AuthenticationFilterFactory authenticationFilterFactory;
    private final RequestGenerateTokenFilterFactory requestAuthenticationFilter;
    private final RequestGenerateTokenFilterFactory requestGenerateTokenFilterFactory;
    @Autowired
    BeanConfiguration(AuthenticationFilterFactory authenticationFilterFactory, RequestGenerateTokenFilterFactory requestAuthenticationFilter, RequestGenerateTokenFilterFactory requestGenerateTokenFilterFactory){
        this.authenticationFilterFactory = authenticationFilterFactory;
        this.requestAuthenticationFilter = requestAuthenticationFilter;
        this.requestGenerateTokenFilterFactory = requestGenerateTokenFilterFactory;
    }
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){

        return builder.routes()
                .route(predicateSpec -> predicateSpec.path("/api/users/login", "/api/users/reGenerateToken").filters(
                        gatewayFilterSpec -> gatewayFilterSpec.filters(requestAuthenticationFilter.newFilter(),authenticationFilterFactory.newFilter()))
                        .uri("lb://user-service"))
                .route(predicateSpec -> predicateSpec.path("/api/users/**").uri("lb://user-service"))
                .route(predicateSpec -> predicateSpec.path("/api/patch/**").uri("lb://patch-service"))
                .route(predicateSpec -> predicateSpec.path("/api/tickets/**").uri("lb://ticket-service"))
                .build();
    }
}
