package io.github.renhaowan.multilogin.core.service.extractor.impl;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.properties.config.GlobalConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import io.github.renhaowan.multilogin.core.service.extractor.ClientTypeExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

/**
 * 默认客户端类型提取器实现
 * 从请求头中提取客户端类型
 *
 * @author wan
 */
@Setter
public class HeaderClientTypeExtractor implements ClientTypeExtractor {

    /**
     * 登录方法配置
     */
    private LoginMethodConfig config;

    /**
     * 全局配置
     */
    private GlobalConfig globalConfig;

    /**
     * 从请求中提取客户端类型
     *
     * @param request HTTP 请求对象
     * @return 客户端类型
     */
    @Override
    public String extractClientType(HttpServletRequest request) {
        // 优先使用方法级配置，否则使用全局配置
        String requestClientHeader = Optional.ofNullable(config.getRequestClientHeader())
                .orElse(globalConfig.getRequestClientHeader());

        // 客户端类型列表：优先使用方法级配置，否则使用全局配置
        List<String> clientTypes = Optional.ofNullable(config.getClientTypes())
                .orElse(globalConfig.getClientTypes());

        String clientType = request.getHeader(requestClientHeader);

        // 如果未找到 Header 或 Header 值不在配置列表中，默认使用配置的第一个客户端类型 (支持配置的第一个客户端类型)
        if (clientType == null || !clientTypes.contains(clientType)) {
            if (!clientTypes.isEmpty()) {
                // 默认支持配置的第一个客户端类型
                return clientTypes.get(0);
            }
            throw new MultiLoginException("Client type cannot be determined and no default type is configured.");
        }

        return clientType;
    }
}