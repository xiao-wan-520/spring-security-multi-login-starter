package com.multiLogin.autoconfigure.expend;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * @author wan
 */
public interface FilterChainExtend {

    // 拓展接口，用于拓展spring security配置
    void extend(HttpSecurity http);
}
