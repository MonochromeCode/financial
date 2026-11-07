param(
    [string]$Username = 'admin',
    [string]$Password = $env:FINANCIAL_DEMO_PASSWORD,
    [string]$ApiUrl = 'http://localhost:48080/admin-api'
)
$ErrorActionPreference = 'Stop'
$ApiUrl = $ApiUrl.TrimEnd('/')
$baseUrl = $ApiUrl + '/finance/assets'
$headers = @{ 'tenant-id' = '1' }
function Request($method, $path, $data = $null) {
    $arguments = @{ Uri = $baseUrl + $path; Method = $method; Headers = $headers }
    if ($null -ne $data) {
        $arguments.ContentType = 'application/json; charset=utf-8'
        $arguments.Body = ConvertTo-Json -InputObject $data -Depth 12 -Compress
    }
    $response = Invoke-RestMethod @arguments
    if ($response.code -ne 0) { throw "Request failed at ${path}: $($response.msg)" }
    return $response.data
}
if (-not $Password) { throw 'Please provide FINANCIAL_DEMO_PASSWORD or -Password' }
$login = Invoke-RestMethod ($ApiUrl + '/system/auth/login') -Method Post -Headers $headers -ContentType 'application/json' -Body (ConvertTo-Json @{ username = $Username; password = $Password })
if ($login.code -ne 0) { throw 'Login failed' }
$headers.Authorization = 'Bearer ' + $login.data.accessToken
$context = Request Get '/context'
if (-not $context.family -or -not $context.owner) { throw 'An existing family owned by this user is required' }
$existing = @(Request Get '/accounts')
$initial = Request Get '/dashboard'
if ($existing.Count -gt 0 -or $initial.trends.Count -gt 0 -or $initial.records.total -gt 0) {
    throw 'Family already contains data; stopped to avoid overwriting or duplicating it'
}
$required = @('liquid', 'fixed', 'investment', 'receivable', 'liability')
foreach ($code in $required) {
    if (-not ($context.categories | Where-Object { $_.code -eq $code -and $_.enabled })) { throw "Required category disabled: $code" }
}
$memberIds = @{ owner = [long]$context.currentMemberId }
foreach ($member in @(@{ key = 'spouse'; name = '爱人（演示）'; order = 1 }, @{ key = 'child'; name = '孩子（演示）'; order = 2 })) {
    $found = $context.members | Where-Object displayName -eq $member.name
    if (-not $found) {
        Request Put '/member' @{ displayName = $member.name; userId = $null; sortOrder = $member.order } | Out-Null
        $context = Request Get '/context'
        $found = $context.members | Where-Object displayName -eq $member.name
    }
    $memberIds[$member.key] = [long]$found.id
}
$definitions = @(
    @('owner', 'liquid', '工商银行储蓄卡', '68000.25', 'liquid', $null, 'card', '工资与日常备用资金'),
    @('owner', 'liquid', '支付宝余额', '12580.68', 'liquid', $null, 'alipay', '日常支付余额'),
    @('owner', 'liquid', '微信钱包', '2680.50', 'liquid', $null, 'wechat', '日常支付余额'),
    @('owner', 'liquid', '现金', '800.00', 'liquid', $null, 'cash', '家庭备用现金'),
    @('owner', 'fixed', '自住房估值', '1800000.00', 'long_term', $null, $null, '房产当前市场估值示例'),
    @('owner', 'investment', '指数基金组合', '98000.80', 'long_term', $null, $null, '长期持有基金的当前市值'),
    @('owner', 'investment', '银行稳健理财', '60000.00', 'stable', $null, $null, '中短期稳健资金'),
    @('owner', 'investment', '个人养老金账户', '32000.00', 'long_term', $null, $null, '养老目标资金'),
    @('owner', 'liquid', '医疗保障备用金', '6000.00', 'insurance', '医疗', $null, '医疗支出预留现金，不是保险保额'),
    @('owner', 'receivable', '朋友借款', '15000.00', 'stable', $null, $null, '借出款项的剩余应收本金'),
    @('owner', 'liability', '房贷待还本金', '680000.00', $null, $null, $null, '贷款剩余本金，按正数记录负债'),
    @('owner', 'liability', '信用卡待还', '4860.35', $null, $null, $null, '本期信用卡待还金额'),
    @('spouse', 'liquid', '工资卡', '45200.36', 'liquid', $null, $null, '家庭另一位成员的工资账户'),
    @('spouse', 'fixed', '家庭用车估值', '120000.00', 'long_term', $null, $null, '车辆二手市场估值示例'),
    @('spouse', 'investment', '债券基金', '38000.20', 'stable', $null, $null, '低波动理财资产'),
    @('spouse', 'investment', '重疾险现金价值', '8000.00', 'insurance', '重疾', $null, '仅记录现金价值，不将保险保额计入资产'),
    @('spouse', 'receivable', '租房押金', '6000.00', 'stable', $null, $null, '退租时可收回的押金'),
    @('spouse', 'liability', '车贷待还本金', '52000.00', $null, $null, $null, '车辆贷款剩余本金'),
    @('child', 'investment', '教育成长基金', '42000.00', 'long_term', $null, $null, '长期教育目标资金'),
    @('child', 'liquid', '压岁钱储蓄', '12000.00', 'stable', $null, $null, '孩子的储蓄账户'),
    @('child', 'liquid', '意外保障备用金', '2000.00', 'insurance', '意外', $null, '保障用途预留现金，不是保险保额'),
    @('child', 'liquid', '备用零钱账户', '0.00', 'liquid', $null, $null, '明确填写为零的账户示例'),
    @('owner', 'investment', '待估值证券账户', $null, 'long_term', $null, $null, '尚未填写估值的账户示例，区别于零余额')
)
$dates = @('2026-04-30', '2026-05-31', '2026-06-30', '2026-07-31', '2026-08-31', '2026-09-30', '2026-10-15', '2026-10-31', '2026-11-01', '2026-11-03', '2026-11-05', '2026-11-07')
$curves = @{
    liquid = @('0.58', '0.62', '0.73', '0.69', '0.78', '0.85', '0.91', '0.94', '0.95', '0.97', '0.985', '1')
    fixed = @('0.97', '0.975', '0.98', '0.985', '0.99', '0.995', '0.997', '0.998', '0.999', '1', '1', '1')
    investment = @('0.72', '0.76', '0.79', '0.82', '0.80', '0.88', '0.91', '0.94', '0.945', '0.968', '0.98', '1')
    receivable = @('2', '2', '1.6', '1.6', '1.4', '1.3', '1.2', '1.1', '1.1', '1', '1', '1')
    liability = @('1.035', '1.03', '1.025', '1.02', '1.015', '1.01', '1.008', '1.006', '1.005', '1.003', '1.001', '1')
    vehicle = @('1.10', '1.08', '1.06', '1.045', '1.03', '1.02', '1.015', '1.01', '1.008', '1.005', '1.003', '1')
    credit = @('0.6', '1.2', '0.8', '1.4', '0.9', '1.1', '0.65', '1.12', '0.8', '0.95', '1.03', '1')
}
$culture = [Globalization.CultureInfo]::InvariantCulture
$batchIds = [Collections.Generic.List[long]]::new()
for ($point = 0; $point -lt $dates.Count; $point++) {
    $date = $dates[$point]
    $saved = @(Request Get '/accounts')
    $changes = for ($position = 0; $position -lt $definitions.Count; $position++) {
        $item = $definitions[$position]
        $tag = 'household-demo-v1-' + ($position + 1).ToString('00')
        $old = $saved | Where-Object { $_.remark -like "*$tag*" }
        $balance = $null
        if ($null -ne $item[3]) {
            $curve = $item[1]
            if ($item[2] -eq '家庭用车估值') { $curve = 'vehicle' }
            if ($item[2] -eq '信用卡待还') { $curve = 'credit' }
            $factor = if ($item[4] -eq 'insurance') { [decimal]1 } else { [decimal]::Parse($curves[$curve][$point], $culture) }
            $value = [decimal]::Parse($item[3], $culture) * $factor
            $balance = [decimal]::Round($value, 2, [MidpointRounding]::AwayFromZero).ToString('F2', $culture)
        }
        $change = @{
            memberId = $memberIds[$item[0]]; categoryCode = $item[1]; name = $item[2] + '（演示）'
            balance = $balance; fundBucket = $item[4]; insuranceType = $item[5]; templateCode = $item[6]
            dataDate = $date; sortOrder = $position; remark = "演示数据｜$($item[7])｜$tag"; deleted = $false
        }
        if ($old) { $change.id = $old.id; $change.version = $old.version }
        $change
    }
    $batch = Request Post '/submit' @{
        requestId = "household-demo-$($context.family.id)-$($date.Replace('-', ''))"
        updateDate = $date
        accounts = @($changes)
    }
    $batchIds.Add([long]$batch)
    Write-Output "Saved $date / batch $batch / $($changes.Count) accounts"
}
$dashboard = Request Get '/dashboard'
$accounts = @(Request Get '/accounts')
$assetTotal = [decimal]0
$debtTotal = [decimal]0
foreach ($account in $accounts) {
    if ($null -eq $account.balance) { continue }
    if ($account.categoryCode -eq 'liability') { $debtTotal += [decimal]::Parse($account.balance, $culture) }
    else { $assetTotal += [decimal]::Parse($account.balance, $culture) }
}
if ([decimal]::Parse($dashboard.overview.totalAssets, $culture) -ne $assetTotal -or [decimal]::Parse($dashboard.overview.totalLiabilities, $culture) -ne $debtTotal -or [decimal]::Parse($dashboard.overview.netAssets, $culture) -ne ($assetTotal - $debtTotal)) { throw 'Totals do not match account balances' }
if ($dashboard.trends.Count -ne $dates.Count -or $dashboard.records.total -ne ($dates.Count * $definitions.Count)) { throw 'Unexpected snapshot or record count' }
if (($dashboard.trends.date | Sort-Object -Unique).Count -ne $dates.Count) { throw 'Duplicate trend dates found' }
foreach ($memberId in $memberIds.Values) {
    $memberDashboard = Request Get "/dashboard?memberId=$memberId"
    if ($null -eq $memberDashboard.overview) { throw "Member dashboard unavailable: $memberId" }
}
$result = [pscustomobject]@{
    FamilyId = $context.family.id
    Members = (Request Get '/context').members.Count
    Accounts = $accounts.Count
    DailySnapshots = $dashboard.trends.Count
    UpdateRecords = $dashboard.records.total
    TotalAssets = $dashboard.overview.totalAssets
    TotalLiabilities = $dashboard.overview.totalLiabilities
    NetAssets = $dashboard.overview.netAssets
    DebtRatio = $dashboard.overview.debtRatio
    MonthlyAssetsChange = $dashboard.monthly.totalAssets
    MonthlyLiabilitiesChange = $dashboard.monthly.totalLiabilities
    BatchIds = @($batchIds)
}
$result | ConvertTo-Json -Depth 5
