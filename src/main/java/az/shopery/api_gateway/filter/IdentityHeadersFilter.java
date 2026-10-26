package az.shopery.api_gateway.filter;

import static az.shopery.api_gateway.utils.common.CommonConstraints.TOKEN_ISSUED_AT_HEADER;
import static az.shopery.api_gateway.utils.common.CommonConstraints.USER_EMAIL_HEADER;

import java.util.Map;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class IdentityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .ofType(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(jwt -> Map.of(
                        USER_EMAIL_HEADER, jwt.getSubject(),
                        TOKEN_ISSUED_AT_HEADER, String.valueOf(jwt.getIssuedAt().getEpochSecond())
                ))
                .defaultIfEmpty(Map.of())
                .map(identityHeaders -> exchange.mutate()
                        .request(request -> request.headers(headers -> {
                            headers.remove(USER_EMAIL_HEADER);
                            headers.remove(TOKEN_ISSUED_AT_HEADER);
                            headers.setAll(identityHeaders);
                        }))
                        .build())
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
