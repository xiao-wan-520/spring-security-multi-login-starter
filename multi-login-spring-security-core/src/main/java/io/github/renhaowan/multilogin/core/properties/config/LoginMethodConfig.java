package io.github.renhaowan.multilogin.core.properties.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wan
 * 登录方式配置类 (LoginMethodConfig)
 */
@Data
public class LoginMethodConfig {

    // 登录的URL
    private String processUrl;

    // 请求方式
    private String httpMethod = "POST";

    // 所有从请求中提取的参数名
    private List<String> paramName = new ArrayList<>();

    // 用于 Spring Security Principal 的参数名 (Token.getPrincipal())
    private List<String> principalParamName = new ArrayList<>();

    // 用于 Spring Security Credential 的参数名 (Token.getCredentials())
    private List<String> credentialParamName = new ArrayList<>();

    // 业务 Provider Bean Name 列表 (与 clientTypes 顺序对应)
    private List<String> providerBeanName = new ArrayList<>();

    // 【方法级覆盖】
    private String requestClientHeader;

    // 如果配置，则覆盖 Global
    private List<String> clientTypes;

    // 如果配置，则覆盖 Global
    private HandlerConfig handler;
}
