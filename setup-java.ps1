# PowerShell script to configure Java 17 for Maven builds
# This script helps set up JAVA_HOME for JDK 17

Write-Host "Checking for Java 17 JDK installation..." -ForegroundColor Cyan

# Common JDK 17 installation locations
$possiblePaths = @(
    "C:\Program Files\Java\jdk-17*",
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\Microsoft\jdk-17*",
    "C:\Program Files (x86)\Java\jdk-17*",
    "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\jdk-17*"
)

$jdkPath = $null

foreach ($path in $possiblePaths) {
    $found = Get-ChildItem -Path $path -ErrorAction SilentlyContinue | 
             Where-Object { $_.PSIsContainer -and (Test-Path (Join-Path $_.FullName "bin\javac.exe")) } |
             Sort-Object LastWriteTime -Descending |
             Select-Object -First 1
    
    if ($found) {
        $jdkPath = $found.FullName
        Write-Host "Found JDK 17 at: $jdkPath" -ForegroundColor Green
        break
    }
}

if (-not $jdkPath) {
    Write-Host ""
    Write-Host "JDK 17 not found on your system." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please install JDK 17 from one of these sources:" -ForegroundColor Yellow
    Write-Host "  1. Eclipse Adoptium (Temurin): https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Cyan
    Write-Host "  2. Microsoft Build of OpenJDK: https://learn.microsoft.com/en-us/java/openjdk/download" -ForegroundColor Cyan
    Write-Host "  3. Oracle JDK: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "After installation, run this script again to configure JAVA_HOME." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Alternatively, you can build using Docker:" -ForegroundColor Yellow
    Write-Host "  docker-compose build" -ForegroundColor Cyan
    Write-Host "  docker-compose up" -ForegroundColor Cyan
    exit 1
}

# Verify it's a JDK (has javac)
$javacPath = Join-Path $jdkPath "bin\javac.exe"
if (-not (Test-Path $javacPath)) {
    Write-Host "Error: Found Java installation but it's not a JDK (javac not found)" -ForegroundColor Red
    exit 1
}

# Set JAVA_HOME for current session
$env:JAVA_HOME = $jdkPath
Write-Host ""
Write-Host "Set JAVA_HOME for current session: $env:JAVA_HOME" -ForegroundColor Green

# Add to PATH if not already there
$javaBin = Join-Path $jdkPath "bin"
if ($env:PATH -notlike "*$javaBin*") {
    $env:PATH = "$javaBin;$env:PATH"
    Write-Host "Added Java bin to PATH for current session" -ForegroundColor Green
}

# Verify Java version
Write-Host ""
Write-Host "Verifying Java installation..." -ForegroundColor Cyan
& "$javaBin\java.exe" -version
& "$javaBin\javac.exe" -version

Write-Host ""
Write-Host "Java 17 JDK is now configured for this PowerShell session!" -ForegroundColor Green
Write-Host ""
Write-Host "To make this permanent, set JAVA_HOME in System Environment Variables:" -ForegroundColor Yellow
Write-Host "  1. Open System Properties > Environment Variables" -ForegroundColor Cyan
Write-Host "  2. Add JAVA_HOME = $jdkPath" -ForegroundColor Cyan
Write-Host "  3. Add %JAVA_HOME%\bin to PATH" -ForegroundColor Cyan
Write-Host ""
Write-Host "You can now run: mvn clean install" -ForegroundColor Green
