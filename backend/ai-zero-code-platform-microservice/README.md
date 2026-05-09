# ai-zero-code-platform-microservice

当前目录是从单体后端隔离出的微服务父模块，单体项目源码不参与修改。

## 模块

- `ai-zero-code-platform-common`: 公共模块，存放通用响应、异常、注解、常量、枚举、DTO、VO、Entity 和 Dubbo RPC 契约
- `ai-zero-code-platform-user-service`: 用户服务，HTTP 端口 `8124`，Dubbo 端口 `20881`
- `ai-zero-code-platform-app-service`: 应用、对话历史、精选申请、代码生成服务，HTTP 端口 `8125`，Dubbo 端口 `20882`
- `higress`: Higress 路由示例，按 `/api/user`、`/api/app` 等路径转发到对应服务

## 职责边界

- 用户服务只保留用户控制器、用户 Service、用户 Mapper，以及用户 Dubbo Provider。
- 应用服务保留应用、对话历史、精选申请、静态预览、AI 代码生成等能力。
- 应用服务不再包含用户 Controller、用户 Mapper、用户业务实现；需要用户信息时通过 `UserDubboService` 调用用户服务。
- 公共模块只承载跨服务共享的类型和 RPC 接口，避免两个业务服务重复维护同一份模型代码。

## 基础设施

- Nacos: `NACOS_SERVER_ADDR`，默认 `127.0.0.1:8848`
- Dubbo: 使用 Nacos 作为注册中心
- Higress: 使用 `higress/ai-zero-code-platform-routes.yaml` 将外部流量路由到微服务

## 启动

```bash
mvn -pl ai-zero-code-platform-user-service spring-boot:run
mvn -pl ai-zero-code-platform-app-service spring-boot:run
```

本地直接访问时：

- 用户服务: `http://localhost:8124/api`
- 应用服务: `http://localhost:8125/api`

通过 Higress 暴露后，对外仍保持单体项目原有 `/api/**` 访问风格。
