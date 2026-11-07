# 理财有数后端

`financial-server` 是后端启动和装配入口，依赖 `financial-module-assets`、`financial-module-system`、`financial-module-infra` 提供资产、系统管理与基础设施能力。

## 业务模块组织

业务实现放在独立 Maven 模块中，server 聚合打包：

```text
financial-server/                  启动、配置、装配
financial-module-assets/
├── src/main/java/cn/iocoder/financial/module/assets/
│   ├── controller/admin/          接口和请求对象
│   ├── service/                   业务校验、计算、字典与导出
│   ├── enums/                     固定计算口径、字典类型
│   └── dal/                       Repository 与 Mapper 接口
└── src/main/resources/mapper/assets/AssetMapper.xml
```

新增业务模块时，建立 `financial-module-<业务名>`，在根 POM 注册并由 server 依赖。Controller、Service 中不编写 SQL，数据访问通过具名 Repository / Mapper 方法完成；SQL 置于 Mapper XML，使用参数绑定。管理端 Controller 放在 `controller.admin` 子包下，框架会添加 `/admin-api` 前缀。

当前业务模块：[家庭资产记账](docs/assets.md)。

## 构建与启动

使用 JDK 25，在项目根目录执行：

```shell
mvn -pl financial-server -am clean verify
java -jar financial-server/target/financial-server.jar
```

启动前配置 MySQL、Redis，并按照各业务模块文档执行数据库初始化脚本。
