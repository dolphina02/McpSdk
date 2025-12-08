# Gradle Wrapper 설정 스크립트

Write-Host "Setting up Gradle Wrapper..." -ForegroundColor Green

# gradle/wrapper 디렉토리 생성
$wrapperDir = "gradle\wrapper"
if (-not (Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
    Write-Host "Created $wrapperDir directory"
}

# gradle-wrapper.jar 다운로드
$jarUrl = "https://services.gradle.org/distributions/gradle-8.5-bin.zip"
$zipPath = "gradle-wrapper.zip"
$jarPath = "$wrapperDir\gradle-wrapper.jar"

Write-Host "Downloading gradle-wrapper.jar from $jarUrl..."

try {
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    $webClient = New-Object System.Net.WebClient
    $webClient.DownloadFile($jarUrl, $zipPath)
    Write-Host "Downloaded successfully" -ForegroundColor Green
    
    # ZIP 파일 추출
    Write-Host "Extracting gradle-wrapper.zip..."
    Expand-Archive -Path $zipPath -DestinationPath . -Force
    Write-Host "Extracted successfully" -ForegroundColor Green
    
    # 정리
    Remove-Item $zipPath -Force
    Write-Host "Cleanup complete" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "Gradle Wrapper setup complete!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Now you can run:" -ForegroundColor Cyan
    Write-Host "  gradlew.bat --version"
    Write-Host "  gradlew.bat clean build"
    Write-Host "  gradlew.bat :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'"
    
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Alternative: Download manually from:" -ForegroundColor Yellow
    Write-Host "  https://repo.gradle.org/gradle/gradle-8.5-wrapper.zip"
    Write-Host "  Extract to the project root directory"
}
