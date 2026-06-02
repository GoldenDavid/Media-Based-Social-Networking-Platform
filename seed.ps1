$baseUrl = "http://localhost:8080"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "Creating Mock Profile..."
$profileBody = @{
    displayName = "Alex Cyber"
    username = "alex_cyber"
    bio = "Digital artist & cyberpunk enthusiast. Creating worlds out of pixels. 🎮✨"
} | ConvertTo-Json
Invoke-WebRequest -Method Post -Uri "$baseUrl/profiles" -Headers $headers -Body $profileBody

Write-Host "Creating Mock Profile Image..."
# Transparent 1x1 PNG pixel for avatar placeholder since we can't easily download unsplash here without auth/API
$avatarB64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
$avatarBody = @{
    base64ImageString = $avatarB64
} | ConvertTo-Json
Invoke-WebRequest -Method Post -Uri "$baseUrl/profiles/profile-image" -Headers $headers -Body $avatarBody

Write-Host "Creating Mock Post 1..."
# Red square
$post1B64 = "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFUlEQVR42mP8z8BQz0AEYBxVSF+FAAhKDveksOjmAAAAAElFTkSuQmCC"
$post1Body = @{
    base64ImageString = $post1B64
    caption = "Exploring the neon streets tonight. The vibes are immaculate! 🌃✨ #cyberpunk #citylights"
} | ConvertTo-Json
Invoke-WebRequest -Method Post -Uri "$baseUrl/posts" -Headers $headers -Body $post1Body

Write-Host "Creating Mock Post 2..."
# Blue square
$post2B64 = "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFUlEQVR42mNkYPjPQAQwjCqkr0IAnf8D9e6F0HkAAAAASUVORK5CYII="
$post2Body = @{
    base64ImageString = $post2B64
    caption = "Abstract fluid gradients are my new obsession. What do you guys think? 🎨"
} | ConvertTo-Json
Invoke-WebRequest -Method Post -Uri "$baseUrl/posts" -Headers $headers -Body $post2Body

Write-Host "Done seeding database!"
