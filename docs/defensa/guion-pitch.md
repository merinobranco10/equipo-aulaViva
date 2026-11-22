# Guion de defensa — AulaViva (15 minutos)

**Sesión:** S18 · Día 6  
**Duración total:** 15:00 exactos  
**Regla:** no leer las láminas; una idea por lámina y cada afirmación debe enlazar a evidencia del repositorio.

## Cronograma maestro

| Bloque | Tiempo | Duración | Presenta | Transición |
|---|---:|---:|---|---|
| 01 · Elevator pitch | 0:00–1:00 | 1:00 | Branco | «Alexander les muestra cómo funciona» |
| 02 · Demo en vivo | 1:00–4:00 | 3:00 | Alexander (Branco maneja navegador) | «Matías explica cómo está construido» |
| 03 · Arquitectura | 4:00–6:30 | 2:30 | Matías | «Valentina cuenta cómo lo protegemos» |
| 04 · DevSecOps | 6:30–8:30 | 2:00 | Valentina | «Gerardo explica la IA» |
| 05 · IA responsable | 8:30–10:30 | 2:00 | Gerardo | «¿Y si algo falla?» |
| 06 · Resiliencia + FinOps | 10:30–12:30 | 2:00 | Valentina 1:00 + Branco 1:00 | «¿Qué sigue?» |
| 07 · Roadmap + aprendizajes | 12:30–15:00 | 2:30 | Matías + una frase de cada integrante | Cierre |

## Texto y pauta por bloque

### 01 · Elevator pitch — Branco (0:00–1:00)
**Idea central:** AulaViva moderniza el aprendizaje escolar con una plataforma SaaS multi-tenant que combina evaluaciones auto-corregidas, seguimiento académico y un tutor IA contextualizado a los apuntes de cada curso.

**Pauta:** problema del docente (tiempo de corrección y visibilidad del progreso); valor para estudiante (feedback inmediato y apoyo contextual); valor institucional (gestión multi-colegio con aislamiento lógico). No prometer resultados cuantitativos de impacto que todavía no estén medidos.

**Transición:** «Alexander les muestra cómo funciona».

### 02 · Demo en vivo — Alexander (1:00–4:00)
Branco maneja el navegador. Seguir `docs/demo/guion-demo.md` y respetar el orden y datos ficticios definidos allí:
1. Docente crea evaluación.
2. Estudiante la rinde y recibe nota/feedback.
3. Estudiante consulta Tutor IA.
4. Se intenta prompt injection y se muestra el bloqueo.
5. Apoderado revisa progreso.
6. Docente de otro colegio intenta consultar la evaluación y recibe 404.

**Control de tiempo:** 40 s creación; 55 s rendición/feedback; 15 s pregunta al tutor; 15 s intento de injection; 25 s panel apoderado; 20 s aislamiento. Dejar margen para cambios de pestaña. No usar datos reales de menores.

**Transición:** «Matías explica cómo está construido».

### 03 · Arquitectura — Matías (4:00–6:30)
** C4 L1 → L2 → L3 (1:15):**
- Explicar el sistema y sus actores en L1.
- En L2, ubicar frontend, backend y servicios externos.
- En L3, mostrar módulos/componentes relevantes: identidad/RBAC, evaluaciones/progreso, outbox y Tutor IA.
- Recalcar que el MVP es un monolito modular con evolución progresiva; no describir el Tutor IA como microservicio ya desplegado.

** Top 3 ADRs y descartes (1:15):**
- ADR 0002: monolito modular como punto de partida y evolución progresiva.
- ADR 0004 (datos/eventos): persistencia PostgreSQL y Outbox en tabla para el MVP; Kafka/MSK diferido hasta que existan 2+ consumidores.
- ADR 0007/0008 o ADR 0009 según el argumento: aislamiento multi-tenant e identidad/RBAC; o proveedor Tutor IA Groq + recuperación BM25. Elegir tres ADRs que estén efectivamente aprobados y mostrar la tabla de alternativas del documento.
- No confundir los dos archivos ADR numerados 0004: uno trata persistencia/mensajería y el otro Kubernetes/IaC.

**Mensaje de cierre:** «Elegimos soluciones que resuelven el MVP hoy y dejamos explícitos los puntos de evolución».

**Transición:** «Valentina cuenta cómo lo protegemos».

### 04 · DevSecOps — Valentina (6:30–8:30)
Presentar el pipeline y sus gates con el último run real de GitHub Actions:
- Calidad y pruebas Maven/JaCoCo.
- SAST Semgrep.
- SCA Trivy y SBOM CycloneDX.
- Detección de secretos.
- Escaneo IaC y contenedor.
- Firma/attestation de supply chain cuando corresponda.
- Mostrar `docs/security/secrets.md` y `docs/threat-model.md`.

**No inventar estado del último run:** abrir el enlace real de Actions durante el ensayo y confirmar que el run enlazado en el deck y el README siga en verde.

**Transición:** «Gerardo explica la IA».

### 05 · IA responsable — Gerardo (8:30–10:30)
- Proveedor Groq; principal `openai/gpt-oss-120b`, respaldo `openai/gpt-oss-20b`.
- RAG léxico BM25 sobre 9 apuntes de 2 cursos; `top-k=5`, `min-score=1,5`.
- Guardrails: bloqueo de prompt injection, enmascaramiento de datos personales, validación de tenant/curso y respuesta con fuentes.
- Evaluación: golden set de 50 casos. Resultado global: Hit@3 85 %, hechos clave 82 %, fidelidad 88 %; la meta de fidelidad era ≥90 %, por lo que queda una brecha explícita de 2 puntos.
- Seguridad evaluada: 4/4 prompt injection bloqueados; aislamiento 1/1 con HTTP 404; protección de RUT 1/1.
- Limitaciones: BM25 puede fallar con vocabulario distinto; caché y presupuesto diario en memoria; no se usa pgvector en el MVP.

**Transición:** «¿Y si algo falla?».

### 06 · Resiliencia + FinOps — Valentina y Branco (10:30–12:30)
**Valentina (10:30–11:30):** SLOs, game day, recuperación y post-mortem. Abrir `docs/sre/` y `docs/postmortems/`; mencionar solo objetivos y resultados documentados.

**Branco (11:30–12:30):** costo mensual, costo LLM por colegio, costo por estudiante, break-even y dos palancas de optimización, según `docs/finops/costos.md`. Usar los valores calculados y los tokens medidos; no estimar cifras al vuelo.

**Transición:** «¿Qué sigue?».

### 07 · Roadmap + aprendizajes — Matías y equipo (12:30–15:00)
**Matías (12:30–14:00):** priorizar los próximos pasos con dos semanas adicionales. El orden sugerido por la pauta es Keycloak/OIDC → RLS → pgvector; confirmar cada punto contra los issues vigentes y distinguir diseño de implementación. Explicar el riesgo actual de aislamiento aplicado en la aplicación y no reforzado todavía con RLS en base de datos.

**Una frase por integrante (14:00–14:40):**
- Branco: valor/impacto y costo sostenible.
- Alexander: calidad de la demo y evidencia de pruebas.
- Valentina: seguridad y operación.
- Gerardo: evaluación y límites del Tutor IA.
- Matías: decisiones y coordinación técnica.

**Cierre Matías (14:40–15:00):** «AulaViva entrega un flujo académico demostrable y evidencia verificable. También mostramos con transparencia qué está implementado, qué está diferido y cuál es el siguiente paso».

## Checklist de ensayo
- [ ] Cronometrar 15:00 exactos, incluyendo transiciones.
- [ ] Probar staging y las seis pestañas/usuarios de demo.
- [ ] Verificar que el último run de Actions está enlazado.
- [ ] Abrir C4, ADRs, eval-report, SLOs, post-mortem y costos antes de presentar.
- [ ] Deck enviado a los cinco integrantes; cada uno ensaya su bloque.
- [ ] Tener fallback de demo según `docs/demo/guion-demo.md`.
