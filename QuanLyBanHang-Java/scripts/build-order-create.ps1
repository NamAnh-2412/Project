# Build React bundle for order create page
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location (Join-Path $root "frontend\order-create")
if (-not (Test-Path "node_modules")) {
    Write-Host "Running npm install..."
    npm install
}
Write-Host "Running vite build..."
npm run build
$bundle = Join-Path $root "src\main\webapp\assets\react\order-create\order-create.js"
if (Test-Path $bundle) {
    $size = (Get-Item $bundle).Length
    Write-Host "OK: $bundle ($size bytes)"
} else {
    Write-Error "Bundle not found at $bundle"
}
