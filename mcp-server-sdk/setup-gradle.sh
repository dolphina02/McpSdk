#!/bin/bash

# Gradle Wrapper 설정 스크립트

echo "Setting up Gradle Wrapper..."

# gradle/wrapper 디렉토리 생성
mkdir -p gradle/wrapper

# gradle-wrapper.jar 다운로드
GRADLE_URL="https://services.gradle.org/distributions/gradle-8.5-bin.zip"
ZIP_FILE="gradle-wrapper.zip"

echo "Downloading gradle-wrapper.jar from $GRADLE_URL..."

if command -v curl &> /dev/null; then
    curl -L -o "$ZIP_FILE" "$GRADLE_URL"
elif command -v wget &> /dev/null; then
    wget -O "$ZIP_FILE" "$GRADLE_URL"
else
    echo "Error: Neither curl nor wget found. Please install one of them."
    exit 1
fi

if [ $? -ne 0 ]; then
    echo "Error: Failed to download Gradle"
    exit 1
fi

echo "Downloaded successfully"

# ZIP 파일 추출
echo "Extracting gradle-wrapper.zip..."
unzip -q "$ZIP_FILE"

if [ $? -ne 0 ]; then
    echo "Error: Failed to extract Gradle"
    exit 1
fi

echo "Extracted successfully"

# 정리
rm "$ZIP_FILE"
echo "Cleanup complete"

echo ""
echo "Gradle Wrapper setup complete!"
echo ""
echo "Now you can run:"
echo "  ./gradlew --version"
echo "  ./gradlew clean build"
echo "  ./gradlew :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'"
