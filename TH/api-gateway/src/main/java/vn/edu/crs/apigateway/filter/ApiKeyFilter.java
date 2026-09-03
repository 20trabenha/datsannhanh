package vn.edu.crs.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyFilter implements GlobalFilter, Ordered {

    // Thêm giá trị mặc định để tránh lỗi sập app nếu chưa đọc được file yml
    @Value("${partner.api-key:crs-partner-key-2026}")
    private String configuredApiKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Kiểm tra API Key đối với các endpoint công khai dành cho đối tác
        if (path.startsWith("/api/public/courses")) {
            String apiKeyHeader = exchange.getRequest().getHeaders().getFirst("X-API-KEY");

            if (apiKeyHeader == null || !apiKeyHeader.equals(configuredApiKey)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}