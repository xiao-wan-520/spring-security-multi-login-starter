package io.github.renhaowan.multilogin.core.service.extractor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 参数提取器接口
 * 负责从 HttpServletRequest 中提取登录所需的所有参数
 *
 * @author wan
 */
public interface ParameterExtractor {

    /**
     * 从请求中提取所有参数
     *
     * @param request HTTP 请求对象
     * @return 参数键值对 Map
     */
    Map<String, Object> extractParameters(HttpServletRequest request);
}
