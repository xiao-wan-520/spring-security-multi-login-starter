package com.multiLogin.autoconfigure.config;

import com.multiLogin.autoconfigure.expend.FilterChainExtend;
import com.multiLongin.core.DynamicAuthenticationFilter;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * @author wan
 * spring security 核心配置
 */
@Configuration
public class MultiLoginSecurityConfigure {

    @Resource
    private List<AbstractAuthenticationProcessingFilter> multiLoginFilters;

    @Bean
    @ConditionalOnMissingBean(FilterChainExtend.class)
    public FilterChainExtend filterChainExtend(){
        return http -> {
        };
    }

    /**
     * 配置 SecurityFilterChain 以注入自定义 Filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, FilterChainExtend filterChainExtend) throws Exception {

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
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permittedUrls.toArray(new String[0])).permitAll()
                );

        // 拓展接口配置导入
        filterChainExtend.extend(http);

        return http.build();
    }
}
