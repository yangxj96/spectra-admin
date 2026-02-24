# 生成 localhost HTTPS 证书（依赖已安装的 CA） 不需要管理员权限

# 保存脚本执行前的当前目录
$OriginalLocation = Get-Location

$WorkDir = "D:\Devops\Platform\LocalHttps"
$CA_Cert = Join-Path $WorkDir "SpectraRootCA.crt"
$CA_Key  = Join-Path $WorkDir "SpectraRootCA.key"

if (-not (Test-Path $WorkDir)) {
    New-Item -ItemType Directory -Path $WorkDir | Out-Null
}

if (!(Test-Path $WorkDir)) {
    Write-Host "❌ 工作目录不存在: $WorkDir" -ForegroundColor Red
    Write-Host "👉 请先运行 install-ca.ps1" -ForegroundColor Yellow
    exit 1
}

if (!(Test-Path $CA_Cert) -or !(Test-Path $CA_Key)) {
    Write-Host "❌ 缺少 CA 文件，请确保 SpectraRootCA.crt 和 SpectraRootCA.key 存在" -ForegroundColor Red
    exit 1
}

# 检查 CA 是否已安装到系统信任库（通过 Thumbprint 匹配）
try {
    $LocalCA = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2($CA_Cert)
} catch {
    Write-Host "❌ 无法读取本地 CA 证书: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$ExpectedThumbprint = $LocalCA.Thumbprint

$InstalledCA = Get-ChildItem -Path Cert:\CurrentUser\Root | Where-Object { $_.Thumbprint -eq $ExpectedThumbprint }

if (-not $InstalledCA) {
    Write-Host "❌ 根 CA 未安装到系统信任库！" -ForegroundColor Red
    Write-Host "💡 期望指纹: $ExpectedThumbprint" -ForegroundColor Yellow
    Write-Host "👉 请先运行 install-ca.ps1" -ForegroundColor Yellow
    exit 1
}

Set-Location $WorkDir

Write-Host "🌐 正在生成 localhost 证书 ..." -ForegroundColor Cyan

# 检查 openssl
if (!(Get-Command openssl -ErrorAction SilentlyContinue)) {
    Write-Host "❌ 未找到 openssl" -ForegroundColor Red
    exit 1
}

# 生成私钥
$KeyFile = "localhost.key"
openssl genrsa -out $KeyFile 2048
if ($LASTEXITCODE -ne 0) { Write-Host "❌ 生成私钥失败" -ForegroundColor Red; exit 1 }

# SAN 配置
$SAN_Config = @"
[req]
default_bits = 2048
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = v3_req

[dn]
CN = localhost

[v3_req]
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
IP.2 = ::1
"@

$SANFile = "san.cnf"
$SAN_Config | Out-File -FilePath $SANFile -Encoding ASCII

# 生成 CSR
$CSRFile = "localhost.csr"
openssl req -new -key $KeyFile -out $CSRFile -config $SANFile
if ($LASTEXITCODE -ne 0) { Write-Host "❌ 生成 CSR 失败" -ForegroundColor Red; exit 1 }

# 签发证书
$CertFile = "localhost.crt"
openssl x509 -req -in $CSRFile `
  -CA $CA_Cert -CAkey $CA_Key -CAcreateserial `
  -out $CertFile -days 365 -sha256 `
  -extfile $SANFile -extensions v3_req
if ($LASTEXITCODE -ne 0) { Write-Host "❌ 签发证书失败" -ForegroundColor Red; exit 1 }

# 生成 .p12（Spring Boot）
$P12File = "keystore.p12"
$Password = "QuVsKppcWvwwX2Vv"
openssl pkcs12 -export -in $CertFile -inkey $KeyFile -out $P12File -name tomcat -password pass:$Password
if ($LASTEXITCODE -ne 0) { Write-Host "❌ 生成 .p12 失败" -ForegroundColor Red; exit 1 }

Write-Host "✅ 证书生成完成！" -ForegroundColor Green
# Write-Host "   📄 localhost.crt" -ForegroundColor White
# Write-Host "   📄 localhost.key" -ForegroundColor White
Write-Host "   📦 keystore.p12 (密码: $Password)" -ForegroundColor White

Write-Host "`n💡 提示: 将 keystore.p12 放入 Spring Boot 的 src/main/resources/" -ForegroundColor Cyan

# 自动清理中间文件
$TempFiles = @($KeyFile, $CSRFile, $CertFile, $SANFile) | Where-Object { $_ -ne $null -and $_ -ne "" }
foreach ($f in $TempFiles) {
    if (Test-Path $f) {
        Remove-Item $f -Force
    }
}
Write-Host "🧹 中间文件已清理" -ForegroundColor Cyan

# 脚本执行完毕后，跳回原来的目录
Set-Location $OriginalLocation

# 打开生成目录
Write-Host "`n📂 打开生成目录: $WorkDir" -ForegroundColor Cyan
Start-Process explorer.exe $WorkDir