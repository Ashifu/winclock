# Winclock Installation Script
# This script copies the built JAR and wrapper to a local directory and adds it to the User PATH.

$installDir = Join-Path $HOME ".winclock\bin"
if (!(Test-Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir -Force | Out-Null
}

$jarFile = "target\win-clock-1.0.jar"
$shimSource = "shim.cs"
$exeFile = "winclock.exe"

# 1. Build JAR if missing
if (!(Test-Path $jarFile)) {
    Write-Host "Building project first..." -ForegroundColor Cyan
    mvn package -DskipTests
}

# 2. Compile Native Shim
if (Test-Path $shimSource) {
    $cscPath = Join-Path $env:SystemRoot "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
    if (!(Test-Path $cscPath)) {
        # Fallback to Framework (non-64)
        $cscPath = Join-Path $env:SystemRoot "Microsoft.NET\Framework\v4.0.30319\csc.exe"
    }

    if (Test-Path $cscPath) {
        Write-Host "Compiling native shim..." -ForegroundColor Cyan
        & $cscPath /out:$exeFile /target:exe /nologo $shimSource
    } else {
        Write-Warning "C# compiler not found. Falling back to batch script."
        $exeFile = "winclock.bat"
    }
}

# 3. Copy Files
if (Test-Path $jarFile) {
    Copy-Item $jarFile (Join-Path $installDir "win-clock-1.0.jar") -Force
    if (Test-Path $exeFile) {
        Copy-Item $exeFile (Join-Path $installDir (Split-Path $exeFile -Leaf)) -Force
    }
    Write-Host "Files installed to $installDir" -ForegroundColor Green
    
    # Add to PATH if not already there
    $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if ($currentPath -notlike "*$installDir*") {
        Write-Host "Adding $installDir to User PATH..." -ForegroundColor Cyan
        [Environment]::SetEnvironmentVariable("Path", $currentPath + ";" + $installDir, "User")
        Write-Host "Permanent PATH updated." -ForegroundColor Green
    }
    
    # Refresh current session path
    if ($env:Path -notlike "*$installDir*") {
        $env:Path += ";$installDir"
    }
    
    Write-Host "Installation complete! Try running 'winclock' now." -ForegroundColor Green
} else {
    Write-Error "Build failed or JAR not found!"
}
