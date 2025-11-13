package com.multiLogin.core;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;
import java.util.Map;

/**
 * @author wan
 */
public class BaseMultiLoginToken extends AbstractAuthenticationToken {
    @Getter
    private final Map<String, String> allParams;
    @Getter
    private final String clientType;
    private final List<String> principalParamNames;
    private final List<String> credentialParamNames;

    // 存储认证成功后的 UserDetails 或自定义 Principal
    private Object principalDetails;

    public BaseMultiLoginToken(Map<String, String> allParams, String clientType,
                               List<String> principalParamNames, List<String> credentialParamNames) {
        // 未认证状态
        super(null);
        this.allParams = allParams;
        this.clientType = clientType;
        this.principalParamNames = principalParamNames;
        this.credentialParamNames = credentialParamNames;
        setAuthenticated(false);
    }

    /**
     * 返回配置的主体参数列表。
     */
    @Override
    public Object getPrincipal() {
        if (principalDetails != null) {
            // 认证成功后返回 UserDetails
            return principalDetails;
        }
        return principalParamNames.stream()
                .map(allParams::get)
                .toList();
    }

    /**
     * 返回配置的凭证参数列表。
     */
    @Override
    public Object getCredentials() {
        return credentialParamNames.stream()
                .map(allParams::get)
                .toList();
    }


    /** 认证成功后，由 Router Provider 调用 */
    public void setPrincipalDetails(Object principalDetails) {
        this.principalDetails = principalDetails;
        this.setAuthenticated(true);
    }
}
