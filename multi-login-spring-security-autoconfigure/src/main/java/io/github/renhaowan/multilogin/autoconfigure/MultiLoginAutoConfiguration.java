package io.github.renhaowan.multilogin.autoconfigure;

import io.github.renhaowan.multilogin.autoconfigure.config.DefaultLoginHandlerConfig;
import io.github.renhaowan.multilogin.core.DynamicAuthenticationFilter;
import io.github.renhaowan.multilogin.core.RouterAuthenticationProvider;
import io.github.renhaowan.multilogin.core.properties.MultiLoginProperties;
import io.github.renhaowan.multilogin.core.properties.config.HandlerConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import io.github.renhaowan.multilogin.core.service.BusinessAuthenticationLogic;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author wan
 * 配置过滤器
 */
@AutoConfiguration
@Import(DefaultLoginHandlerConfig.class)
@EnableConfigurationProperties(MultiLoginProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "multi-login", name = "enabled", havingValue = "true")
public class MultiLoginAutoConfiguration {

    private final MultiLoginProperties properties;
    private final ApplicationContext applicationContext;

    /**
     * 自动装配所有的自定义认证过滤器
     * 过滤器列表将被 MultiLoginSecurityConfigurer 注入到 Spring Security 链中。
     */
    @Bean("multiLoginFilters")
    public List<AbstractAuthenticationProcessingFilter> multiLoginFilters() {
        List<AbstractAuthenticationProcessingFilter> filters = new ArrayList<>();

        for (LoginMethodConfig config : properties.getMethods().values()) {
            // 获取业务 Provider Bean
            List<BusinessAuthenticationLogic> businessLogics = getBusinessProviders(config);

            // 路由 Provider
            List<String> clientTypes = Optional.ofNullable(config.getClientTypes())
                    .orElse(properties.getGlobal().getClientTypes());
            RouterAuthenticationProvider routerProvider = new RouterAuthenticationProvider(businessLogics, clientTypes);

            // ProviderManager
            ProviderManager providerManager = new ProviderManager(routerProvider);

            // Dynamic Filter
            DynamicAuthenticationFilter filter = new DynamicAuthenticationFilter(
                    config, properties.getGlobal(), providerManager
            );

            // 配置 Success/Failure Handler
            configureHandlers(config, filter);

            filters.add(filter);
        }

        return filters;
    }

    // 获取业务 Provider 和配置 Handler
    private List<BusinessAuthenticationLogic> getBusinessProviders(LoginMethodConfig config) {
        return config.getProviderBeanName().stream()
                .map(name -> (BusinessAuthenticationLogic) applicationContext.getBean(name))
                .toList();
    }

    private void configureHandlers(LoginMethodConfig config, DynamicAuthenticationFilter filter) {
        // 确定最终的 Handler 配置
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
