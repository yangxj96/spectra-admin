# 生成并安装根 CA 证书（一次即可） 需管理员权限

# 保存脚本执行前的当前目录
$OriginalLocation = Get-Location

$WorkDir = Join-Path $env:USERPROFILE "dev-https"
$CA_Name = "Development CA"
$Org = "Development"
$Country = "CN"
$State = "Kunming"
$City = "Kunming"
$ValidDays = 3650  # 10年

# ==================== 检查管理员权限 ====================
try {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($identity)
    $isAdmin = $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
} catch {
    Write-Host "❌ 无法检测权限: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "💡 请确保你的系统支持 .NET Framework 并以管理员身份运行" -ForegroundColor Yellow
    exit 1
}

if (-not $isAdmin) {
    Write-Host "❌ 必须以管理员身份运行此脚本！" -ForegroundColor Red
    Write-Host "👉 右键脚本 → '以管理员身份运行'" -ForegroundColor Yellow
    exit 1
}

New-Item -ItemType Directory -Path $WorkDir -Force | Out-Null
Set-Location $WorkDir

Write-Host "🔐 正在生成根 CA: $CA_Name ..." -ForegroundColor Cyan

# 检查 openssl
if (!(Get-Command openssl -ErrorAction SilentlyContinue)) {
    Write-Host "❌ 未找到 openssl，请安装并加入 PATH" -ForegroundColor Red
    exit 1
}

# 生成 CA 私钥和证书
openssl genrsa -out rootCA.key 2048
if ($LASTEXITCODE -ne 0) { Write-Host "❌ 生成 CA 私钥失败" -ForegroundColor Red; exit 1 }

openssl req -x509 -new -nodes -key rootCA.key `
  -sha256 -days $ValidDays `
  -subj "/C=$Country/ST=$State/L=$City/O=$Org/CN=$CA_Name" `
  -out rootCA.crt
if ($LASTEXITCODE -ne 0) { Write-Host "❌ 生成 CA 证书失败" -ForegroundColor Red; exit 1 }

# 安装到信任库
Write-Host "🛡️ 正在安装 CA 到 '受信任的根证书颁发机构' ..." -ForegroundColor Cyan
Import-Certificate -FilePath "$WorkDir\rootCA.crt" -CertStoreLocation Cert:\CurrentUser\Root | Out-Null

Write-Host "✅ 根 CA 已生成并安装！" -ForegroundColor Green
Write-Host "   📄 $WorkDir\rootCA.crt" -ForegroundColor Yellow
Write-Host "   💡 可将此文件分发给团队成员用于信任" -ForegroundColor Cyan

# 脚本执行完毕后，跳回原来的目录
Set-Location $OriginalLocation