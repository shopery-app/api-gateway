package az.shopery.api_gateway.utils.security;

import static az.shopery.api_gateway.utils.common.CommonConstraints.PUBLIC_PATHS;
import static az.shopery.api_gateway.utils.common.CommonConstraints.WEBSOCKET_PATHS;
import static az.shopery.api_gateway.utils.common.CommonConstraints.WEBSOCKET_TOKEN_PARAM;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayBearerTokenConverter implements ServerAuthenticationConverter {

    private final ServerWebExchangeMatcher publicPathsMatcher = ServerWebExchangeMatchers.pathMatchers(PUBLIC_PATHS);
    private final ServerWebExchangeMatcher webSocketPathsMatcher = ServerWebExchangeMatchers.pathMatchers(WEBSOCKET_PATHS);
    private final ServerBearerTokenAuthenticationConverter bearerTokenConverter = new ServerBearerTokenAuthenticationConverter();

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return publicPathsMatcher.matches(exchange)
                .filter(matchResult -> !matchResult.isMatch())
                .flatMap(_ -> webSocketPathsMatcher.matches(exchange))
                .flatMap(matchResult -> matchResult.isMatch() ? convertFromQueryParam(exchange) : bearerTokenConverter.convert(exchange));
    }

    private Mono<Authentication> convertFromQueryParam(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getQueryParams().getFirst(WEBSOCKET_TOKEN_PARAM))
                .filter(token -> !token.isBlank())
                .map(BearerTokenAuthenticationToken::new);
    }
}
