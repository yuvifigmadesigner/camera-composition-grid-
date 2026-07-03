$ErrorActionPreference = "Stop"

$toolsDir = "c:\Users\Itsyu\Downloads\Overlay\tools"
$jdkDir = "$toolsDir\jdk"
$sdkDir = "$toolsDir\android-sdk"
$projectDir = "c:\Users\Itsyu\Downloads\Overlay"

If (!(Test-Path $toolsDir)) { New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null }

Write-Host "Downloading Microsoft OpenJDK 17..."
$jdkZip = "$toolsDir\jdk.zip"
If (!(Test-Path $jdkZip)) {
    Invoke-WebRequest -Uri "https://aka.ms/download-jdk/microsoft-jdk-17.0.12-windows-x64.zip" -OutFile $jdkZip
}

Write-Host "Extracting JDK..."
If (!(Test-Path $jdkDir)) {
    Expand-Archive -Path $jdkZip -DestinationPath $toolsDir -Force
    # Rename the extracted folder (e.g. jdk-17.0.12+7) to just 'jdk'
    $extractedFolder = Get-ChildItem -Path $toolsDir -Directory | Where-Object { $_.Name -like "jdk-17*" }
    Rename-Item -Path $extractedFolder.FullName -NewName "jdk"
}

Write-Host "Downloading Android Command Line Tools..."
$cmdlineZip = "$toolsDir\cmdline-tools.zip"
If (!(Test-Path $cmdlineZip)) {
    Invoke-WebRequest -Uri "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" -OutFile $cmdlineZip
}

Write-Host "Extracting Android SDK..."
If (!(Test-Path "$sdkDir\cmdline-tools")) {
    New-Item -ItemType Directory -Force -Path "$sdkDir\cmdline-tools" | Out-Null
    Expand-Archive -Path $cmdlineZip -DestinationPath "$sdkDir\cmdline-tools" -Force
    # The tools extract as 'cmdline-tools\cmdline-tools'. We need them at 'cmdline-tools\latest' to work properly
    Rename-Item -Path "$sdkDir\cmdline-tools\cmdline-tools" -NewName "latest"
}

# Set Environment Variables for the build
$env:JAVA_HOME = $jdkDir
$env:ANDROID_HOME = $sdkDir
$env:PATH = "$jdkDir\bin;$sdkDir\cmdline-tools\latest\bin;$env:PATH"

Write-Host "Accepting Android SDK licenses and installing platform..."
"y`n" * 50 | Out-File -FilePath "$toolsDir\yes.txt" -Encoding ascii
cmd.exe /c "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat --licenses < $toolsDir\yes.txt"
cmd.exe /c "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat `"platforms;android-34`" `"build-tools;34.0.0`" < $toolsDir\yes.txt"

Write-Host "Downloading Gradle..."
$gradleZip = "$toolsDir\gradle.zip"
$gradleDir = "$toolsDir\gradle"
if (Test-Path $gradleZip) { Remove-Item $gradleZip -Force }
Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.4-bin.zip" -OutFile $gradleZip

If (!(Test-Path $gradleDir)) {
    Expand-Archive -Path $gradleZip -DestinationPath $toolsDir -Force
    $extractedFolder = Get-ChildItem -Path $toolsDir -Directory | Where-Object { $_.Name -like "gradle-8*" }
    Rename-Item -Path $extractedFolder.FullName -NewName "gradle"
}

$env:PATH = "$gradleDir\bin;$env:PATH"

Write-Host "Building the APK..."
Set-Location $projectDir
gradle assembleDebug --no-daemon

Write-Host "Build complete! Checking for APK..."
$apkPath = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"
If (Test-Path $apkPath) {
    Copy-Item $apkPath -Destination "$projectDir\GridOverlay.apk" -Force
    Write-Host "SUCCESS! The APK has been copied to: $projectDir\GridOverlay.apk"
} Else {
    Write-Host "ERROR: APK was not found. Build may have failed."
}
