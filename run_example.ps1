$ErrorActionPreference = 'Stop'

$libDir = Join-Path $PSScriptRoot 'lib'
if (!(Test-Path $libDir)) { New-Item -ItemType Directory -Path $libDir | Out-Null }

$jars = @(
    'https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar',
    'https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar',
    'https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar'
)

Write-Host "Downloading jars to $libDir..."
foreach ($url in $jars) {
    $file = Split-Path $url -Leaf
    $out = Join-Path $libDir $file
    if (!(Test-Path $out)) {
        Write-Host "  Downloading $file..."
        Invoke-WebRequest -Uri $url -OutFile $out
    } else {
        Write-Host "  Already have $file"
    }
}

$targetClasses = Join-Path $PSScriptRoot 'target\classes'
if (!(Test-Path $targetClasses)) { New-Item -ItemType Directory -Path $targetClasses -Force | Out-Null }

Write-Host "Compiling Java sources..."
$sources = Get-ChildItem -Path (Join-Path $PSScriptRoot 'src\main\java') -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$cp = Join-Path $libDir '*'

javac -cp $cp -d $targetClasses $sources

Write-Host "Running example..."
java -cp "$targetClasses;$libDir\*" org.yourcompany.yourproject.JIMP21
