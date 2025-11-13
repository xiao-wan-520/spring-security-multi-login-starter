package com.multiLogin.autoconfigure.config;

import com.multiLogin.core.DynamicAuthenticationFilter;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * @author wan
 * spring security 配置
 */
@Configuration
public class MultiLoginSecurity {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 注入自定义 Filter
     * 初始化多登录过滤器
     */
    public void initializeMultiLoginFilters(HttpSecurity http) throws Exception {

        // 不开启多方式登录
        List<AbstractAuthenticationProcessingFilter> multiLoginFilters;
        try {
            Object loginFilters = applicationContext.getBean("multiLoginFilters");
            multiLoginFilters = (List<AbstractAuthenticationProcessingFilter>) loginFilters;
        } catch (Exception ignored) {
            return;
        }

        // 允许配置的登录路径通过
        List<String> permittedUrls = multiLoginFilters.stream()
                .map(filter -> (((DynamicAuthenticationFilter)filter).getAntPathRequestMatcher().getPattern()))
                .toList();

        // 核心：将所有动态创建的 Filter 注入到 Spring Security 链中
        for (AbstractAuthenticationProcessingFilter filter : multiLoginFilters) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        // 放行登录接口
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permittedUrls.toArray(new String[0])).permitAll()
                );
    }
}
