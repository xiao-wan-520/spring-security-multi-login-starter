package io.github.renhaowan.multilogin.autoconfigure.config;

import io.github.renhaowan.multilogin.core.service.handler.DefaultFailureHandler;
import io.github.renhaowan.multilogin.core.service.handler.DefaultSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * @author wan
 * 默认登录成功/失败的处理器
 */
@Configuration
public class DefaultLoginHandlerConfig {

    /**
     * 默认登录成功处理器
     * @return 默认登录成功处理器
     */
    @Bean("defaultSuccessHandler")
    public AuthenticationSuccessHandler defaultSuccessHandler(){
        return new DefaultSuccessHandler();
    }

    /**
     * 默认登录失败处理器
     * @return 默认登录失败处理器
     */
    @Bean("defaultFailureHandler")
    public AuthenticationFailureHandler defaultFailureHandler(){
        return new DefaultFailureHandler();
    }
}
