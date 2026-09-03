package vn.edu.crs.apigateway.filter;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.edu.crs.apigateway.util.JwtUtil;
import vn.edu.crs.apigateway.validator.RouteValidator;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return this.onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                jwtUtil.validateToken(token);

                Claims claims = jwtUtil.getAllClaimsFromToken(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                // Never forward identity headers supplied by the browser.  Downstream
                // services must receive only the identity derived from this verified JWT.
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .headers(headers -> {
                            headers.remove("X-User-Name");
                            headers.remove("X-User-Role");
                            headers.set("X-User-Name", username);
                            headers.set("X-User-Role", role != null ? role : "");
                        })
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return this.onError(exchange, "Unauthorized access: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String escapedError = err.replace("\\", "\\\\").replace("\"", "\\\"");
        byte[] body = ("{\"message\":\"" + escapedError + "\"}").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
