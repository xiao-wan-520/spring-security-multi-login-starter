# multi-login-spring-security-starter

![](https://img.shields.io/maven-central/v/io.github.xiao-wan-520/multi-login-spring-security-starter) [![Java CI with Maven](https://github.com/xiao-wan-520/multi-login-spring-security-starter/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/xiao-wan-520/multi-login-spring-security-starter/actions/workflows/maven.yml)

`multi-login-spring-security-starter` 是一个配置驱动的 Spring Security 扩展包，旨在通过自动装配机制，极大地简化多方式登录（如手机验证码、邮箱密码等）和多客户端（如 Customer、Employee）的接入。

##  1. 快速入门 (Quick Start)

### 1.1 依赖引入

```
<dependency>
    <groupId>io.github.xiao-wan-520</groupId>
    <artifactId>spring-security-multiLogin-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 1.2 简单配置

此配置只实现了**一个登录方式**（`phone`），只有**一个客户端类型**（`customer`），并使用 Starter 提供的**默认成功/失败处理器**。

```yaml
multi-login:
  enabled: true   # 开启 Starter
  methods:
    phone:
      process-url: /login/phone       # 登录请求路径
      http-method: POST
      param-name:                 # 登录请求需携带的所有参数名
        - phone
        - captcha
      principal-param-name: phone             # 用于认证的主体参数名
      credential-param-name: captcha          # 用于认证的凭证参数名
      provider-bean-name:                     # 认证逻辑实现的 Bean 名称
        - phoneLoginService
```

### 1.3 业务实现
开发者只需实现 `BusinessAuthenticationLogic` 接口，完成业务校验逻辑。
```java
@Service("phoneLoginService")
public class PhoneLoginService implements BusinessAuthenticationLogic {
    /**
     * @param allParams 登录请求中所有参数的 Map
     * @return 认证成功后返回一个 Spring Security 的 UserDetails 对象
     * @throws AuthenticationException 认证失败时抛出异常
     */
    @Override
    public Object authenticate(Map<String, String> allParams) throws AuthenticationException {
        // ... 业务校验逻辑：如校验手机号和验证码是否匹配 ...
        // 成功则返回一个代表用户的对象
        return new User("user-" + allParams.get("phone"));
    }
}
```

### 1.4 Spring Security 集成配置
在 Spring Security 配置中，注入 `MultiLoginSecurity` 并调用其关键的初始化方法。

```java
@Configuration
public class SecurityConfig {

    @Resource
    private MultiLoginSecurity multiLoginSecurity; // 注入 Starter 核心类

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 关键方法：将动态认证 Filter 注册到 Spring Security 认证链中
        multiLoginSecurity.initializeMultiLoginFilters(http);

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionManagement) -> {
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 通常用于 RESTful API
                });
        return http.build();
    }
}
```

### 1.5 启动与测试

- **请求**: POST 到 /login/phone

- **参数**: 携带 phone 和 captcha

- **客户端 Header**: 无特殊要求。

- **结果**: 认证成功返回 Starter 默认的String成功信息。



## 2. 高级配置（多方式登录与多客户端）

此配置展示了如何实现 **两种登录方式** (`phone`, `email`)，并支持 **两种客户端类型** (`customer`, `employee`)。

### 2.1 完整 YAML 配置示例

```yml
multi-login:
  enabled: true 

  global:
    # 客户端差异化认证：指定哪个请求头用于区分客户端类型（可选）
    request-client-header: X-Request-Client
    # 定义所有允许的客户端类型
    client-types:
      - customer
      - employee
    handler:
      # 统一配置成功的处理器 Bean 名称 (需开发者实现)
      success: jsonLoginSuccessHandler 
#     failure: jsonLoginFailureHandler # 可选，使用默认失败处理器则无需配置
  
  methods:
    # --- 1. 手机号验证码登录配置 (多 Provider) ---
    phone:
      process-url: /login/phone
      http-method: POST
      param-name: # 允许携带的全部参数
        - name
        - phone
        - nickname
        - role
      principal-param-name: phone # Principal (主体)
      credential-param-name: role # Credential (凭证/角色等)
      # 关键：为不同客户端指定不同的 Provider Bean
      provider-bean-name: 
        - phoneCustomerLoginService # 默认或 X-Request-Client: customer
        - phoneEmployeeLoginService # X-Request-Client: employee

    # --- 2. 邮箱验证码登录配置 (单 Provider, 特殊 Header) ---
    email: 
      # process-url 默认为 /login/email
      principal-param-name: email 
      credential-param-name:
        - captcha
        - role
      # 关键：针对此种登录方式，可单独覆盖客户端区分的 Header, 如果没有携带请求头，默认会走第一个处理逻辑
      request-client-header: X-Request-Client-Email 
      client-types: employee 
      provider-bean-name: 
        - emailLoginService
```

### 2.2 客户端 Provider 路由机制

| **客户端 Header (X-Request-Client)** | **使用的 Provider Bean**                                     |
| ------------------------------------ | ------------------------------------------------------------ |
| `customer`                           | `phoneCustomerLoginService`                                  |
| `employee`                           | `phoneEmployeeLoginService`                                  |
| **缺失/其他**                        | **`phoneCustomerLoginService`** (自动使用 `provider-bean-name` 列表中的**第一个**作为默认) |


## 3. 登录参数简化配置

```yaml
multi-login:
  enabled: true
  methods:
    phone:
      principal-param-name: phone    # process-url 默认为 /login/phone
      credential-param-name: captcha
      provider-bean-name: phoneLoginService
      # 注意: 在此简化配置下，param-name 默认为 principal-param-name + credential-param-name
      # 即：param-name: [phone, captcha]

    email:
      principal-param-name: email    # process-url 默认为 /login/email
      credential-param-name:
        - captcha
        - role
      provider-bean-name: emailLoginService
      # 注意: 在此简化配置下，param-name 默认为 [email, captcha, role]
```





