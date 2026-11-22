# Costo mensual — AulaViva

**Fecha de la estimación:** 22-11-2026 · **Autor:** Gerardo González (AI/Data)

## 1. Cómo se calculó

- **Infraestructura:** AWS Pricing Calculator, región São Paulo (`sa-east-1`), fecha de la consulta 22-11-2026. Link de la estimación: https://calculator.aws/#/estimate?id=3406fc2d6bbf6ad2ced01d54ba3b61e22df6e66e
- **Servicios usados:** los del [ADR 0003](../adr/0003-cloud-style.md) que sí están en el MVP: Kubernetes (EKS), PostgreSQL (RDS), balanceador (ALB), NAT, S3 (backups, [ADR 0010](../adr/0010-backups-y-recuperacion.md)) y logs (CloudWatch). Mensajería (MSK), identidad (Cognito) y búsqueda vectorial (pgvector) quedaron diferidos el 18-11-2026, por eso no se cobran aquí.
- **LLM:** tokens medidos el Día 4 ([mediciones.md](mediciones.md)) y precios de pago de Groq.

## 2. Infraestructura (AWS, por mes, en USD)

| Componente (según ADR 0003) | Staging | Producción | Supuesto |
|---|---:|---:|---|
| Kubernetes gestionado (EKS) + nodos | 125,10 | 229,29 | EKS: 1 cluster por ambiente (73,00). Nodos t3.medium On-Demand: 1 en staging (52,10) y 3 en producción (156,29). La app arranca con mínimo 2 copias y puede llegar a 5 (`k8s/hpa.yaml`) |
| Base de datos (RDS PostgreSQL) | 31,10 | 254,05 | Staging: db.t4g.micro, 1 zona, 20 GB. Producción: db.t4g.medium, Multi-AZ, 100 GB |
| Balanceador (ALB) | 24,96 | 24,96 | 1 balanceador por ambiente |
| NAT | 68,82 | 145,08 | Staging: 1 NAT, 10 GB. Producción: 2 NAT, 50 GB cada uno |
| Almacenamiento (S3) | 0,43 | 4,07 | Backups de la base: 10 GB en staging y 100 GB en producción |
| Logs (CloudWatch) | 4,53 | 27,18 | 5 GB al mes en staging y 30 GB en producción |
| **Total infraestructura** | **254,94** | **684,63** | |

## 3. Costo del LLM (Tutor IA)

**Precios de Groq para `openai/gpt-oss-120b`:** USD 0,15 por millón de tokens de entrada y USD 0,60 por millón de salida. Fuente: https://groq.com/pricing, consultada el 21-11-2026. (Los 0,75 de salida eran el precio de lanzamiento de agosto 2025, ya no se usan.) La demo usa el plan gratuito; este es el precio si se paga.

**Tokens medidos (Día 4):** por cada pregunta que llega al modelo, unos 540 de entrada y 70 de salida.

**Costo de una pregunta:** (540 × 0,15 + 70 × 0,60) / 1.000.000 = **USD 0,000123**.

**Escenario base:** 5 colegios × 600 estudiantes = **3.000 estudiantes**.

| Supuesto | Valor |
|---|---|
| Preguntas al tutor por estudiante al mes | 40 |
| Tasa de acierto de la caché | 50 % (la medición dio 91 % con preguntas repetidas, que es el mejor caso) |

**Fórmula:** estudiantes × preguntas × costo por pregunta × (1 − acierto de caché)

| | Por colegio | Total (5 colegios) |
|---|---:|---:|
| Preguntas al mes | 24.000 | 120.000 |
| Costo sin caché | USD 2,95 | USD 14,76 |
| **Costo con caché (50 %)** | **USD 1,48** | **USD 7,38** |

## 4. Costo por estudiante y punto de equilibrio

**Costo por estudiante al mes** = (infraestructura staging + infraestructura producción + LLM total) / 3.000

= (254,94 + 684,63 + 7,38) / 3.000 = 946,95 / 3.000 = **USD 0,32**

**Precio por estudiante (supuesto de negocio):** USD 2 al mes. Es un valor bajo y parejo con otros servicios escolares; cada colegio de 600 estudiantes pagaría USD 1.200 al mes.

**Punto de equilibrio (colegios)** = costo fijo mensual / (precio × estudiantes por colegio − costo variable por colegio)

- Costo fijo mensual = infraestructura staging + producción = USD 939,57
- Ingreso por colegio = 2 × 600 = USD 1.200
- Costo variable por colegio (LLM) = USD 1,48

= 939,57 / (1.200 − 1,48) = 939,57 / 1.198,52 = 0,78, es decir, **1 colegio**. Con un solo colegio ya se cubre el costo fijo.

## 5. Las 2 palancas del Día 4, aplicadas a este escenario

| Palanca | Antes | Después | Ahorro al mes | Decisión |
|---|---:|---:|---:|---|
| **1. Caché** (50 % de acierto) | USD 14,76 | USD 7,38 | **USD 7,38** (−50 %; con preguntas repetidas la medición llegó a −92 %) | Se adopta |
| **2. Contexto acotado** (`top-k = 3`) | USD 7,38 | USD 6,05 | USD 1,33 (−18 %) | **No se adopta**: bajó 7 puntos los hechos clave y el límite es 5 |

## 6. Límite diario y presupuesto

- Hoy el límite es 50.000 tokens por colegio al día: alcanza para unas 80 preguntas sin caché.
- Con 600 estudiantes y 40 preguntas al mes, cada colegio necesita unos 665.000 tokens al día sin caché (unos 333.000 con 50 % de caché). Para producción hay que subir `TUTOR_DAILY_TOKEN_BUDGET` (por ejemplo a 400.000). No hace falta cambiar código.
- El plan gratuito de Groq no sirve para producción: permite 1.000 solicitudes al día y 8.000 tokens por minuto por modelo. Producción necesita el plan de pago.

## 7. Etiquetas, medición y alerta

- **Etiquetas:** `project`, `env`, `owner` y `cost-center` (definidas en `terraform/main.tf`). En AWS se activan en **Billing → Cost allocation tags** para filtrar el costo por proyecto y ambiente.
- **Costo del LLM por colegio:** sale de la métrica `tutor.tokens` (por `tenant` y tipo `prompt` o `completion`), porque la infraestructura es compartida. El costo de infraestructura se reparte por número de estudiantes.
- **Presupuesto por colegio:** límite diario en la app (ya implementado, `TutorBudget`).
- **Alerta mensual (diseño):** AWS Budgets con aviso al 80 % y al 100 % del presupuesto mensual. Todavía no está creada.

Decisión completa en el [ADR 0011](../adr/0011-finops.md).
