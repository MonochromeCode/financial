# AI 代码风格约束 · 理财有数后端

适用于本仓库全部目录。更深层 `AGENTS.md` 优先；用户明确要求优先。修改前阅读本文件、相关源码、POM 及 `financial-server/docs/assets.md`。

## 代码模板：financial-module-system

新增和调整代码以 `financial-module-system` 为风格模板，标准 CRUD 优先参考 `notice`，复杂业务参考 `user`，不要以当前资产模块的简化实现定义新规范。

以下路径均相对于 `financial-module-system/src/main/java/cn/iocoder/financial/module/system/`：

| 职责 | 参考文件 |
| --- | --- |
| Controller、路由、权限及响应 | `controller/admin/notice/NoticeController.java` |
| Service 接口及实现 | `service/notice/NoticeService.java`、`service/notice/NoticeServiceImpl.java` |
| 独立请求与响应 VO | `controller/admin/notice/vo/NoticeSaveReqVO.java`、`NoticePageReqVO.java`、`NoticeRespVO.java` |
| 实体与持久化查询 | `dal/dataobject/notice/NoticeDO.java`、`dal/mysql/notice/NoticeMapper.java` |
| 租户实体与复杂业务 | `dal/dataobject/user/AdminUserDO.java`、`service/user/AdminUserServiceImpl.java` |
| 复杂对象转换及错误码 | `convert/user/UserConvert.java`、`enums/ErrorCodeConstants.java` |

新增业务按领域子包组织：`controller/admin/<领域>/vo`、`service/<领域>`、`dal/dataobject/<领域>`、`dal/mysql/<领域>`；按需要使用 `convert/<领域>` 和 `enums`。

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
- 标准业务使用 `XxxController`、`XxxService` 接口、`XxxServiceImpl` 实现、`XxxMapper` 和 `XxxDO`；请求与响应分别命名为 `XxxSaveReqVO`、`XxxPageReqVO`、`XxxRespVO`，不将所有请求类堆在一个嵌套类容器里。
- Spring 依赖注入沿用 `system` 的 `@Resource` 字段注入；Service 实现标注 `@Service` 并实现接口方法，不另外强制构造器注入风格。
- 复用现有 Lombok 配置；VO / DO 按需使用 `@Data`、`@EqualsAndHashCode(callSuper = true)` 等注解，校验、资源注入和 Servlet 类型使用 `jakarta.*`。
- 导入分组与静态导入沿用对应 `system` 文件；工具方法、错误码可静态导入，不为格式偏好批量整理无关代码。
- 方法保持单一职责，优先提前校验并返回，避免嵌套多层条件、重复校验代码和含义模糊的布尔参数组合。
- 新增业务使用明确的 VO、DO 和必要的内部值类型，不以 `Map<String, Object>` 或 `CommonResult<?>` 代替确定的数据结构；已有资产链路的 Map 在相关重构时逐步替换并保持字段契约。
- 业务异常沿用 `throw exception(ERROR_CODE)`，在所属模块的 `enums/ErrorCodeConstants` 定义有业务含义的错误码；不在新功能中散落 HTTP 状态码和临时错误文本。
- 不捕获 `Exception` 后忽略或返回成功；沿用统一异常处理，日志不得输出完整账户、令牌或敏感请求体。
- 代码注释详细明了，参考 `system` 的中文 JavaDoc：类说明职责，Service 接口说明行为、参数和返回值，DO 字段说明业务含义及关联枚举；复杂流程说明关键校验、步骤和原因，不复制其他模块的作者或无关描述。

## 3. 分层与接口契约

- `controller/admin/<领域>`：沿用 `@Tag`、`@RestController`、`@RequestMapping`、`@Validated`，方法声明 `@Operation`，请求使用 `@Valid` / `@Validated`；只处理参数、Service 调用和响应转换。
- 新增标准 CRUD 路由参考 `system` 的 `/create`、`/update`、`/delete`、`/delete-list`、`/get`、`/page`、`/export-excel`，按实际功能提供，不为了凑齐路由添加无需求接口；现有 `/finance/assets` 契约保持兼容。
- 菜单及操作权限沿用 `@PreAuthorize("@ss.hasPermission('模块:资源:操作')")`，权限标识与菜单配置、前端操作一致；家庭成员数据权限仍由 Service 独立校验。
- `controller/admin/<领域>/vo`：每种请求/响应使用独立文件，以 `@Schema` 说明字段、示例和必填性，以 Jakarta Validation 校验输入；分页请求继承 `PageParam`，分页响应使用 `PageResult<XxxRespVO>`。
- DO 位于 `dal/dataobject/<领域>`，使用 `@TableName` 映射真实表；公共审计字段继承现有 `BaseDO`，需要显式租户字段时参考 `AdminUserDO` 使用 `TenantBaseDO`，不能仅为复用字段改变表结构或租户语义。
- `XxxService` 定义业务方法，`XxxServiceImpl` 实现校验与编排；更新/删除前通过 `validateXxxExists` 等具名方法校验，复杂流程分步骤组织。
- `service`：家庭权限、业务流程和事务编排；金额聚合复用 `AssetAmounts`，历史还原复用 `AssetAccountSnapshots`，字典及导出分别归 `AssetDictionaryService`、`AssetWorkbook`。
- 新增标准持久化使用 `@Mapper` 和 `BaseMapperX<XxxDO>`，ServiceImpl 直接调用 Mapper；条件查询参考 `LambdaQueryWrapperX` 的 `eqIfPresent`、`likeIfPresent` 等方法，使用实体方法引用，不额外强制增加 Repository 层。
- 多表关联或复杂 SQL 按需放入对应 Mapper XML，不能把简单 CRUD 全部手写为 XML；Controller / Service 不引入 `JdbcTemplate`、拼接 SQL 或通用“执行任意 SQL”方法。
- 已有 `AssetRepository` 承担显式租户参数及结果规范化，维护当前资产链路时继续经它调用 Mapper；若将该链路迁移为 `system` 风格，必须同时迁移类型、租户/家庭约束及验证，不只删除 Repository。
- 普通对象转换沿用 `BeanUtils.toBean`，复杂关联组装参考 `UserConvert`，避免在 Controller 手工拼装大段 Map；不为简单字段复制新建转换框架。
- Mapper 使用 `#{...}` 参数绑定；动态排序等标识符必须来自服务端白名单，不把客户端值直接放入 `${...}`。
- 管理端框架自动添加 `/admin-api`，资产 Controller 使用现有 `/finance/assets`；不能重复添加前缀或无意改变接口地址。
- 普通接口使用明确泛型的 `CommonResult<T>` 和 `success(...)`；新增返回值参考 `system` 的创建返回 ID、修改/删除返回布尔值、查询返回 RespVO。金额通过现有十进制字符串转换返回，不能因转为 VO 而丢失精度；Excel 等二进制响应沿用独立下载方式及缓存控制。
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
