package io.github.renhaowan.multilogin.core;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;
import java.util.Map;

/**
 * @author wan
 * 多端登录基础 Token 类
 * 存储登录请求的核心参数和客户端信息
 */
public class BaseMultiLoginToken extends AbstractAuthenticationToken {

    /**
     * 登录请求所有参数集合
     */
    @Getter
    private final Map<String, String> allParams;

    /**
     * 客户端类型（如 PC、APP、H5）
     */
    @Getter
    private final String clientType;
    private final List<String> principalParamNames;
    private final List<String> credentialParamNames;

    /**
     * 存储认证成功后的 UserDetails 或自定义 Principal
     */
    private Object principalDetails;

    /**
     * 构造多端登录基础 Token
     * @param allParams 登录请求所有参数集合
     * @param clientType 客户端类型
     * @param principalParamNames 主体参数名称列表（如用户名、手机号）
     * @param credentialParamNames 凭证参数名称列表（如密码、验证码）
     */
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


    /**
     * 认证成功后，由 Router Provider 调用
     * @param principalDetails 认证成功后的 UserDetails 或自定义 Principal
     */
    public void setPrincipalDetails(Object principalDetails) {
        this.principalDetails = principalDetails;
        this.setAuthenticated(true);
    }
}
