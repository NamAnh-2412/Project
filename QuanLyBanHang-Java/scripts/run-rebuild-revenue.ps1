# Goi API rebuild daily_revenue (tuong duong rebuild_daily_revenue.php)
param(
    [string]$BaseUrl = "http://localhost:8081/quanlybanhang",
    [string]$SessionCookie = ""
)

if ([string]::IsNullOrWhiteSpace($SessionCookie)) {
    Write-Error "Can JSESSIONID. Dang nhap app, lay cookie tu trinh duyet roi chay:"
    Write-Host '  .\run-rebuild-revenue.ps1 -SessionCookie "JSESSIONID=..."'
    exit 1
}

$uri = "$BaseUrl/api/rebuild-daily-revenue"
$response = Invoke-WebRequest -Uri $uri -Method POST -Headers @{ Cookie = $SessionCookie } -UseBasicParsing
Write-Host $response.Content
