package com.multiLogin.core.properties;

import com.multiLogin.core.exception.MultiLoginException;
import com.multiLogin.core.properties.config.GlobalConfig;
import com.multiLogin.core.properties.config.LoginMethodConfig;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wan
 * 主配置类
 */
@ConfigurationProperties(prefix = "multi-login")
@Data
public class MultiLoginProperties {
    private boolean enabled = false;
    private GlobalConfig global = new GlobalConfig();

    // key 表示登录的名称/策略
    private Map<String, LoginMethodConfig> methods = new HashMap<>();

    // 如果配置了 processUrl 则直接使用；如果未配置但有 name，则用 "/login/" + key 生成
    @PostConstruct
    public void determineProcessUrl() {
        for (Map.Entry<String, LoginMethodConfig> method : methods.entrySet()) {
            String key = method.getKey();
            String processUrl = method.getValue().getProcessUrl();
            // 若processUrl未配置（为空），动态生成
            if (processUrl == null || processUrl.trim().isEmpty()) {
                method.getValue().setProcessUrl("/login/" + key);
            }
        }
    }

    // 优先使用显式配置的 paramName（需包含 principalParamName + credentialParamName 所有元素）；
    // 未配置则自动用后两者合并作为 paramName
    @PostConstruct
    public void initParamName() {
        for (Map.Entry<String, LoginMethodConfig> method : methods.entrySet()) {
            LoginMethodConfig methodValue = method.getValue();
            List<String> paramName = methodValue.getParamName();
            List<String> principalParamName = methodValue.getPrincipalParamName();
            List<String> credentialParamName = methodValue.getCredentialParamName();
            // 未配置paramName：自动合并principal和credential的参数名（去重）
            if (paramName == null || paramName.isEmpty()) {
                List<String> mergedParams = new ArrayList<>(principalParamName);
                // 添加credential中不存在于principal的参数（避免重复）
                for (String credParam : credentialParamName) {
                    if (!mergedParams.contains(credParam)) {
                        mergedParams.add(credParam);
                    }
                }
                methodValue.setParamName(mergedParams);
            } else {
                // 已配置paramName：校验是否包含principal和credential的所有参数
                List<String> missingParams = getParams(principalParamName, paramName, credentialParamName);
                // 缺失则抛异常提示
                if (!missingParams.isEmpty()) {
                    throw new MultiLoginException("paramName must contain all the parameters of principalParamName and credentialParamName");
                }
            }
        }
    }

    private static List<String> getParams(List<String> principalParamName, List<String> paramName, List<String> credentialParamName) {
        List<String> missingParams = new ArrayList<>();
        // 校验principal的参数是否都在paramName中
        for (String principalParam : principalParamName) {
            if (!paramName.contains(principalParam)) {
                missingParams.add(principalParam);
            }
        }
        // 校验credential的参数是否都在paramName中
        for (String credParam : credentialParamName) {
            if (!paramName.contains(credParam)) {
                missingParams.add(credParam);
            }
        }
        return missingParams;
    }
}
