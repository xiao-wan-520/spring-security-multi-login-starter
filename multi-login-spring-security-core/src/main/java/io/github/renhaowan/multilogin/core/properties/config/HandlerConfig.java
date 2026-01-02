package io.github.renhaowan.multilogin.core.properties.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wan
 * Handler Bean Name 配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandlerConfig {
    // 成功处理器 Bean Name
    private String success;

    // 失败处理器 Bean Name
    private String failure;
}
