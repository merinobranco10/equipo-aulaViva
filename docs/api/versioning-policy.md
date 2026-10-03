# Política de versionado y compatibilidad de la API

## 1. Objetivo

Este documento define la política de versionado, compatibilidad y deprecación de la API de AulaViva, con el objetivo de mantener una evolución controlada de la API y evitar cambios incompatibles inesperados para sus consumidores.

## 2. Versionado

AulaViva utiliza **versionado mayor mediante la URL**.

Las versiones se identifican mediante el prefijo `/vN`, por ejemplo:

```text
/v1/evaluations
/v1/students/{studentId}/progress
/v1/tutor/questions
```

La versión `v1` corresponde a la primera versión estable de la API definida para el MVP.

El campo `info.version` del documento OpenAPI (`1.0.0`) representa la versión del contrato documentado, mientras que `/v1` identifica la versión mayor de la API.

### 2.1. Cambios compatibles

Los siguientes cambios pueden realizarse dentro de una misma versión mayor sin crear una nueva versión de la API:

* Agregar nuevos endpoints.
* Agregar nuevos campos opcionales en las respuestas.
* Agregar nuevos parámetros opcionales.
* Agregar nuevos recursos o funcionalidades.
* Corregir errores que no modifiquen el comportamiento esperado de los consumidores.
* Agregar nuevos valores únicamente cuando el contrato existente permita su extensión sin afectar a los consumidores.

Estos cambios deben mantener la compatibilidad con los consumidores existentes.

### 2.2. Cambios incompatibles

Los siguientes cambios requieren una nueva versión mayor:

* Eliminar un endpoint existente.
* Cambiar el método HTTP de un endpoint.
* Cambiar la estructura de una respuesta de forma incompatible.
* Eliminar o renombrar campos existentes.
* Cambiar un campo de opcional a obligatorio.
* Modificar el tipo de un campo existente.
* Cambiar el significado de un campo de manera incompatible.
* Modificar las reglas de autenticación o autorización de forma que impidan el funcionamiento de consumidores existentes.

Por ejemplo, un cambio incompatible de `/v1/evaluations` se implementaría mediante una nueva versión:

```text
/v2/evaluations
```

La versión anterior podrá mantenerse temporalmente durante el período de transición definido para los consumidores existentes.

## 3. Política de compatibilidad

AulaViva seguirá el principio de **compatibilidad hacia atrás dentro de una misma versión mayor**.

Esto significa que un consumidor que utilice correctamente `/v1` no debería dejar de funcionar debido a cambios compatibles realizados posteriormente en `/v1`.

Los cambios incompatibles no se introducirán directamente en una versión mayor existente. En su lugar, se publicará una nueva versión mayor y se documentarán las diferencias entre ambas versiones.

El contrato definido para una versión mayor se mantendrá compatible durante su ciclo de vida, salvo las modificaciones explícitamente contempladas como compatibles en esta política.

## 4. Política de deprecación

Una funcionalidad o endpoint podrá ser marcado como **deprecated** cuando exista una alternativa que deba utilizarse en su reemplazo o cuando se planifique su eliminación.

La deprecación seguirá las siguientes reglas:

1. Se indicará explícitamente que el recurso está deprecado en la documentación de la API.
2. Se informará cuál es la alternativa recomendada, cuando exista.
3. Se mantendrá la funcionalidad durante un período de transición para permitir la migración de los consumidores existentes.
4. No se eliminará inmediatamente una funcionalidad que haya sido marcada como deprecada.
5. La eliminación de una funcionalidad deprecada se realizará como un cambio incompatible y, cuando corresponda, mediante una nueva versión mayor.

Por ejemplo, si un endpoint de `/v1` fuera reemplazado por una nueva operación, el endpoint original podría permanecer disponible y documentarse como deprecado mientras los consumidores migran a la alternativa.

## 5. Criterio para nuevas versiones

Se creará una nueva versión mayor (`v2`, `v3`, etc.) cuando los cambios requeridos no puedan realizarse manteniendo la compatibilidad hacia atrás.

Las versiones mayores deberán documentar:

* Cambios respecto de la versión anterior;
* Endpoints modificados, agregados o eliminados;
* Cambios en los esquemas de solicitud y respuesta;
* Funcionalidades deprecadas;
* Período de transición, cuando corresponda.

De esta forma, la evolución de la API de AulaViva se realizará de manera controlada, manteniendo la compatibilidad de los consumidores dentro de cada versión mayor.
