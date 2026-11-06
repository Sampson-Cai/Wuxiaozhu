<#
  仓小助接口试跑脚本
  用法：先启动服务，再执行
        powershell -ExecutionPolicy Bypass -File scripts\试跑接口.ps1
  可选参数：-Base http://127.0.0.1:18080 -User admin -Pwd admin123
#>
param(
    [string]$Base = "http://127.0.0.1:18080",
    [string]$User = "admin",
    [string]$Pwd  = "admin123"
)

$ErrorActionPreference = 'Stop'

function Read-Utf8Json($response) {
    $bytes = $response.RawContentStream.ToArray()
    return [Text.Encoding]::UTF8.GetString($bytes) | ConvertFrom-Json
}

function Invoke-Api($method, $path, $headers, $body) {
    $args = @{ Method = $method; Uri = "$Base$path"; UseBasicParsing = $true }
    if ($headers) { $args.Headers = $headers }
    if ($body) { $args.Body = $body }
    $resp = Invoke-WebRequest @args
    return Read-Utf8Json $resp
}

Write-Host "== 1. 登录" -ForegroundColor Cyan
try {
    $login = Invoke-Api 'Post' '/login' $null @{ username = $User; password = $Pwd }
}
catch {
    Write-Host ""
    Write-Host ("连不上 {0}" -f $Base) -ForegroundColor Red
    Write-Host "原因：服务没有启动，或端口不是 18080。" -ForegroundColor Yellow
    Write-Host "处理：先双击 scripts\启动服务.bat，等窗口出现 Started RuoYiApplication 后再运行本脚本。" -ForegroundColor Yellow
    exit 1
}
Write-Host ("   code={0}  token={1}..." -f $login.code, $login.token.Substring(0, 12))

$h = @{ Authorization = "Bearer $($login.token)" }

Write-Host "== 2. 当前用户" -ForegroundColor Cyan
$info = Invoke-Api 'Get' '/getInfo' $h
Write-Host ("   用户={0}  角色={1}  权限={2}" -f $info.user.userName, ($info.roles -join ','), ($info.permissions -join ','))

Write-Host "== 3. 首页预警" -ForegroundColor Cyan
$summary = Invoke-Api 'Get' '/business/dashboard/summary' $h
Write-Host ("   库存预警={0}  待审批={1}  七天内到期应付={2}  逾期={3}  未付总额={4}" -f `
    $summary.lowStock, $summary.pendingApproval, $summary.payableDue, $summary.payableOverdue, $summary.unpaidAmount)

Write-Host "== 4. 商品列表" -ForegroundColor Cyan
$products = Invoke-Api 'Get' '/business/product/list?pageNum=1&pageSize=10' $h
Write-Host ("   共 {0} 条" -f $products.total)
foreach ($p in $products.rows) {
    Write-Host ("   - {0}  {1}  库存={2}" -f $p.productCode, $p.productName, $p.stockQty)
}

Write-Host "== 5. 采购单" -ForegroundColor Cyan
$orders = Invoke-Api 'Get' '/business/purchase/list?pageNum=1&pageSize=10' $h
Write-Host ("   共 {0} 条" -f $orders.total)
foreach ($o in $orders.rows) {
    Write-Host ("   - {0}  状态={1}  金额={2}" -f $o.orderNo, $o.status, $o.totalAmount)
}

Write-Host "== 6. 应付账款" -ForegroundColor Cyan
$payables = Invoke-Api 'Get' '/business/payable/list?pageNum=1&pageSize=10' $h
Write-Host ("   共 {0} 条" -f $payables.total)
foreach ($y in $payables.rows) {
    Write-Host ("   - {0}  应付={1}  未付={2}  到期状态={3}" -f $y.payableNo, $y.totalAmount, $y.remainingAmount, $y.dueStatus)
}

Write-Host "== 7. 不带令牌访问（应被拒绝）" -ForegroundColor Cyan
$anon = Invoke-Api 'Get' '/business/product/list'
if ($anon.code -eq 401) {
    Write-Host ("   已拒绝，code={0}" -f $anon.code) -ForegroundColor Green
}
else {
    Write-Host ("   异常：返回 code={0}" -f $anon.code) -ForegroundColor Red
}

Write-Host "`n完成。以上数据来自本机数据库 cangxiaozhu_dev。" -ForegroundColor Green
