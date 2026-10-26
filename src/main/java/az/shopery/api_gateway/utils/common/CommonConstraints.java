package az.shopery.api_gateway.utils.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstraints {

    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String TOKEN_ISSUED_AT_HEADER = "X-Token-Issued-At";
    public static final String WEBSOCKET_TOKEN_PARAM = "token";

    public static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/api/v1/shops/**",
            "/api/v1/products/**",
            "/api/v1/blogs/**",
            "/api/v1/dropdowns/**",
            "/api/v1/payments/stripe/webhook",
            "/actuator/health/**"
    };

    public static final String[] WEBSOCKET_PATHS = {
            "/ws",
            "/ws/**"
    };
}
