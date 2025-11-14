# Spring Security Multi-Login Starter

> `spring-security-multi-login-starter` 是一个配置驱动的 Spring Security 扩展包，旨在通过自动装配机制，极大地简化多方式登录（如手机验证码、邮箱密码等）和多客户端（如 Customer、Employee）的接入。



##  1. 快速入门 (Quick Start)

### 1.1 依赖引入

```
<dependency>
    <groupId>com.multiLogin</groupId>
    <artifactId>spring-security-multiLogin-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 1.2 简单配置

> 此配置只实现了一个登录方式（`phone`），只有一个客户端类型（`customer`），并使用 Starter 提供的**默认成功/失败处理器**。

```yaml
multi-login:
  enabled: true
  methods:
    - name: phone
      process-url: /login/phone
      http-method: POST
      param-name:
        - phone
        - captcha
      principal-param-name: phone
      credential-param-name: captcha
      provider-bean-name:
        - phoneLoginService
```

### 1.3 业务实现

```java
@Service("phoneLoginService")
public class PhoneLoginService implements BusinessAuthenticationLogic {
    @Override
    public Object authenticate(Map<String, String> allParams) throws AuthenticationException {
        // ... 业务校验逻辑 ...
        return new User("user-" + allParams.get("phone"));
    }
}
```

### 1.4 配置`Spring Security`时使用框架的初始化方法

```java
@Configuration
public class SecurityConfig {

    @Resource
    private MultiLoginSecurity multiLoginSecurity;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        multiLoginSecurity.initializeMultiLoginFilters(http);          // 关键方法
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> {
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                });
        return http.build();
    }

}
```

### 1.5 启动与测试

- **请求:** `POST` 到 `/login/phone`，携带参数 `phone` 和 `captcha`。
- **客户端 Header:** **不需要**任何请求头。
- **结果:** 如果认证成功，返回 Starter 默认的 JSON 成功信息。



## 2. 高级配置（多方式登录与多客户端）

> 此配置实现了两种登录方式（`phone`, `email`），并支持两种客户端（`customer`, `employee`）。

```yaml
multi-login:
  # 开启多方式登录 Starter
  enabled: true

  global:
    # 客户端差异化认证（可选）
    request-client-header: X-Request-Client
    client-types:
      - customer
      - employee
    handler:
      success: jsonLoginSuccessHandler
  #      failure: jsonLoginFailureHandler

  # 定义所有登录方式的列表
  methods:
    # --- 1. 手机号验证码登录配置 ---
    phone:
      # 登录请求的路径和方式
      process-url: /login/phone
      http-method: POST
      param-name:
        - name
        - phone
        - nickname
        - role
      # Token中用于认证的主体（principal）和凭证（credential）字段名
      principal-param-name:
        - phone
      credential-param-name:
        - role
      # 认证Provider的实现类名（开发者必须提供的Bean）
      provider-bean-name:
        - phoneCustomerLoginService
        - phoneEmployeeLoginService

    # --- 2. 邮箱验证码登录配置 ---
    email: # 默认 process-url: /login/email
      principal-param-name: email    # 默认 param-name = principal-param-name + credential-param-name
      credential-param-name:
        - captcha
        - role
      request-client-header: X-Request-Client-Email
      client-types: employee
      provider-bean-name: emailLoginService
```



**核心设计原则：**

- **配置优先**：所有登录方式通过 `application.yml` 声明。
- **业务解耦**：开发者只需实现一个业务逻辑接口，无需接触 Spring Security 的底层接口。

| **特性**                 | **描述**                                                     | **实现机制**                                       |
| ------------------------ | ------------------------------------------------------------ | -------------------------------------------------- |
| **Provider 路由**        | 根据请求头中的客户端类型，将认证请求路由到对应的业务 Provider。 | `RouterAuthenticationProvider`                     |
| **无 Header 默认**       | **如果客户端未提供请求头**，系统将自动使用配置中**第一个** `provider-bean-name` 进行认证。 | `DynamicAuthenticationFilter` 逻辑判断             |
| **默认 Handler**         | 成功和失败处理器提供简单的 String 返回默认实现，无需额外配置。 | `DefaultSuccessHandler` 和 `DefaultFailureHandler` |
| **极简业务层**           | 开发者只需实现 `BusinessAuthenticationLogic` 接口。          | 业务层与 Spring Security 解耦                      |
| **Spring Security 集成** | 通过注入 `SecurityFilterChain` Bean，将动态 Filter 注册到认证链中。 | `MultiLoginSecurityConfigure`                      |



```yaml
multi-login:
  enabled: true
  methods:
    phone:
      principal-param-name: phone
      credential-param-name: captcha
      provider-bean-name: phoneLoginService
    email:
      principal-param-name: email
      credential-param-name:
        - captcha
        - role
      provider-bean-name: emailLoginService
```





