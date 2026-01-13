param(
    [string]$tomlFile = "../../spectra-admin/.mise.local.toml"
)

# 检查文件是否存在
if (-Not (Test-Path $tomlFile)) {
    Write-Error "TOML 文件不存在: $tomlFile"
    exit 1
}

# 输出文件路径：与 TOML 文件同一目录
$outputFile = Join-Path (Split-Path $tomlFile) ".env"

# 清空旧文件，使用 UTF8
Set-Content -Path $outputFile -Value "" -Encoding UTF8

$inEnvSection = $false

# 逐行读取 TOML 文件
Get-Content $tomlFile | ForEach-Object {
    $line = $_.Trim()

    # 跳过空行或注释
    if ($line -eq "" -or $line.StartsWith("#")) { return }

    # 检测 [env] 段落开始
    if ($line -match '^\[env\]') {
        $inEnvSection = $true
        return
    }

    # 如果遇到新的段落，退出 env 段
    if ($line -match '^\[.*\]' -and $inEnvSection) {
        $inEnvSection = $false
        return
    }

    # 只处理 env 段落内的 key = "value"
    if ($inEnvSection -and $line -match '^\s*([A-Z0-9_]+)\s*=\s*"(.*)"') {
        $key = $matches[1]
        $value = $matches[2]

        # 写入 .env 文件，UTF8 编码，保持完整字符串
        Add-Content -Path $outputFile -Value "$key=$value" -Encoding UTF8
    }
}

Write-Output ".env 文件已生成: $outputFile"
