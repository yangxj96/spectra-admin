# 卸载根 CA 证书 需管理员权限

# 保存脚本执行前的当前目录
$OriginalLocation = Get-Location

$CA_Name = "Spectra CA"
$WorkDir = Join-Path $env:USERPROFILE "dev-https"

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

Write-Host "🗑️ 正在卸载根 CA: $CA_Name ..." -ForegroundColor Yellow

# ✅ 使用 -like 模糊匹配 Subject，支持完整 DN
$Cert = Get-ChildItem -Path Cert:\CurrentUser\Root | Where-Object {
    $_.Subject -like "CN=$CA_Name" -or
    $_.Subject -like "CN=$CA_Name,*" -or
    $_.Subject -like "*,CN=$CA_Name" -or
    $_.Subject -like "*,CN=$CA_Name,*"
}

if ($Cert) {
    $Thumbprint = $Cert.Thumbprint
    Remove-Item -Path "Cert:\CurrentUser\Root\$Thumbprint" -Force
    Write-Host "✅ CA 已卸载: $Thumbprint" -ForegroundColor Green
} else {
    Write-Host "🔍 未找到主题包含 'CN=$CA_Name' 的 CA 证书" -ForegroundColor Red
}

# 可选：删除工作目录中的文件
$Confirm = Read-Host "是否删除 $WorkDir 中的证书文件？(y/N)"
if ($Confirm -match "^[Yy]") {
    Remove-Item -Path $WorkDir -Recurse -Force
    Write-Host "📁 $WorkDir 已删除" -ForegroundColor Green
}

# 脚本执行完毕后，跳回原来的目录
Set-Location $OriginalLocation