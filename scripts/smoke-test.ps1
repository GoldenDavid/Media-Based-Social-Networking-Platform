# Smoke test for the Media Social Platform Docker stack (Windows / PowerShell 5.1+).
# Brings up the full stack, polls /actuator/health on every service,
# exercises a few public endpoints, then tears everything down.
#
# Usage (from repo root):
#   .\scripts\smoke-test.ps1
#
# Requires: PowerShell 5.1, Docker (compose v2), curl.exe.
# Exits with non-zero on first failure so it is CI-friendly.

[CmdletBinding()]
param(
    [int]$HealthTimeoutSec = 180,
    [int]$PollIntervalSec  = 3
)

$ErrorActionPreference = 'Stop'

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $RepoRoot
try {
    # docker compose is available (Docker Compose v2)
    function Step($msg)   { Write-Host "`n── $msg ──" -ForegroundColor Yellow }
    function Ok($msg)     { Write-Host "[OK] $msg" -ForegroundColor Green }
    function Fail($msg)   { Write-Host "[FAIL] $msg" -ForegroundColor Red; throw $msg }

    function Wait-Health {
        param([string]$Name, [string]$Url)
        $elapsed = 0
        Step "Waiting for $Name to be healthy at $Url"
        while ($elapsed -lt $HealthTimeoutSec) {
            try {
                $code = (curl.exe -s -o NUL -w '%{http_code}' --max-time 5 $Url)
            } catch { $code = '000' }
            if ($code -eq '200') { Ok "$Name is UP ($Url)"; return }
            Start-Sleep -Seconds $PollIntervalSec
            $elapsed += $PollIntervalSec
        }
        Fail "$Name did not become healthy within ${HealthTimeoutSec}s (last code=$code)"
    }

    Step 'Bringing up the stack'
    docker compose up -d --wait --remove-orphans | Out-Null

    Wait-Health 'gateway (8080)'         'http://localhost:8080/actuator/health'
    Wait-Health 'monolith (8081)'        'http://localhost:8081/actuator/health'
    Wait-Health 'media-service (8083)'   'http://localhost:8083/actuator/health'
    Wait-Health 'profile-service (8084)' 'http://localhost:8084/actuator/health'
    Wait-Health 'post-service (8085)'    'http://localhost:8085/actuator/health'
    Wait-Health 'notification (8086)'    'http://localhost:8086/actuator/health'
    Wait-Health 'feed-service (8087)'    'http://localhost:8087/actuator/health'
    Wait-Health 'MinIO (9000)'           'http://localhost:9000/minio/health/live'
    Wait-Health 'RabbitMQ (15672)'       'http://localhost:15672/api/overview'

    Step 'Verifying public gateway endpoints'

    $code = (curl.exe -s -o NUL -w '%{http_code}' --max-time 5 'http://localhost:8080/auth/inspect')
    if ($code -ne '401') { Fail "GET /auth/inspect expected 401, got $code" }
    Ok 'GET /auth/inspect -> 401 (as expected)'

    $code = (curl.exe -s -o $env:TEMP\feed.json -w '%{http_code}' --max-time 5 'http://localhost:8080/dynamic-feeds?page=1&limit=10')
    if ($code -ne '200') { Fail "GET /dynamic-feeds expected 200, got $code" }
    Ok 'GET /dynamic-feeds -> 200'

    $code = ''
    try { $code = (curl.exe -s -o NUL -w '%{http_code}' --max-time 5 'http://localhost:3000/') } catch { $code = '000' }
    if ($code -eq '200') { Ok 'frontend (3000) -> 200' }
    else { Write-Host "[skip] frontend (3000) not reachable (code=$code)" -ForegroundColor Yellow }

    Step 'Tearing down the stack'
    docker compose down -v | Out-Null

    Write-Host ''
    Write-Host '================  SMOKE TEST PASSED  ================' -ForegroundColor Green
}
finally {
    Pop-Location
}
