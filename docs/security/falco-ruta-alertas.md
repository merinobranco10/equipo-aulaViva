# Falco en AulaViva: reglas y ruta de alertas

## 1. Qué hace Falco aquí

Falco vigila los contenedores del namespace `equipo-aulaviva` y avisa cuando
pasa algo raro. Las reglas están en `falco/rules-custom.yaml`.

## 2. Las 3 reglas

| Regla | Qué detecta | Nivel | Amenaza relacionada |
|---|---|---|---|
| Shell abierta dentro de un contenedor | Alguien abre una terminal dentro de un contenedor | Medio (WARNING) | T13 |
| Escritura en /etc dentro de un contenedor | Alguien intenta cambiar archivos del sistema | Alto (CRITICAL) | T3 |
| Tutor IA con conexión saliente inesperada | El tutor se conecta a un puerto que no usa (solo usa DNS, HTTPS y PostgreSQL) | Alto (CRITICAL) | T8 |

La regla 3 está pensada para el tutor IA / RAG porque maneja datos de
estudiantes, y una conexión rara podría ser una fuga de información.

## 3. Ruta de las alertas

1. Falco detecta algo raro y escribe la alerta en su registro
   (`kubectl logs -n falco -l app.kubernetes.io/name=falco -c falco`).
2. Se revisa la alerta según su nivel:

| Nivel | A quién llega | Qué se hace |
|---|---|---|
| Medio (WARNING) | Valentina León (DevSecOps) | Revisar el mismo día. Si fue una prueba o un error, se anota y no se abre incidente |
| Alto (CRITICAL) | Valentina León (DevSecOps) y Matías Díaz (Tech Lead). Se avisa a Gerardo González (AI/Data) si es del tutor IA, y a Branco Merino (PO) para que esté informado | Se abre un incidente (ver sección 4) |

3. El aviso al equipo se hace por Discord, el canal principal del equipo.

## 4. Qué incidente se abre con una alerta Alta

Se crea un Issue en GitHub con la etiqueta `incidente`:

- Título: `[INCIDENTE] <nombre de la regla> - <fecha>`
- Contenido: qué regla saltó, pod y contenedor afectados, hora, y la línea
  completa de la alerta de Falco.
- Responsable: Valentina León. Si es del tutor IA, Gerardo González la apoya.
- Pasos: aislar el pod, revisar qué pasó, corregir y cerrar el Issue con
  una nota de qué se aprendió.

## 5. Evidencias de las pruebas

Las pruebas se hacen automáticamente con GitHub Actions
(`.github/workflows/falco-test.yml`). El robot crea un cluster de prueba,
instala Falco con nuestras reglas y dispara las 3 alertas.

- Corrida del workflow: (pega aquí el link de la corrida)
- Regla 1 (shell): (captura)
- Regla 2 (escritura en /etc): (captura)
- Regla 3 (conexión saliente del tutor IA): (captura)

El backend real no se usó para las pruebas porque no tiene shell y su sistema
de archivos es de solo lectura, así que esos ataques ya se bloquean por
diseño. Se usó un pod de prueba `aulaviva-tutor-test` dentro del cluster
temporal, que desaparece al terminar la corrida.
