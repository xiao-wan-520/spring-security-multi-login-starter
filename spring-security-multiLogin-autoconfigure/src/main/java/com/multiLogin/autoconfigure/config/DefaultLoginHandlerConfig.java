package com.multiLogin.autoconfigure.config;

import com.multiLogin.core.service.handler.DefaultFailureHandler;
import com.multiLogin.core.service.handler.DefaultSuccessHandler;
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

    @Bean("defaultSuccessHandler")
    public AuthenticationSuccessHandler defaultSuccessHandler(){
        return new DefaultSuccessHandler();
    }

    @Bean("defaultFailureHandler")
    public AuthenticationFailureHandler defaultFailureHandler(){
        return new DefaultFailureHandler();
    }
}
