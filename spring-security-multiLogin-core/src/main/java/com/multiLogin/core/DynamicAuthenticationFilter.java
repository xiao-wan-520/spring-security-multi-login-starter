package com.multiLogin.core;

import com.multiLogin.core.exception.MultiLoginException;
import com.multiLogin.core.properties.config.GlobalConfig;
import com.multiLogin.core.properties.config.LoginMethodConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.*;

/**
 * @author wan
 */
public class DynamicAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final LoginMethodConfig config;
    private final GlobalConfig globalConfig;

    @Getter
    private final AntPathRequestMatcher antPathRequestMatcher;

    public DynamicAuthenticationFilter(LoginMethodConfig config, GlobalConfig globalConfig, AuthenticationManager authenticationManager) {
        // 设置 Filter 拦截路径
        super(new AntPathRequestMatcher(config.getProcessUrl(), config.getHttpMethod()));
        this.antPathRequestMatcher = new AntPathRequestMatcher(config.getProcessUrl(), config.getHttpMethod());
        this.config = config;
        this.globalConfig = globalConfig;
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // 提取所有参数
        Map<String, String> allParams = extractAllParameters(request);

        // 提取客户端类型
        String clientType = extractClientType(request);

        // 创建 Token 实例
        BaseMultiLoginToken token = new BaseMultiLoginToken(
                allParams, clientType, config.getPrincipalParamName(), config.getCredentialParamName()
        );

        // 设置“认证请求”的附加信息，看UsernamePasswordAuthenticationFilter的setDetail方法就可以得到，设置session和ip
        token.setDetails(this.authenticationDetailsSource.buildDetails(request));

        // 委托给 AuthenticationManager (其中包含 Router Provider)
        return this.getAuthenticationManager().authenticate(token);
    }

    // 可以暴露接口使外部能够拓展，比如使用json提交的数据这里解析不了
    private Map<String, String> extractAllParameters(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();

        // 合并所有参数名并去重
        Set<String> allParamNames = new HashSet<>();
        allParamNames.addAll(config.getParamName());
        allParamNames.addAll(config.getPrincipalParamName());
        allParamNames.addAll(config.getCredentialParamName());

        for (String paramName : allParamNames) {
            String value = request.getParameter(paramName);
            if (value != null) {
                params.put(paramName, value);
            }
        }
        return params;
    }

    // 可以暴露接口使外部能欧拓展不使用请求头携带策略的方式
    private String extractClientType(HttpServletRequest request) {
        // 优先使用方法级配置，否则使用全局配置
        String requestClientHeader = Optional.ofNullable(config.getRequestClientHeader())
                .orElse(globalConfig.getRequestClientHeader());

        // 客户端类型列表：优先使用方法级配置，否则使用全局配置
        List<String> clientTypes = Optional.ofNullable(config.getClientTypes())
                .orElse(globalConfig.getClientTypes());

        String clientType = request.getHeader(requestClientHeader);

        // 如果未找到 Header 或 Header 值不在配置列表中，默认使用配置的第一个客户端类型 (支持配置的第一个客户端类型)
        if (clientType == null || !clientTypes.contains(clientType)) {
            if (!clientTypes.isEmpty()) {
                // 默认支持配置的第一个客户端类型
                return clientTypes.get(0);
            }
            throw new MultiLoginException("Client type cannot be determined and no default type is configured.");
        }

        return clientType;
    }
}
