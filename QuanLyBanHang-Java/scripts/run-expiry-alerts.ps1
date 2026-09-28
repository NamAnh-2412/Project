# Goi API canh bao het han kho (tuong duong check_expiry_alerts.php)
param(
    [string]$BaseUrl = "http://localhost:8081/quanlybanhang",
    [string]$SessionCookie = ""
)

if ([string]::IsNullOrWhiteSpace($SessionCookie)) {
    Write-Error "Can JSESSIONID. Dang nhap app, lay cookie tu trinh duyet roi chay:"
    Write-Host '  .\run-expiry-alerts.ps1 -SessionCookie "JSESSIONID=..."'
    exit 1
}

$uri = "$BaseUrl/api/kho/check-expiry-alerts"
$response = Invoke-WebRequest -Uri $uri -Method POST -Headers @{ Cookie = $SessionCookie } -UseBasicParsing
Write-Host $response.Content
