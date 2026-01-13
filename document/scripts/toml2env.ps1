<#
.SYNOPSIS
    将 TOML 文件中的 [env] 段转换为 .env 文件
.DESCRIPTION
    - 只处理 [env] 段
    - 自动为包含空格或特殊字符的值加双引号
    - 空字符串写成 KEY=""
    - UTF-8 输出
    - 生成完成后自动打开目录
.PARAMETER tomlFile
    TOML 文件路径，默认 "../../spectra-admin/.mise.local.toml"
#>

param(
    [string]$tomlFile = "../../spectra-admin/.mise.local.toml"
)

# 检查 TOML 文件是否存在
if (-Not (Test-Path $tomlFile)) {
    Write-Error "TOML 文件不存在: $tomlFile"
    exit 1
}

# 输出 .env 文件路径（与 TOML 文件同目录）
$outputFile = Join-Path (Split-Path $tomlFile) ".env"

# 清空旧文件，使用 UTF-8 编码
Set-Content -Path $outputFile -Value "" -Encoding UTF8

$inEnvSection = $false

# 函数：判断值是否需要双引号
function NeedsQuotes($val) {
    # 空字符串需要双引号
    if ([string]::IsNullOrEmpty($val)) { return $true }

    # 包含空格或特殊字符 (# $ ! ` " ' =) 需要双引号
    return $val -match '[\s#$!`"''=]'
}

# 逐行读取 TOML 文件
Get-Content $tomlFile | ForEach-Object {
    $line = $_.Trim()

    # 跳过空行或注释
    if ($line -eq "" -or $line.StartsWith("#")) { return }

    # 检测 [env] 段落开始
    if ($line -match "^\[env\]") {
        $inEnvSection = $true
        return
    }

    # 遇到新的段落，退出 env 段
    if ($line -match "^\[.*\]" -and $inEnvSection) {
        $inEnvSection = $false
        return
    }

    # 只处理 env 段落内的 key = "value"
    if ($inEnvSection -and $line -match '^\s*([A-Z0-9_]+)\s*=\s*"(.*)"') {
        $key = $matches[1]
        $value = $matches[2]

        # 判断是否需要加双引号
        if (NeedsQuotes $value) {
            $value = "`"$value`""  # PowerShell 转义双引号
        }

        # 写入 .env 文件，UTF-8 编码
        Add-Content -Path $outputFile -Value "$key=$value" -Encoding UTF8
    }
}

Write-Host ".env 文件已生成: $outputFile" -ForegroundColor Green

# 打开生成目录
$outputDir = Split-Path $outputFile
Write-Host "📂 打开生成目录: $outputDir" -ForegroundColor Cyan
Start-Process explorer.exe $outputDir
