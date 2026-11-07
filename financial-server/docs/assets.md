# 家庭资产记账

这是家庭资产负债表业务模块，不包含日常消费流水。业务位于独立模块 `financial-module-assets`，包名为 `cn.iocoder.financial.module.assets`，由 `financial-server` 聚合启动和打包；前端位于 Vben 工程 `apps/web-antd/src/views/finance`。Service 不包含 SQL，读写经 Repository 注入当前租户后调用 Mapper XML；权限仍由业务层检查家庭、成员范围，写入继续使用原事务和家庭行锁。

## 部署

1. 新库在业务数据库执行 `script/sql/household-assets.sql`，创建带表、字段注释的七张 `financial_*` 表。已有库补充注释时执行 `script/sql/household-assets-comments.sql`；`CREATE TABLE IF NOT EXISTS` 不会更新已有表的注释。两个脚本均可重复执行，不删除已有数据。
   新库和已有库均执行 `script/sql/household-assets-dictionaries.sql` 初始化资产字典；脚本仅补齐缺少的类型和条目，不覆盖已有配置。
2. 使用 JDK 25 构建：`mvn -pl financial-server -am package`，然后重新启动服务。
3. 启动 Vben 的 `apps/web-antd`。入口为 `/dashboard/workspace`，编辑页为 `/finance/assets/edit`，管理页为 `/finance/assets/manage`。
4. 首次使用点击“建立家庭账本”。所有者可以创建成员，并关联同租户下已启用的系统用户编号。一个系统用户只关联一个家庭成员。默认普通成员仅能查看、编辑本人资产。

单账户金额上限由 `financial.assets.max-account-balance` 配置，默认 `999999999999.99` 元。也可以设置环境变量 `FINANCIAL_ASSETS_MAX_ACCOUNT_BALANCE`。金额在数据库使用 DECIMAL、服务端使用 BigDecimal；响应中的金额使用十进制字符串，前端即时汇总使用整数分。

## 字典配置

业务职责划分：`AssetFamilyService` 管理账本、成员和家庭分类，`AssetAccessService` 统一家庭范围、成员可见与可编辑权限，`AssetService` 处理资产查询与记账事务，`AssetAmounts` 处理金额与月度比较，`AssetAccountSnapshots` 还原历史状态，`AssetWorkbook` 生成导出，`AssetDictionaryService` 统一字典配置和选项校验。

在系统管理的数据字典中维护以下类型，前端通过 `context.options` 使用同一份配置，Excel 导出也读取字典标签。

| 字典类型 | 用途 |
| --- | --- |
| `financial_asset_category` | 新账本的五类资产默认名称 |
| `financial_account_template` | 银行卡、现金、支付宝等账户模板 |
| `financial_fund_bucket` | 资金用途；`css_class` 为颜色，备注为期限说明 |
| `financial_insurance_type` | 保险类型 |
| `financial_record_operation` | 新增、修改、删除的显示标签 |
| `financial_record_source` | 手动、导入来源的显示标签 |

停用的类型或条目不能被新选用；已有账户可以保留原值，历史及导出仍展示字典标签，已删除字典值回退显示原编码。家庭自定义分类名称、颜色和说明继续保存在 `financial_category`，不受系统字典改名覆盖。五类资产编码、负债扣减及保险专用校验属于固定业务语义，不能通过添加字典条目改变；操作及来源由服务端写入。新增资金用途、保险类型的编码须符合数据库字段长度（分别为 16、20 个字符）。

## 数据库字段维护

初始化脚本 `script/sql/household-assets.sql` 是家庭资产模块的完整建表定义，包含七张表、88 个字段及其中文注释。`script/sql/household-assets-comments.sql` 用于为相同字段定义的已有库补齐注释；MySQL 的 `MODIFY COLUMN` 必须包含完整字段定义，因此该脚本保留初始化脚本中的类型、长度、精度、默认值、可空性、自增及时间自动更新规则，不增删字段、索引或业务数据。执行前应确认已有库的字段定义与初始化脚本一致。

| 表 | 用途 | 关键约束 |
| --- | --- | --- |
| `financial_family` | 家庭账本与可见权限 | 同租户所有者唯一；允许查看其他成员不代表允许编辑 |
| `financial_member` | 家庭成员与登录用户关联 | 同租户系统用户唯一；`user_id=NULL` 表示未关联登录用户 |
| `financial_category` | 家庭五类资产配置 | 同租户、家庭的分类编码唯一 |
| `financial_account` | 账户当前状态 | `version` 用于并发校验；`deleted` 为逻辑删除标记 |
| `financial_update_batch` | 一次提交与汇总结果 | 同租户、家庭的 `request_id` 唯一；`request_hash` 校验提交内容 |
| `financial_update_record` | 每次账户变动明细 | 保存操作时的账户名称、前后余额及来源；删除账户不删除历史 |
| `financial_snapshot` | 家庭及成员每日快照 | 同租户、家庭、成员和日期唯一；`member_id=0` 表示家庭汇总 |

字段口径：

- 金额单位均为元。账户余额、前后余额为 `DECIMAL(18,2)`，单次变化为 `DECIMAL(19,2)`，汇总及快照为 `DECIMAL(24,2)`。余额 `NULL` 表示待填写，`0.00` 表示明确为零；计算变化时 `NULL` 按零处理。负债以正数保存，净资产允许为负数。
- `data_date` 是账户数据日期，`update_date` / `snapshot_date` 是记账日期，`created_at` 是操作时间，三者分别维护。月度比较按快照记账日期计算，不按导入或手动操作时间计算。
- 分类编码固定为 `liquid`、`fixed`、`investment`、`receivable`、`liability`；分类名称可以修改。系统账户模板为 `bank`、`card`、`cash`、`alipay`、`wechat`、`ecny`，自定义账户的 `template_code` 为 `NULL`。
- `fund_bucket` 为 `liquid`、`stable`、`long_term`、`insurance` 或 `NULL`；负债不分配资金用途。`insurance_type` 仅用于保险用途账户，可取意外、医疗、重疾、寿险、其他。
- 账户和明细的 `source_type`、批次的 `source` 默认值均为 `manual`；`import` 表示导入来源，来源由服务端维护。币种 `currency` 当前仅使用 `CNY`。
- `monthly.latestDate`、`monthly.baselineDate`、`monthly.fallback`、`monthly.firstRecord` 及月度变化金额为接口计算字段，无需新增数据库列。最近日期取可见成员的最新快照；基准取该快照所在月份之前的最后一次快照；最近日期早于本月时启用回退，展示最近有记录月份的变化。

后续调整字段时，同时更新初始化脚本、单独的已有库升级脚本、服务端读写与校验、前端接口类型及本节口径，避免只修改建表脚本而遗漏已有数据库。

## 演示数据

启动后端并建立一个空家庭账本后，在后端工程根目录使用 PowerShell 7 执行：

```powershell
$env:FINANCIAL_DEMO_PASSWORD = Read-Host '请输入账本所有者的登录密码' -MaskInput
./script/seed-household-demo.ps1 -Username admin
Remove-Item Env:FINANCIAL_DEMO_PASSWORD
```

脚本通过正式接口写入 3 位成员、23 个账户、2026 年 4 月至 11 月共 12 天的快照和 276 条更新记录。所有新增账户均标注“演示”；账本已有账户、快照或更新记录时会停止，避免重复或覆盖。历史日期截至 2026-11-07，运行环境日期须不早于该日。可通过 `-ApiUrl` 指定后端地址，默认 `http://localhost:48080/admin-api`。

## 行为

- 五类资产可配置名称、图标、颜色、说明、顺序和启用状态；存在账户的分类不能停用。
- 全部修改在一次事务内写入账户、批次、逐账户记录、家庭及成员每日快照。`member_id=0` 表示家庭汇总，避免空值唯一键产生重复日期。普通成员查询使用有权限的成员快照聚合，避免泄露家庭总额。
- 同一家庭写入时锁定家庭行；账户版本校验防止覆盖并发修改。重复的请求编号和相同内容返回已有批次，不重复入账；不同内容不能复用编号。
- 同日快照覆盖为最后结果，账户明细记录逐次追加。记账日期不能早于家庭最近快照日期，避免将当前状态写入过去。单账户数据日期可以更早，不能晚于今天。
- 月度变化使用最近一次快照，对比该快照所在月份之前的最后一次快照，支持跨月断档。本月没有记录时展示最近有记录月份的资产、负债及净资产变化，标题明确标注年月（例如“2026年10月资产变化”），并注明本月暂无记录、截至日期及对比日期；没有历史基准时标记首次记录，没有任何快照时显示暂无资产记录。同日快照仅保留最后结果。排除固定资产同步影响总览、月度变化、组成、四笔钱及趋势。
- “查看记录金额”弹窗展示全部历史记账日期，以具体账户为列，同时展示总资产、总负债和净资产。同日取最后状态，未修改账户沿用上次余额；不存在、已删除或不在筛选范围内的账户显示“—”，空余额显示“待填写”。同名账户以独立编号区分，表头补充成员和分类；支持横纵滚动、固定表头和日期列，遵循隐私及成员、固定资产筛选。
- 空余额保存为 NULL，明确的零保存为 0.00。负债输入正数，可以产生负净资产。账户逻辑删除，历史继续保留。
- 浏览器草稿按租户、用户、家庭隔离；提交失败保留输入，仅成功提交或主动放弃时清理。隐私状态仅保存在本机，并隐藏金额、历史详情和图表提示。
- 真实导入、外部账户绑定及自动同步不在此版本启用，相关入口隐藏。页面不会显示虚构的同步成功状态。
- 成员有关联账户、记录或快照时不可删除，可以改名或解除普通成员的用户关联。所有者不可解除自己的用户关联。

## 接口

全部位于 `/admin-api/finance/assets`，需要已有系统登录令牌及租户头。家庭范围从当前用户成员关系推导，不接受客户端指定其他家庭。

| 方法 | 路径 | 功能 |
| --- | --- | --- |
| GET | `/context` | 当前家庭、可见成员、分类、金额上限和字典 `options`（categories/templates/buckets/insuranceTypes/operations/sources） |
| POST | `/initialize` | 建立家庭账本 |
| PUT | `/family` | 所有者配置家庭及成员查看权限 |
| PUT / DELETE | `/member` | 所有者管理成员 |
| PUT | `/category` | 所有者配置预置分类 |
| GET | `/accounts` | 可见账户，支持成员、分类筛选 |
| GET | `/account-snapshots` | 全部历史记账日期的逐账户余额及汇总，支持 `memberId`、`excludeFixed`；按历史记录还原每日最后状态并延续未变账户 |
| GET | `/dashboard` | 总览、可见成员的真实账户明细（`accounts`）、月变动、组成、四笔钱、趋势和最近记录 |
| GET | `/export-excel` | 导出 `.xlsx`，包含资产汇总、当前账户明细、全部历史账户明细，支持 `memberId`、`excludeFixed`、`hidden` |
| GET | `/trends` | 支持成员、起止日期、排除固定资产；前端选择分类及净资产口径 |
| GET | `/history` | 分页明细，支持成员、分类、账户数据日期筛选 |
| POST | `/submit` | 原子保存新增、编辑、排序及删除账户 |

`submit` 请求包含 `requestId`、`updateDate`、`accounts`。已有账户必须携带 `id`、`version`；新账户省略编号；删除使用 `deleted=true`。其余账户字段与 `/accounts` 返回一致。只有修改的账户需要提交，不会覆盖未提交的账户。

`export-excel` 在同一事务内读取当前可见账户并计算汇总，复用家庭及成员权限校验；排除固定资产同时作用于汇总、当前账户和历史明细。历史明细直接读取全部可见更新记录，按操作时间、记录编号升序导出，不受页面分页和趋势日期限制，包含已删除账户的记录及导入记录，并保留操作时账户名称、前后余额、变动金额、数据日期、操作时间、操作类型、来源和操作人；超过单个工作表行数上限时自动续表。金额单位为人民币元，空余额写为“待填写”，零余额写为数值零；超过 Excel 15 位有效数字的金额以文本保留精度。`hidden=true` 时汇总、账户余额、历史前后余额、变动金额及负债率写为 `****`，其余字段保留。下载响应设置 `Cache-Control: no-store`。
