package com.multiLongin.core.service;

import org.springframework.security.core.AuthenticationException;

import java.util.Map;

/**
 * @author wan
 * 开发者业务逻辑接口：负责根据参数校验用户并返回用户信息。
 * 替代了传统的 AuthenticationProvider。
 */
public interface BusinessAuthenticationLogic {

    /**
     * 执行业务认证逻辑
     * @param allParams 包含请求中所有配置参数的 Map<String, Object>
     * @return 认证成功后的用户主体信息（例如 UserDetails 或自定义的 AuthenticationPrincipal）
     */
    Object authenticate(Map<String, String> allParams) throws AuthenticationException;
}
