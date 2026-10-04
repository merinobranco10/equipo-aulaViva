# Catálogo de Eventos de Dominio — AulaViva

## 1. Propósito

Este documento define el catálogo de eventos de dominio utilizados para la comunicación entre los bounded contexts de AulaViva.

Los eventos representan hechos ocurridos dentro del dominio y permiten desacoplar los distintos contextos mediante comunicación asíncrona utilizando Kafka/MSK como broker de eventos.

Los eventos definidos consideran los siguientes bounded contexts:

- Gestión Académica
- Evaluaciones y Progreso
- Tutor IA y Contenidos
- Identidad y Acceso

---

## 2. Convenciones generales

Todos los eventos deben cumplir las siguientes convenciones:

- El nombre del evento representa un hecho que ya ocurrió.
- Los nombres utilizan el formato `recurso.acción.pasado`.
- Cada evento posee una versión.
- La versión inicial de los eventos es `1.0`.
- Los eventos incluyen identificadores para garantizar trazabilidad.
- La entrega de eventos utiliza un modelo **at-least-once**.
- El consumidor debe implementar procesamiento idempotente utilizando `event_id`.
- `tenant_id` permite identificar el tenant al que pertenece el evento.
- `occurred_at` indica cuándo ocurrió el hecho en el dominio.
- `trace_id` permite relacionar eventos pertenecientes a una misma operación o flujo distribuido.
- `aggregate_id` identifica el agregado principal relacionado con el evento.

---

## 3. Schema general de los eventos

Todos los eventos de AulaViva utilizan un envelope común:

```json
{
  "event_id": "uuid",
  "event_type": "evaluation.submitted",
  "event_version": "1.0",
  "occurred_at": "2026-10-04T15:30:00Z",
  "aggregate_id": "uuid",
  "tenant_id": "uuid",
  "trace_id": "uuid",
  "data": {}
}

3. Catálogo de eventos
