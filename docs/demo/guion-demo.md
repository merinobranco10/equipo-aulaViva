# Guion de la demo (borrador · Día 1)

> Borrador del PO. Matías lo completa el Día 2 con la UI (URLs, orden de pestañas y capturas).
> Duración objetivo: 3 minutos. Demo en vivo sobre staging; nunca video.

## Datos de la demo

| Colegio (tenant) | Curso | Usuarios |
|---|---|---|
| Colegio Los Andes (`colegio-a`) | Matemática 8° básico A (`matematica-8a`) | `docente.a`, `estudiante.a`, `apoderado.a` |
| Colegio Pacífico (`colegio-b`) | Ciencias Naturales 8° básico B (`ciencias-8b`) | `docente.b`, `estudiante.b`, `coordinador.b` |

- La contraseña de los usuarios es la variable `DEMO_PASSWORD` (Codespaces/GitHub secret). **No se escribe en este documento.**
- Al iniciar, `estudiante.a` ya tiene "Números enteros" rendida (nota 4,5) y "Potencias de base entera" pendiente.
- Todos los nombres son ficticios.

## Pasos

| # | Usuario | Acción | Qué se dice (1 frase) | Fallback si falla |
|---|---|---|---|---|
| 1 | docente.a | Crea una evaluación de 3 preguntas con alternativas | "El docente crea una prueba en 30 segundos." | Mostrar "Potencias de base entera", ya creada en la semilla |
| 2 | estudiante.a | Rinde la evaluación y ve nota y feedback por pregunta | "La corrección y el feedback son inmediatos, con escala de 1,0 a 7,0." | Rendir "Potencias" (pendiente en la semilla) |
| 3 | estudiante.a | Pregunta al tutor algo del apunte | "Responde solo con los apuntes del curso y cita la fuente." | Modo sin IA (respuesta extractiva) · se habilita el Día 4 |
| 4 | estudiante.a | Intenta un prompt injection | "El guardrail lo bloquea y queda registrado." | Mostrar el test del guardrail en CI · Día 4/5 |
| 5 | apoderado.a | Ve el avance de su pupilo | "El apoderado sigue el progreso sin pedir informes." | Datos de la semilla (1 rendida, 1 pendiente) |
| 6 | docente.b | Abre la evaluación del Colegio A | "Otro colegio no puede verla: responde 404." | Mostrar `SeguridadTest#docenteDeOtroColegioRecibe404` |

## Probar hoy (Día 1, sin UI)

Ventana 1 (dejarla abierta):

```powershell
cd backend
$env:DEMO_PASSWORD = '<tu-clave>'
java -Xmx512m -jar target\app.jar --spring.profiles.active=h2,demo
```

Ventana 2, en PowerShell:

```powershell
.\scripts\demo-dia1.ps1 -Password '<tu-clave>'
```

O en Git Bash / Codespaces:

```bash
DEMO_PASSWORD='<tu-clave>' ./scripts/demo-dia1.sh
```

Ambos scripts recorren los pasos 1, 2, 5 y 6 y fallan si el aislamiento entre colegios no responde 404.

## Pendiente (Día 2 · Matías)

- [ ] Reemplazar curl por la UI (`index.html`, `docente.html`, `estudiante.html`, `apoderado.html`).
- [ ] URL de staging y orden de pestañas.
- [ ] Tiempo real de cada paso tras probarlo 2 veces.
