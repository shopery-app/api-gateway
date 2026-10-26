package az.shopery.api_gateway.filter;

import az.shopery.api_gateway.utils.common.ErrorResponseWriter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RateLimitResponseFilter implements GlobalFilter, Ordered {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> setComplete() {
                if (Objects.equals(getStatusCode(), HttpStatus.TOO_MANY_REQUESTS)) {
                    return errorResponseWriter.write(exchange, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded, please try again later!");
                }
                return super.setComplete();
            }
        };
        return chain.filter(exchange.mutate().response(response).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
