@echo off
REM Gradle Wrapper 설정 스크립트

echo Setting up Gradle Wrapper...

REM gradle/wrapper 디렉토리 생성
if not exist gradle\wrapper mkdir gradle\wrapper

REM gradle-wrapper.jar 다운로드
echo Downloading gradle-wrapper.jar...
powershell -Command "(New-Object System.Net.ServicePointManager).SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; (New-Object System.Net.WebClient).DownloadFile('https://repo.gradle.org/gradle/gradle-8.5-wrapper.zip', 'gradle-wrapper.zip')"

REM ZIP 파일 추출
echo Extracting gradle-wrapper.zip...
powershell -Command "Expand-Archive -Path gradle-wrapper.zip -DestinationPath . -Force"

REM 정리
del gradle-wrapper.zip

echo Gradle Wrapper setup complete!
echo.
echo Now you can run:
echo   gradlew.bat --version
echo   gradlew.bat clean build
