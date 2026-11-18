# Índice de Architecture Decision Records (ADRs)

**Última actualización:** 18-11-2026

Este directorio contiene los Architecture Decision Records (ADRs) del proyecto AulaViva. Cada ADR documenta una decisión arquitectónica relevante, su contexto, las alternativas consideradas, la decisión adoptada y sus consecuencias.

## Índice de ADRs

| # | Título | Estado | Fecha |
|---|---|---|---|
| [0001](0001-eleccion-iniciativa.md) | Elección de iniciativa | Aceptada | Pendiente de confirmar |
| [0002](0002-estilo-arquitectonico.md) | Estilo arquitectónico: monolito modular | Aceptada | 04-09-2026 |
| [0003](0003-cloud-style.md) | Proveedor cloud: AWS | Aceptada | 11-09-2026 |
| [0004](0004-datos-y-eventos.md) | Persistencia y mensajería (actualizada 17-11) | Aceptada | 06-10-2026 |
| [0004](0004-plataforma-k8s.md) | Plataforma Kubernetes e IaC | Aceptada | 15-10-2026 |
| [0005](0005-gitops-strategy.md) | Estrategia GitOps | Aceptada | 22-10-2026 |
| [0006](0006-supply-chain.md) | Supply chain: firma y SLSA | Aceptada | 10-11-2026 |
| [0007](0007-estrategia-multi-tenant.md) | Estrategia multi-tenant | Aceptada | 17-11-2026 |
| [0008](0008-identidad-y-rbac.md) | Identidad y RBAC del MVP | Aceptada | 17-11-2026 |
| 0009 | Tutor IA | Pendiente | Día 4 |
| 0010 | Resiliencia y Disaster Recovery (DR) | Pendiente | Día 5 |
| 0011 | FinOps | Pendiente | Día 6 |

## Convenciones

- **Aceptada:** decisión aprobada y vigente en la arquitectura.
- **Propuesta:** decisión pendiente de revisión y aprobación.
- **Pendiente:** ADR planificado, todavía no documentado o aprobado.
- Las decisiones pueden actualizarse mediante nuevas secciones de actualización, conservando el historial de la decisión original.

## Estructura de cada ADR

Cada documento debe incluir:

1. Título, estado, fecha y autor.
2. Contexto y problema que motiva la decisión.
3. Decisión arquitectónica adoptada.
4. Alternativas consideradas, con criterios comparativos explícitos.
5. Consecuencias positivas y negativas.
6. Verificación mediante pruebas o evidencias.
