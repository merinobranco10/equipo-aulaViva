<#
.SYNOPSIS
  AulaViva - Demo del Dia 1 (S18) en PowerShell: recorre los pasos 1, 2, 5 y 6 del guion.

.DESCRIPTION
  Requiere la app corriendo en OTRA ventana con el perfil demo y la misma clave:
    cd backend
    $env:DEMO_PASSWORD = '<tu-clave>'
    java -Xmx512m -jar target\app.jar --spring.profiles.active=h2,demo

  Pasos que muestra:
    1. docente.a crea una evaluacion de 2 preguntas.
    2. estudiante.a la ve (sin respuestas correctas), la rinde y recibe nota + feedback.
    5. apoderado.a ve el avance de su pupilo.
    6. docente.b (otro colegio) intenta abrir la evaluacion -> 404 (aislamiento).
  Termina con error si el aislamiento no responde 404.

.EXAMPLE
  .\scripts\demo-dia1.ps1 -Password '<tu-clave>'

.EXAMPLE
  .\scripts\demo-dia1.ps1 -Base 'https://<codespace>-8080.app.github.dev' -Password '<tu-clave>'

.NOTES
  Si Windows bloquea el script: Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
  En Windows PowerShell 5.1 los tildes del feedback pueden verse mal en consola; en
  PowerShell 7 (pwsh) se ven bien. Es solo visual: la API responde en UTF-8.
#>
param(
    [string]$Base = 'http://localhost:8080',
    [string]$Password = $env:DEMO_PASSWORD
)

$ErrorActionPreference = 'Stop'
if (-not $Password) {
    throw 'Indica la clave con -Password o define $env:DEMO_PASSWORD (la misma con que levantaste la app).'
}

function Auth([string]$User) {
    $token = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("${User}:$Password"))
    return @{ Authorization = "Basic $token" }
}

function Clave {
    return @{ 'Idempotency-Key' = [guid]::NewGuid().ToString() }
}

function Paso([string]$Texto) {
    Write-Host ''
    Write-Host "== $Texto" -ForegroundColor Cyan
}

function Post-Json([string]$Uri, [hashtable]$Headers, $Object) {
    $json = $Object | ConvertTo-Json -Depth 6
    $bytes = [Text.Encoding]::UTF8.GetBytes($json)
    return Invoke-RestMethod -Method Post -Uri $Uri -Headers $Headers `
        -ContentType 'application/json; charset=utf-8' -Body $bytes
}

function Get-Status([string]$Uri, [hashtable]$Headers) {
    try {
        Invoke-RestMethod -Uri $Uri -Headers $Headers | Out-Null
        return 200
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        throw
    }
}

Paso '0. La app responde y la clave es correcta'
try {
    Invoke-RestMethod -Uri "$Base/api/v1/status" | Out-Null
} catch {
    throw "No hay respuesta en $Base. Levanta la app en otra ventana (ver ayuda: Get-Help .\scripts\demo-dia1.ps1)."
}
$me = $null
try {
    $me = Invoke-RestMethod -Uri "$Base/v1/me" -Headers (Auth 'docente.a')
} catch {
    throw 'La clave no coincide con la de la app (401). Usa la misma que en $env:DEMO_PASSWORD de la otra ventana.'
}
Write-Host ("Conectado como {0} ({1}) en {2}" -f $me.fullName, $me.role, $me.tenantName)

Paso '1. docente.a crea una evaluacion de 2 preguntas'
$evaluacion = @{
    courseId  = 'matematica-8a'
    title     = 'Demo enteros'
    questions = @(
        @{ text = 'Cuanto es (-5) x 3?'; options = @('15', '-15', '-8'); correctOption = 1; points = 2;
           explanation = 'Signos distintos: el producto es negativo.' },
        @{ text = 'Cuanto es (-20) : (-4)?'; options = @('5', '-5', '-24'); correctOption = 0; points = 2 }
    )
}
$creada = Post-Json "$Base/v1/evaluations" ((Auth 'docente.a') + (Clave)) $evaluacion
$creada | Format-List id, title, status

Paso '2a. estudiante.a ve la evaluacion (sin respuestas correctas)'
$vista = Invoke-RestMethod -Uri "$Base/v1/evaluations/$($creada.id)" -Headers (Auth 'estudiante.a')
$vista.questions | ForEach-Object {
    Write-Host (" - {0}  opciones: {1}" -f $_.text, ($_.options -join ' | '))
}

Paso '2b. estudiante.a rinde (1 correcta y 1 incorrecta) y recibe nota + feedback'
$respuestas = @{
    answers = @(
        @{ questionId = $vista.questions[0].id; selectedOption = 1 },
        @{ questionId = $vista.questions[1].id; selectedOption = 1 }
    )
}
$resultado = Post-Json "$Base/v1/evaluations/$($creada.id)/submissions" ((Auth 'estudiante.a') + (Clave)) $respuestas
Write-Host ("Nota: {0}   Puntaje: {1}/{2}   Estado: {3}" -f $resultado.grade, $resultado.score, $resultado.maxScore, $resultado.status)
$resultado.feedback | ForEach-Object { Write-Host (" - {0}" -f $_.message) }

Paso '5. apoderado.a ve el avance de su pupilo'
$progreso = Invoke-RestMethod -Uri "$Base/v1/students/u-estudiante-a/progress" -Headers (Auth 'apoderado.a')
$progreso | Format-List completedEvaluations, pendingEvaluations, averageGrade

Paso '6. docente.b (otro colegio) intenta abrir la evaluacion de A -> debe ser 404'
$codigo = Get-Status "$Base/v1/evaluations/$($creada.id)" (Auth 'docente.b')
Write-Host "HTTP $codigo"
if ($codigo -ne 404) {
    throw "FALLA: se esperaba 404 y llego $codigo (revisar aislamiento entre colegios)."
}
Write-Host 'OK: aislamiento entre colegios' -ForegroundColor Green

Paso 'Extra. Sin credenciales -> 401'
Write-Host ("HTTP {0}" -f (Get-Status "$Base/v1/me" @{}))

Write-Host ''
Write-Host 'Demo del Dia 1 completa.' -ForegroundColor Green
