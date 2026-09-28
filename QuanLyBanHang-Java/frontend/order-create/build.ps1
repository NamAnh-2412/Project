$env:npm_config_cache = "D:\npm-cache"
$env:TEMP = "D:\temp-cursor"
$env:TMP = "D:\temp-cursor"
Set-Location $PSScriptRoot
& ".\node_modules\.bin\vite.cmd" build *> "D:\temp-cursor\vite-build.log"
exit $LASTEXITCODE
