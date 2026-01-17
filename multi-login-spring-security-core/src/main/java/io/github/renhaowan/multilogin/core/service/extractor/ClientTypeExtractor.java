package io.github.renhaowan.multilogin.core.service.extractor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端类型提取器接口
 * 负责从 HttpServletRequest 中识别客户端类型（如 PC、APP、H5）
 *
 * @author wan
 */
public interface ClientTypeExtractor {

    /**
     * 从请求中提取客户端类型
     *
     * @param request HTTP 请求对象
     * @return 客户端类型标识
     */
    String extractClientType(HttpServletRequest request);
}
