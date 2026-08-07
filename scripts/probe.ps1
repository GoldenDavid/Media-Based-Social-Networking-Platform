$urls = @(
  @{ name = 'gateway';       url = 'http://localhost:8080/actuator/health' }
  @{ name = 'monolith';      url = 'http://localhost:8081/actuator/health' }
  @{ name = 'media';         url = 'http://localhost:8083/actuator/health' }
  @{ name = 'profile';       url = 'http://localhost:8084/actuator/health' }
  @{ name = 'post';          url = 'http://localhost:8085/actuator/health' }
  @{ name = 'notification';  url = 'http://localhost:8086/actuator/health' }
  @{ name = 'feed';          url = 'http://localhost:8087/actuator/health' }
  @{ name = 'minio';         url = 'http://localhost:9000/minio/health/live' }
  @{ name = 'rabbitmq';      url = 'http://localhost:15672/api/overview' }
  @{ name = 'frontend';      url = 'http://localhost:3000/' }
)

foreach ($u in $urls) {
  try {
    $r = Invoke-WebRequest -Uri $u.url -UseBasicParsing -TimeoutSec 5
    Write-Host ("{0,-14} {1,4}  {2}" -f $u.name, $r.StatusCode, $u.url)
  } catch {
    $code = $_.Exception.Response.StatusCode.value__
    if (-not $code) { $code = 'fail' }
    Write-Host ("{0,-14} {1,4}  {2}" -f $u.name, $code, $u.url) -ForegroundColor Yellow
  }
}

# Public endpoints (no auth)
$endpoints = @(
  @{ name = 'auth-inspect';  url = 'http://localhost:8080/auth/inspect' }
  @{ name = 'dynamic-feeds'; url = 'http://localhost:8080/dynamic-feeds?page=1&limit=10' }
  @{ name = 'precomputed';   url = 'http://localhost:8080/precomputed-feeds?page=1&limit=10' }
)

Write-Host ''
Write-Host 'Public endpoints:'
foreach ($e in $endpoints) {
  try {
    $r = Invoke-WebRequest -Uri $e.url -UseBasicParsing -TimeoutSec 5
    Write-Host ("{0,-14} {1,4}  {2}" -f $e.name, $r.StatusCode, $e.url)
  } catch {
    $code = $_.Exception.Response.StatusCode.value__
    if (-not $code) { $code = 'fail' }
    Write-Host ("{0,-14} {1,4}  {2}" -f $e.name, $code, $e.url) -ForegroundColor Yellow
  }
}
