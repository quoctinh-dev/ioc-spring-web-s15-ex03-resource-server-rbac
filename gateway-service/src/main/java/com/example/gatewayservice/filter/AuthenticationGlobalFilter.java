package com.example.gatewayservice.filter;

import com.example.gatewayservice.config.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Bỏ qua kiểm tra token cho endpoint login / public
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        // 2. Kiểm tra Header Authorization
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. Giải mã và validate token bằng JJWT 0.12.x
        if (!jwtUtils.validateToken(token)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 4. Lấy claim "role" từ Payload
        Claims claims = jwtUtils.extractAllClaims(token);
        String role = claims.get("role", String.class);

        // 5. Gắn Header X-User-Role vào request forward xuống phía sau
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Role", role != null ? role : "")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}