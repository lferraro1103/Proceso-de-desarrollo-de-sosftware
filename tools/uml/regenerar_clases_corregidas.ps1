param([string]$PythonExecutable = 'python')
$ErrorActionPreference = 'Stop'
$raizClases = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
function ComprobarPaso([string]$pasoClases) {
    if ($LASTEXITCODE -ne 0) { throw "Fallo: $pasoClases ($LASTEXITCODE)" }
}
Push-Location $raizClases
try {
    & powershell -NoProfile -ExecutionPolicy Bypass -File tools/uml/regenerar.ps1 -SoloPruebas
    ComprobarPaso 'compilacion y pruebas de aplicacion'
    & javac -encoding UTF-8 -parameters -cp 'TP/Grupo9_POO_EntregaFinal/Codigo/lib/*' -d .tools tools/uml/InventarioUml.java
    ComprobarPaso 'compilacion de inventario'
    & java -cp '.tools;TP/Grupo9_POO_EntregaFinal/Codigo/out;TP/Grupo9_POO_EntregaFinal/Codigo/lib/*' InventarioUml TP/Grupo9_POO_EntregaFinal/Codigo docs/iteracion-4/inventario.json
    ComprobarPaso 'inventario AST y bytecode'
    New-Item -ItemType Directory -Force .tools/plantuml-codigo | Out-Null
    Copy-Item -LiteralPath docs/iteracion-4/inventario.json -Destination .tools/plantuml-codigo/inventario-actual.json
    $jarClases = Join-Path $raizClases '.tools/plantuml-1.2026.9.jar'
    if (-not (Test-Path -LiteralPath $jarClases)) {
        New-Item -ItemType Directory -Force .tools | Out-Null
        Invoke-WebRequest https://github.com/plantuml/plantuml/releases/download/v1.2026.9/plantuml-1.2026.9.jar -OutFile $jarClases
    }
    if ((Get-FileHash -LiteralPath $jarClases -Algorithm SHA256).Hash -ne 'AA4A1D06F8911C4B842BEFF4828DD955BE431692B631A312D04C0E9FAB9581DE') {
        throw 'PlantUML: SHA-256 inesperado'
    }
    & $PythonExecutable -X utf8 tools/uml/uml_correcto.py
    ComprobarPaso 'modelo y fuentes'
    foreach ($formatoClases in @('-tsvg','-tpng')) {
        & java -Xmx1g '-DPLANTUML_LIMIT_SIZE=32768' -jar $jarClases -charset UTF-8 $formatoClases -o ../diagramas 'docs/iteracion-4/uml-corregido/fuentes/*.puml'
        ComprobarPaso "render $formatoClases"
    }
    foreach ($scriptClases in @('principal_conectado.py','ajustar_rotulos_svg.py','documentar_relaciones.py','verificar_uml_correcto.py','crear_uml_a4.py','version_sin_curvas.py','exportar_plantuml_codigo.py')) {
        & $PythonExecutable -X utf8 (Join-Path tools/uml $scriptClases)
        ComprobarPaso $scriptClases
    }
} finally { Pop-Location }
