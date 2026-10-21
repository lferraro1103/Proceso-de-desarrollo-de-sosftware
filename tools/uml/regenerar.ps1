param(
    [string]$PlantUmlJar = '',
    [switch]$SoloPruebas,
    [string]$PythonExecutable = 'python'
)
$ErrorActionPreference = 'Stop'
$raizUml = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$codigoUml = Join-Path $raizUml 'TP/Grupo9_POO_EntregaFinal/Codigo'
function ExigirExito([string]$paso) {
    if ($LASTEXITCODE -ne 0) { throw "Falló: $paso (código $LASTEXITCODE)" }
}
Push-Location $codigoUml
try {
    $fuentesUml = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
    & javac -encoding UTF-8 -parameters -cp 'lib/*' -d out $fuentesUml
    ExigirExito 'compilación de aplicación'
    & javac -encoding UTF-8 -cp 'out;lib/*' -d out-test test/PruebasIteracion4.java
    ExigirExito 'compilación de pruebas'
    & java -ea -cp 'out-test;out;lib/*' PruebasIteracion4
    ExigirExito 'regresiones'
} finally { Pop-Location }
if ($SoloPruebas) { return }
Push-Location $raizUml
try {
    New-Item -ItemType Directory -Path .tools -Force | Out-Null
    if (-not $PlantUmlJar) {
        $PlantUmlJar = Join-Path $raizUml '.tools/plantuml-1.2026.9.jar'
        if (-not (Test-Path -LiteralPath $PlantUmlJar)) {
            Invoke-WebRequest -Uri 'https://github.com/plantuml/plantuml/releases/download/v1.2026.9/plantuml-1.2026.9.jar' -OutFile $PlantUmlJar
        }
        $esperadoUml = 'AA4A1D06F8911C4B842BEFF4828DD955BE431692B631A312D04C0E9FAB9581DE'
        if ((Get-FileHash -LiteralPath $PlantUmlJar -Algorithm SHA256).Hash -ne $esperadoUml) {
            throw 'El SHA-256 del jar PlantUML no coincide con el usado para esta entrega.'
        }
    }
    $PlantUmlJar = (Resolve-Path -LiteralPath $PlantUmlJar).Path
    & javac -encoding UTF-8 -parameters -cp 'TP/Grupo9_POO_EntregaFinal/Codigo/lib/*' -d .tools tools/uml/InventarioUml.java
    ExigirExito 'compilación del inventario'
    & java -cp '.tools;TP/Grupo9_POO_EntregaFinal/Codigo/out;TP/Grupo9_POO_EntregaFinal/Codigo/lib/*' InventarioUml TP/Grupo9_POO_EntregaFinal/Codigo docs/iteracion-4/inventario.json
    ExigirExito 'inventario AST / bytecode'
    & $PythonExecutable -c 'import PIL'
    ExigirExito 'dependencia Pillow para medir fuentes y dibujar PNG'
    & $PythonExecutable tools/uml/generar_diagramas.py
    ExigirExito 'fuentes UML'
    & $PythonExecutable tools/uml/test_trazado.py
    ExigirExito 'pruebas del detector de superposiciones'
    & $PythonExecutable tools/uml/ordenar_diagramas.py
    ExigirExito 'render determinista PNG y SVG'
    & java -Xmx384m '-XX:+UseSerialGC' -jar $PlantUmlJar -checkonly -charset UTF-8 'docs/iteracion-4/fuentes/*.puml'
    ExigirExito 'sintaxis de fuentes semanticas PlantUML'
    & $PythonExecutable tools/uml/verificar_diagramas.py
    ExigirExito 'cobertura e integridad de diagramas'
} finally { Pop-Location }
