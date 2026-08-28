# Impact Map — AulaViva

## Goal 
Modernizar la experiencia de aprendizaje de un grupo de colegios de la Región
Metropolitana mediante una plataforma SaaS multi-tenant con evaluaciones
automáticas y un tutor IA contextualizado al currículum vigente del MINEDUC,
reduciendo la carga laboral en los docentes y mejorando el acompañamiento académico,
sin comprometer el aislamiento de datos entre colegios ni la privacidad de los
menores.

## Actores clave
- **Docente**
- **Estudiante**

*Actores adicionales mapeados para contexto completo del negocio:*

Coordinador académico · Apoderado · Sostenedor.

## Mapa Actor → Impact → Deliverable

### 1. Docente
**Impact:** Deja de invertir horas corrigiendo evaluaciones manualmente y gana
visibilidad real del avance de su curso.
**Deliverables:**
- Motor de evaluaciones auto-corregidas con feedback.
- Panel de gestión de cursos y estudiantes con RBAC.

### 2. Estudiante
**Impact:** Recibe apoyo de estudio inmediato y personalizado, sin salirse del
currículum de su curso.
**Deliverables:**
- Tutor IA con RAG sobre los apuntes de cada curso.
- Evaluaciones auto-corregidas con feedback instantáneo.

### 3. Coordinador académico
**Impact:** Verifica que el contenido y las respuestas del tutor IA respeten
el currículum MINEDUC a través de múltiples tenants.
**Deliverables:**
- Panel de administración multi-tenant.
- RBAC de gestión de cursos / docentes / estudiantes.

### 4. Apoderado
**Impact:** Sigue el avance académico de su hijo/a en tiempo real y otorga
consentimiento informado sobre el uso de sus datos.
**Deliverables:**
- Panel del apoderado con seguimiento de avance.

### 5. Sostenedor
**Impact:** Escala la plataforma a múltiples colegios controlando costos de
LLM y garantizando aislamiento de datos entre tenants.
**Deliverables:**
- Arquitectura multi-tenant (1 colegio = 1 tenant aislado lógicamente).

## Regla de oro aplicada
Cada deliverable listado traza una línea directa a un impacto medible en un
actor concreto, y ese impacto contribuye al goal. Se descartaron entregables
sin trazabilidad clara en esta etapa por no tener un impacto directo justificable hoy.
