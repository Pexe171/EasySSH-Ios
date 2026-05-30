$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$jdk = Join-Path $root ".tools\jdk17-dist\jdk-17.0.19+10"
if (Test-Path $jdk) {
    $env:JAVA_HOME = $jdk
    $env:PATH = "$jdk\bin;$env:PATH"
}

Push-Location $root
try {
    .\gradlew.bat testDebugUnitTest assembleDebug
} finally {
    Pop-Location
}

