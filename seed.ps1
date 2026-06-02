# NOTE: This seed script is a legacy dev-only seed. It hits the gateway
# without an authenticated session cookie. With the Phase 2 security work,
# the profile/post endpoints now return 401 without a valid SESSION cookie.
#
# To re-enable seeding, either:
#   1. Authenticate via OAuth2 first and reuse the SESSION cookie, OR
#   2. Use the `seed` profile (or a dev-only seed token) — to be added
#      in a follow-up lane per plan Phase 2.5 / 5.5.
#
# The username below is a placeholder aligned with ADR-012 (alice/bob/carol).
$baseUrl = "http://localhost:8080"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "Creating seed profile (requires authenticated session; will return 401 if not logged in)..."
$profileBody = @{
    displayName = "Alice Dev"
    username = "alice_dev"
    bio = "Digital artist & cyberpunk enthusiast. Creating worlds out of pixels."
} | ConvertTo-Json
try {
    Invoke-WebRequest -Method Post -Uri "$baseUrl/profiles" -Headers $headers -Body $profileBody
} catch {
    Write-Host "Profile creation skipped: $($_.Exception.Message)"
}

Write-Host "Creating seed profile image (requires authenticated session)..."
# Transparent 1x1 PNG pixel for avatar placeholder since we can't easily download unsplash here without auth/API
$avatarB64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
$avatarBody = @{
    base64ImageString = $avatarB64
} | ConvertTo-Json
try {
    Invoke-WebRequest -Method Post -Uri "$baseUrl/profiles/profile-image" -Headers $headers -Body $avatarBody
} catch {
    Write-Host "Profile image upload skipped: $($_.Exception.Message)"
}

Write-Host "Creating seed post 1 (requires authenticated session)..."
# Red square
$post1B64 = "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFUlEQVR42mP8z8BQz0AEYBxVSF+FAAhKDveksOjmAAAAAElFTkSuQmCC"
$post1Body = @{
    base64ImageString = $post1B64
    caption = "Exploring the neon streets tonight. The vibes are immaculate! #cyberpunk #citylights"
} | ConvertTo-Json
try {
    Invoke-WebRequest -Method Post -Uri "$baseUrl/posts" -Headers $headers -Body $post1Body
} catch {
    Write-Host "Post 1 creation skipped: $($_.Exception.Message)"
}

Write-Host "Creating seed post 2 (requires authenticated session)..."
# Blue square
$post2B64 = "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFUlEQVR42mNkYPjPQAQwjCqkr0IAnf8D9e6F0HkAAAAASUVORK5CYII="
$post2Body = @{
    base64ImageString = $post2B64
    caption = "Abstract fluid gradients are my new obsession. What do you guys think?"
} | ConvertTo-Json
try {
    Invoke-WebRequest -Method Post -Uri "$baseUrl/posts" -Headers $headers -Body $post2Body
} catch {
    Write-Host "Post 2 creation skipped: $($_.Exception.Message)"
}

Write-Host "Done seeding (requests that required auth may have been skipped)."
