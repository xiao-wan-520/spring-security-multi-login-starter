package io.github.renhaowan.multilogin.core;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.service.BusinessAuthenticationLogic;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wan
 */
public class RouterAuthenticationProvider implements AuthenticationProvider {
    // Map<ClientType, BusinessAuthenticationLogic>
    private final Map<String, BusinessAuthenticationLogic> businessProviders;

    public RouterAuthenticationProvider(List<BusinessAuthenticationLogic> providers, List<String> clientTypes) {
        this.businessProviders = new HashMap<>();
        // 建立 ClientType -> BusinessLogic 的映射关系
        for (int i = 0; i < clientTypes.size(); i++) {
            this.businessProviders.put(clientTypes.get(i), providers.get(i));
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 类型检查
        if (!(authentication instanceof BaseMultiLoginToken token)) {
            return null;
        }

        // 路由：根据客户端类型查找对应的业务 Provider
        String clientType = token.getClientType();
        BusinessAuthenticationLogic businessLogic = businessProviders.get(clientType);

        if (businessLogic == null) {
            throw new MultiLoginException("Login method provider not configured for client type: " + clientType);
        }

        // 执行业务逻辑
        Object principal = businessLogic.authenticate(token.getAllParams());

        if (principal == null) {
            throw new MultiLoginException("Authentication failed: User details is null.");
        }

        // 认证成功，设置已认证状态并返回
        token.setPrincipalDetails(principal);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 开发者无需实现，Starter 确保只处理自己的 Token
        return BaseMultiLoginToken.class.isAssignableFrom(authentication);
    }
}
