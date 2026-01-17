package io.github.renhaowan.multilogin.autoconfigure;

import io.github.renhaowan.multilogin.autoconfigure.config.DefaultExtractorConfig;
import io.github.renhaowan.multilogin.autoconfigure.config.DefaultLoginHandlerConfig;
import io.github.renhaowan.multilogin.autoconfigure.factory.DynamicAuthenticationFilterFactory;
import io.github.renhaowan.multilogin.core.properties.MultiLoginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.util.List;

/**
 * @author wan
 * 配置过滤器
 */
@AutoConfiguration
@Import({DefaultLoginHandlerConfig.class, DefaultExtractorConfig.class})
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
        DynamicAuthenticationFilterFactory factory = new DynamicAuthenticationFilterFactory(
                properties, applicationContext
        );
        return factory.createFilters();
    }
}
