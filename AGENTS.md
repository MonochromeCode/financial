# AI 代码风格约束 · 理财有数后端

适用于本仓库全部目录。更深层 `AGENTS.md` 优先；用户明确要求优先。修改前阅读本文件、相关源码、POM 及 `financial-server/docs/assets.md`。

## 1. 技术与模块边界

- 使用 JDK 25、Maven 3.9+、Spring Boot 4.1.2、Spring Security、MySQL 8、Redis、MyBatis Plus / Mapper XML；版本以当前 POM 为准。
- 包名前缀为 `cn.iocoder.financial`，Maven groupId 沿用现有 `cn.iocoder.boot`；不因二者不同而批量重命名。
- `financial-server` 仅负责启动、配置和模块装配；家庭资产业务归属 `financial-module-assets`，不重新放入旧的 `server.module.assets` 路径。
- `financial-module-system` 管理用户、权限及系统字典，`financial-module-infra` 提供基础设施，`financial-framework` 放跨模块公共能力。
- 新业务模块只有在任务需要独立领域时建立，并同步根 POM 和 server 依赖；不为单个方法增加 Maven 模块。
- 先执行 `git status --short`，保留已有迁移及未跟踪源码。`target/`、`.flattened-pom.xml` 等生成文件不手工修改。

## 2. Java 命名与代码风格

- 使用 UTF-8、4 空格缩进，花括号沿用同目录 Java 风格；不为统一格式重排无关代码。
- 类名使用 PascalCase，方法与字段使用 camelCase，常量使用 UPPER_SNAKE_CASE；命名体现家庭、成员、账户、记录和快照等现有概念。
- 同类职责沿用 `*Controller`、`*Service`、`*Repository`、`*Mapper` 及现有请求对象命名，不机械增加无实际用途的接口/实现层。
- 使用构造器注入，依赖字段优先 `final`；复用现有 Lombok 配置，校验和 Servlet 相关类型使用 `jakarta.*`。
- 新增导入优先显式列出类型，不用通配符；不顺手批量清理遗留通配符。
- 方法保持单一职责，优先提前校验并返回，避免嵌套多层条件、重复校验代码和含义模糊的布尔参数组合。
- 新增代码优先明确的请求/响应或内部值类型；已有资产链路使用 Map 的部分保持字段契约，不在无关任务中整体改写，也不再扩散无约束的 Map 到新业务。
- 不捕获 `Exception` 后忽略或返回成功；沿用项目业务异常和统一异常处理，日志不得输出完整账户、令牌或敏感请求体。
- 代码注释详细明了。

## 3. 分层与接口契约

- `controller/admin`：路由、Jakarta Validation、参数转换及响应封装，不写 SQL 或完整业务流程。
- `controller/admin/vo`：请求字段与格式约束；权限、跨字段及数据库状态校验仍由 Service 完成。
- `service`：家庭权限、业务流程和事务编排；金额聚合复用 `AssetAmounts`，历史还原复用 `AssetAccountSnapshots`，字典及导出分别归 `AssetDictionaryService`、`AssetWorkbook`。
- `dal/AssetRepository`：具名数据访问、租户参数及结果规范化；`dal/mysql/AssetMapper` 对应 Mapper XML，SQL 集中在 `src/main/resources/mapper/assets/AssetMapper.xml`。
- Controller / Service 中不引入 `JdbcTemplate`、拼接 SQL 或通用“执行任意 SQL”方法；不绕过 Repository 的租户封装直接访问资产 Mapper。
- Mapper 使用 `#{...}` 参数绑定；动态排序等标识符必须来自服务端白名单，不把客户端值直接放入 `${...}`。
- 管理端框架自动添加 `/admin-api`，资产 Controller 使用现有 `/finance/assets`；不能重复添加前缀或无意改变接口地址。
- 普通接口沿用 `CommonResult`；金额通过现有十进制字符串转换返回。Excel 等二进制响应沿用独立下载方式及缓存控制。
- 新增或调整接口同步核对前端 `src/api/finance/assets.ts`、字段可空性、时间格式和业务文档。

## 4. 金额与历史规则

- 金额使用 `BigDecimal` 和数据库 `DECIMAL`，从十进制文本构造，禁止从浮点数构造或用 `double` / `float` 计算账本。
- 金额保留两位小数，沿用现有输入精度及最大金额限制；比例计算明确舍入规则，不通过静默舍入修正非法输入。
- 区分空余额和零余额：账户原值可空，汇总时按既有规则计算；历史、响应和 Excel 仍保留“待填写”语义。
- 五类固定计算代码以 `AssetCategory` 为准；显示名称可配置，负债不并入总资产或四笔钱。净资产允许为负，总资产为零时负债率为空。
- 月度比较由 `AssetAmounts.monthlyChanges` 处理，基准为最新快照所在月份之前的最后快照；保留首次记录、无快照及回退标记。
- 历史按业务记账日期还原，同日取最终余额，未更新账户沿用此前余额；保留删除账户和导入记录，不把历史变成当前账户列表的投影。
- 账户数据日期、记账日期和创建时间各有含义，禁止相互替代；日期计算沿用项目时区及 `LocalDate` 语义。
- 字典值及停用项的历史兼容复用 `AssetDictionaryService` 和 `AssetDictType`，不再维护另一套模板或标签常量。

## 5. 权限、事务与数据库

- 当前用户及租户从服务端安全上下文获取，禁止信任请求中的用户、所有者或租户身份。
- 资产查询、计数、更新、删除、历史和导出均限定当前租户与家庭；成员筛选还需校验当前用户的可见范围。
- 权限在 Service 校验；前端按钮状态和客户端筛选不能代替服务端授权，不能通过返回全部数据让前端自行过滤。
- 保留资产提交的事务边界：账户变更、更新批次、历史和每日快照原子写入，任一步骤失败均回滚。
- 保留 `version` 并发校验、`requestId` 去重及数据库约束；不能以先读后写的普通判断替代已有并发保护。
- 导出复用相同权限及筛选口径，包含符合条件的全部历史，不受页面分页限制；隐藏金额时导出也隐藏，超出 Excel 精确数值范围的金额以文本保留。
- 数据库变更在 `script/sql/` 提供可审查的增量脚本，遵循现有 `financial_*` 表命名、租户字段和索引习惯。
- 不直接执行初始化 SQL、清库、重建表或演示数据脚本；已有库升级不得以重新导入全量 `ruoyi-vue-pro.sql` 代替迁移。
- 数据库、Redis 及密钥配置使用环境变量或不入库的本地配置；不提交真实账本和导出内容。

## 6. 验证与交付

以下命令在本仓库根目录执行，确保 Maven 实际使用 JDK 25：

```shell
# 资产模块及其依赖编译
mvn -pl financial-module-assets -am compile
# 涉及 server 装配、依赖或需要完整构建验证
mvn -pl financial-server -am verify
```

- 已有相关测试时运行受影响的测试；新增金额、历史还原或权限逻辑需提供能验证边界的用例或可复现验证步骤，不声称编译通过等于业务验证通过。
- 金额重点覆盖精度、上限、空/零余额及负净资产；权限覆盖跨租户、跨家庭及不可见成员；提交覆盖重复请求、版本冲突和回滚。
- 集成验证使用明确的测试数据和环境，不在真实账本中自动造数；启动或替换现有服务需有明确任务依据。
- 纯文档修改只核对路径、命令和格式，不要求 Maven 构建。依赖、环境或已有问题导致失败时如实报告，不删除校验或测试绕过失败。
- 不自动提交或推送；交付时简述改动、实际验证和仍未验证的内容。
