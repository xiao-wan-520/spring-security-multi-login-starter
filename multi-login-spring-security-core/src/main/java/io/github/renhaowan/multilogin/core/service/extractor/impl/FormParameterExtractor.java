package io.github.renhaowan.multilogin.core.service.extractor.impl;

import io.github.renhaowan.multilogin.core.service.extractor.AbstractInlineParameterExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 表单参数提取器
 * 从 HTTP 请求参数（Form 表单）中提取配置指定的参数
 *
 * @author wan
 */
public class FormParameterExtractor extends AbstractInlineParameterExtractor implements ParameterExtractor {

    /**
     * 从请求中提取所有参数
     *
     * @param request HTTP 请求对象
     * @return 参数键值对 Map
     */
    @Override
    protected Map<String, Object> doExtractParameters(HttpServletRequest request, Set<String> paramNames) {
        Map<String, Object> params = new HashMap<>();
        for (String paramName : paramNames) {
            String value = request.getParameter(paramName);
            if (value != null) {
                params.put(paramName, value);
            }
        }
        return params;
    }
}
