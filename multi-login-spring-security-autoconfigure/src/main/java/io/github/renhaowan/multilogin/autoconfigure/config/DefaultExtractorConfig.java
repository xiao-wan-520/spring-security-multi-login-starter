package io.github.renhaowan.multilogin.autoconfigure.config;

import io.github.renhaowan.multilogin.core.service.extractor.ClientTypeExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.impl.FormParameterExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.impl.HeaderClientTypeExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.impl.JsonParameterExtractor;
import org.springframework.context.annotation.Bean;

/**
 * 配置参数和客户端类型提取器
 *
 * @author wan
 */
public class DefaultExtractorConfig {

    /**
     * 默认参数提取器
     *
     * @return 表单参数提取器
     */
    @Bean("formParameterExtractor")
    public ParameterExtractor parameterExtractor() {
        return new FormParameterExtractor();
    }

    /**
     * 内置JSON参数提取器
     *
     * @return JSON参数提取器
     */
    @Bean("jsonParameterExtractor")
    public ParameterExtractor jsonParameterExtractor() {
        return new JsonParameterExtractor();
    }

    /**
     * 默认客户端类型提取器
     *
     * @return 请求头客户端类型提取器
     */
    @Bean("headerClientTypeExtractor")
    public ClientTypeExtractor clientTypeExtractor() {
        return new HeaderClientTypeExtractor();
    }
}
