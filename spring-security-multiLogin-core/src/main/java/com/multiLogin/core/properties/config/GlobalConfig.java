package com.multiLogin.core.properties.config;

import lombok.Data;

import java.util.List;

/**
 * @author wan
 * 全局配置类
 */
@Data
public class GlobalConfig {

    // 客户端请求头标识
    private String requestClientHeader = "request-client";

    // 客户端种类
    private List<String> clientTypes = List.of("DEFAULT");

    // 成功和失败处理回调
    private HandlerConfig handler = new HandlerConfig("defaultSuccessHandler", "defaultFailureHandler");
}
