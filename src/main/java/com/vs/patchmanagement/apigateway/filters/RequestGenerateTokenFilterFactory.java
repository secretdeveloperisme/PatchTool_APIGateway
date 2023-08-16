package com.vs.patchmanagement.apigateway.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.vs.patchmanagement.apigateway.cache.handlers.TokenCacheHandling;
import com.vs.patchmanagement.apigateway.configurations.RouterValidator;
import com.vs.patchmanagement.apigateway.dtos.ResponseObject;
import com.vs.patchmanagement.apigateway.utils.JWTUtil;
import com.vs.patchmanagement.apigateway.utils.JsonUtil;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Component
public class RequestGenerateTokenFilterFactory extends AbstractGatewayFilterFactory<RequestGenerateTokenFilterFactory.Config>  {
    private final List<HttpMessageReader<?>> messageReaders;
    private final TokenCacheHandling tokenCacheHandling;
    private final JsonUtil jsonUtil;

    public RequestGenerateTokenFilterFactory(TokenCacheHandling tokenCacheHandling, JsonUtil jsonUtil) {
        super(Config.class);
        this.tokenCacheHandling = tokenCacheHandling;
        this.jsonUtil = jsonUtil;
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }
    public GatewayFilter newFilter(){
        Config config = new Config();
        return apply(config);
    }

    public static class Config {
    }
    @Override
    @SuppressWarnings("unchecked")
    public GatewayFilter apply(RequestGenerateTokenFilterFactory.Config config) {
        return (exchange, chain) -> {
            ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);

            Mono<JsonNode> modifiedBody = serverRequest.bodyToMono(JsonNode.class)
                    .flatMap(originalBody -> (Mono)RewriteHandler.apply(exchange, originalBody, tokenCacheHandling))
                    .switchIfEmpty(Mono.defer(() -> (Mono) RewriteHandler.apply(exchange, null, tokenCacheHandling)));

            BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, JsonNode.class);
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.remove(HttpHeaders.CONTENT_LENGTH);
            CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
            return bodyInserter.insert(outputMessage, new BodyInserterContext())
                    .then(Mono.defer(() -> {
                        ServerHttpRequest decorator = decorate(exchange, headers, outputMessage);
                        return chain.filter(exchange.mutate().request(decorator).build());
                    })).onErrorResume((Function<Throwable, Mono<Void>>) throwable -> release(exchange,
                            outputMessage, throwable));
        };
    }

    protected Mono<Void> release(ServerWebExchange exchange, CachedBodyOutputMessage outputMessage,
                                 Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        String responseJson;
        try {
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            headers.setContentType(MediaType.APPLICATION_JSON);
            responseJson = jsonUtil.toJson(new ResponseObject(throwable.getMessage(),null));
        } catch (JsonProcessingException jsonProcessingException){
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            headers.setContentType(MediaType.TEXT_PLAIN);
            responseJson = jsonProcessingException.getMessage();
        }
        DataBuffer buffer = response.bufferFactory().wrap(responseJson.getBytes());
        return response.writeWith(Flux.just(buffer));
    }

    ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers,
                                        CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headers);
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                }
                else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

    public static class RewriteHandler {

        public static Publisher<JsonNode> apply(ServerWebExchange exchange, JsonNode jsonNode, TokenCacheHandling tokenCacheHandling) {
            ServerHttpRequest request = exchange.getRequest();
            String username = null;
            if(request.getURI().getPath().contains(RouterValidator.loginEndPoint)){
                if(jsonNode != null){
                    username = jsonNode.get("username").asText();
                }
            } else if (request.getURI().getPath().contains(RouterValidator.reGenerateTokenEndpoint)) {
                String refreshToken = request.getHeaders().getFirst("refreshToken");
                username = JWTUtil.getInstance().getSubject(refreshToken);
            }
            if(username != null){
                if(tokenCacheHandling.countTokens(username) >= tokenCacheHandling.maximumAmountToken){
                    return Mono.error(new RuntimeException("The amount of tokens exceed the limit"));
                }
            }
            return Mono.justOrEmpty(jsonNode);
        }

    }
}