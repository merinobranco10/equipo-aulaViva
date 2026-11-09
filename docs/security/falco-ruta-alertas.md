# Falco en AulaViva: reglas y ruta de alertas

## 1. Qué hace Falco aquí

Falco vigila los contenedores del namespace `equipo-aulaviva` y avisa cuando
pasa algo raro. Las reglas están en `falco/rules-custom.yaml` y aplican a los
pods `aulaviva-*`, que incluyen al tutor IA / RAG.

## 2. Las 3 reglas

| Regla | Qué detecta | Nivel | Amenaza relacionada |
|---|---|---|---|
| Shell en contenedor | Alguien abre una terminal (`sh`, `bash`) dentro de un contenedor | Medio (WARNING) | T13 |
| Escritura en /etc | Un proceso intenta cambiar archivos del sistema en `/etc/` | Alto (CRITICAL) | T3 |
| Conexión saliente inesperada | Un pod de AulaViva se conecta a una IP pública en un puerto distinto de 443 | Alto (ERROR) | T8 |

La regla 3 cubre también al tutor IA / RAG, porque maneja datos de estudiantes
y una conexión rara podría ser una fuga de información. Se permite el puerto
443 porque el tutor debe llamar al proveedor del modelo.

## 3. Ruta de las alertas

1. Falco detecta algo raro y escribe la alerta en su registro
   (`kubectl logs -n falco -l app.kubernetes.io/name=falco -c falco`).
2. Se revisa la alerta según su nivel:

| Nivel | A quién llega | Qué se hace |
|---|---|---|
| Medio (WARNING) | Valentina León (DevSecOps) | Revisar el mismo día. Si fue una prueba o un error, se anota y no se abre incidente |
| Alto (ERROR o CRITICAL) | Valentina León (DevSecOps) y Matías Díaz (Tech Lead). Se avisa a Gerardo González (AI/Data) si es del tutor IA, y a Branco Merino (PO) para que esté informado | Se abre un incidente (ver sección 4) |

3. Por ahora las alertas se revisan en los logs de Falco.

Mejora futura: enviar las alertas a Discord con Falcosidekick.

## 4. Qué incidente se abre con una alerta Alta

Se crea un Issue en GitHub con la etiqueta `incident`:

- Título: `[INCIDENTE] <nombre de la regla> - <fecha>`
- Contenido: qué regla saltó, pod y contenedor afectados, hora, y la línea
  completa de la alerta de Falco.
- Responsable: Valentina León. Si es del tutor IA, Gerardo González la apoya.
- Pasos: aislar el pod, revisar qué pasó, corregir y cerrar el Issue con
  una nota de qué se aprendió.

## 5. Evidencias de las pruebas

Las reglas se probaron mediante el workflow automatizado `Falco - Prueba de reglas (S10)`, utilizando un clúster temporal de Kubernetes creado con Kind.

Se utilizó un pod auxiliar `aulaviva-tutor-test` (BusyBox) en el namespace `equipo-aulaviva`.

Las pruebas realizadas fueron:

- `sh -c "ls"` → regla 1: Shell en contenedor.
- `touch /etc/prueba-falco` → regla 2: Escritura en `/etc`.
- `nc -w 2 1.1.1.1 8080` → regla 3: Conexión saliente inesperada.

Las tres reglas generaron alertas correctamente en la ejecución #10 de GitHub Actions.

El workflow guardó las alertas en el resumen de ejecución y generó el artefacto `alertas-falco`.

El backend real no se utilizó para las pruebas porque su imagen distroless no incluye una shell. Por eso se utilizó un pod auxiliar para simular el comportamiento del tutor IA/RAG.
El backend real no se usó para las pruebas porque su imagen no tiene shell
(distroless), por eso se probó con un pod auxiliar.
