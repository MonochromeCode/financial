# 理财有数后端

`financial-server` 是后端启动和业务模块的统一入口，依赖 `financial-module-system`、`financial-module-infra` 提供系统管理与基础设施能力。

## 业务模块组织

各业务模块在 `src/main/java/cn/iocoder/financial/server/module` 下按包划分，由 server 统一编译、扫描和打包：

```text
server/
├── YudaoServerApplication.java
├── controller/
└── module/
    └── assets/
        ├── controller/admin/
        │   └── vo/
        ├── service/
        └── dal/
```

新增业务模块时，在 `server.module` 下建立业务包，并在包内划分 Controller、Service、数据访问层。管理端 Controller 放在 `controller.admin` 子包下，框架会添加 `/admin-api` 前缀。

当前业务模块：[家庭资产记账](docs/assets.md)。

## 构建与启动

使用 JDK 25，在项目根目录执行：

```shell
mvn -pl financial-server -am clean verify
java -jar financial-server/target/financial-server.jar
```

启动前配置 MySQL、Redis，并按照各业务模块文档执行数据库初始化脚本。
