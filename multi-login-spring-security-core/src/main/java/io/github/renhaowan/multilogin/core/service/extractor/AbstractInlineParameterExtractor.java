package io.github.renhaowan.multilogin.core.service.extractor;

import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 抽象框架内置参数提取器
 * 内置参数提取器从配置中封装参数。
 *
 * @author wan
 */
@Setter
public abstract class AbstractInlineParameterExtractor implements ParameterExtractor {

    /**
     * 登录方法配置
     */
    protected LoginMethodConfig config;


    /**
     * 对外暴露的统一入口（通用流程）
     *
     * @param request HTTP请求对象
     * @return 提取后的参数Map
     */
    @Override
    public Map<String, Object> extractParameters(HttpServletRequest request) {
        Set<String> allParamNames = mergeAndDeduplicateParamNames();
        if (CollectionUtils.isEmpty(allParamNames)) {
            return Collections.emptyMap();
        }
        // 子类实现具体的参数提取
        return doExtractParameters(request, allParamNames);
    }

    /**
     * 通用逻辑：合并参数名并去重（抽取出来复用）
     */
    private Set<String> mergeAndDeduplicateParamNames() {
        Set<String> allParamNames = new HashSet<>();
        allParamNames.addAll(config.getParamName());
        allParamNames.addAll(config.getPrincipalParamName());
        allParamNames.addAll(config.getCredentialParamName());
        return allParamNames;
    }


    /**
     * 模板方法：子类实现具体的参数提取逻辑
     * @param request HTTP请求对象
     * @param paramNames 需要提取的参数名集合
     * @return 提取后的参数Map
     */
    protected abstract Map<String, Object> doExtractParameters(HttpServletRequest request, Set<String> paramNames);

}
