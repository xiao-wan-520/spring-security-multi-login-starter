package io.github.renhaowan.multilogin.autoconfigure.factory;

import io.github.renhaowan.multilogin.core.DynamicAuthenticationFilter;
import io.github.renhaowan.multilogin.core.RouterAuthenticationProvider;
import io.github.renhaowan.multilogin.core.properties.MultiLoginProperties;
import io.github.renhaowan.multilogin.core.properties.config.GlobalConfig;
import io.github.renhaowan.multilogin.core.properties.config.HandlerConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import io.github.renhaowan.multilogin.core.service.BusinessAuthenticationLogic;
import io.github.renhaowan.multilogin.core.service.extractor.ClientTypeExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.impl.FormParameterExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.impl.HeaderClientTypeExtractor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 动态认证过滤器工厂
 * 使用简单工厂模式创建 DynamicAuthenticationFilter 实例
 *
 * @author wan
 */
public class DynamicAuthenticationFilterFactory {

    private final MultiLoginProperties properties;
    private final ApplicationContext applicationContext;

    public DynamicAuthenticationFilterFactory(MultiLoginProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    /**
     * 创建所有配置的认证过滤器
     *
     * @return 过滤器列表
     */
    public List<AbstractAuthenticationProcessingFilter> createFilters() {
        List<AbstractAuthenticationProcessingFilter> filters = new ArrayList<>();

        for (LoginMethodConfig config : properties.getMethods().values()) {
            DynamicAuthenticationFilter filter = createFilter(config);
            filters.add(filter);
        }

        return filters;
    }

    /**
     * 创建单个认证过滤器
     *
     * @param config 登录方法配置
     * @return 认证过滤器
     */
    private DynamicAuthenticationFilter createFilter(LoginMethodConfig config) {
        // 获取业务 Provider Bean
        List<BusinessAuthenticationLogic> businessLogics = getBusinessProviders(config);

        // 路由 Provider
        List<String> clientTypes = Optional.ofNullable(config.getClientTypes())
                .orElse(properties.getGlobal().getClientTypes());
        RouterAuthenticationProvider routerProvider = new RouterAuthenticationProvider(businessLogics, clientTypes);

        // ProviderManager
        ProviderManager providerManager = new ProviderManager(routerProvider);

        // 根据配置创建 Extractor
        ParameterExtractor parameterExtractor = getParameterExtractor(config, properties.getGlobal());
        ClientTypeExtractor clientTypeExtractor = getClientTypeExtractor(config, properties.getGlobal());

        // Dynamic Filter
        DynamicAuthenticationFilter filter = new DynamicAuthenticationFilter(
                config, parameterExtractor, clientTypeExtractor, providerManager
        );

        // 配置 Success/Failure Handler
        configureHandlers(config, filter);

        return filter;
    }

    /**
     * 获取参数提取器
     *
     * @param config       登录方法配置
     * @param globalConfig 全局配置
     * @return 参数提取器
     */
    private ParameterExtractor getParameterExtractor(LoginMethodConfig config, GlobalConfig globalConfig) {
        String parameterExtractorBeanName = Optional.ofNullable(config.getParameterExtractorBeanName())
                .orElse(globalConfig.getParameterExtractorBeanName());
        ParameterExtractor extractor;
        try {
            extractor = applicationContext.getBean(parameterExtractorBeanName, ParameterExtractor.class);
        } catch (BeansException e) {
            throw new IllegalArgumentException("ParameterExtractor Bean not found: " + parameterExtractorBeanName, e);
        }
        if (extractor instanceof FormParameterExtractor formParameterExtractor) {
            formParameterExtractor.setConfig(config);
            return formParameterExtractor;
        }
        return extractor;
    }

    /**
     * 获取客户端类型提取器
     *
     * @param config       登录方法配置
     * @param globalConfig 全局配置
     * @return 客户端类型提取器
     */
    private ClientTypeExtractor getClientTypeExtractor(LoginMethodConfig config, GlobalConfig globalConfig) {
        String clientTypeExtractorBeanName = Optional.ofNullable(config.getClientTypeExtractorBeanName())
                .orElse(globalConfig.getClientTypeExtractorBeanName());
        ClientTypeExtractor clientTypeExtractor;
        try {
            clientTypeExtractor = applicationContext.getBean(clientTypeExtractorBeanName, ClientTypeExtractor.class);
        } catch (BeansException e) {
            throw new IllegalArgumentException("ClientTypeExtractor Bean not found: " + clientTypeExtractorBeanName, e);
        }
        if (clientTypeExtractor instanceof HeaderClientTypeExtractor headerClientTypeExtractor) {
            headerClientTypeExtractor.setConfig(config);
            headerClientTypeExtractor.setGlobalConfig(globalConfig);
            return headerClientTypeExtractor;
        }
        return clientTypeExtractor;
    }

    /**
     * 获取业务逻辑提供者
     *
     * @param config 登录方法配置
     * @return 业务逻辑提供者列表
     */
    private List<BusinessAuthenticationLogic> getBusinessProviders(LoginMethodConfig config) {
        return config.getProviderBeanName().stream()
                .map(name -> (BusinessAuthenticationLogic) applicationContext.getBean(name))
                .toList();
    }

    /**
     * 配置成功/失败处理器
     *
     * @param config 登录方法配置
     * @param filter 认证过滤器
     */
    private void configureHandlers(LoginMethodConfig config, DynamicAuthenticationFilter filter) {
        String successHandlerName = Optional.ofNullable(config.getHandler())
                .map(HandlerConfig::getSuccess)
                .orElse(properties.getGlobal().getHandler().getSuccess());
        String failureHandlerName = Optional.ofNullable(config.getHandler())
                .map(HandlerConfig::getFailure)
                .orElse(properties.getGlobal().getHandler().getFailure());

        filter.setAuthenticationSuccessHandler(applicationContext.getBean(successHandlerName, AuthenticationSuccessHandler.class));
        filter.setAuthenticationFailureHandler(applicationContext.getBean(failureHandlerName, AuthenticationFailureHandler.class));
    }
}
