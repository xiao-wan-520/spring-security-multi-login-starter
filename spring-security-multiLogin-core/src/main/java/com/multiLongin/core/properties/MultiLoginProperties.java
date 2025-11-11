package com.multiLongin.core.properties;

import com.multiLongin.core.properties.config.GlobalConfig;
import com.multiLongin.core.properties.config.LoginMethodConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wan
 * 主配置类
 */
@ConfigurationProperties(prefix = "multi-login")
@Data
public class MultiLoginProperties {
    private boolean enabled = false;
    private GlobalConfig global = new GlobalConfig();
    private List<LoginMethodConfig> methods = new ArrayList<>();
}
