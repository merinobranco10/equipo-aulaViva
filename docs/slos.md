# Definición de SLOs (Service Level Objectives)

1. **Disponibilidad (Availability):** 99.9% de las peticiones exitosas (HTTP 2xx) durante una ventana de 30 días.
2. **Latencia (Latency):** El percentil 95 (p95) de las peticiones debe ser menor a 300ms.
3. **Tasa de Errores (Error Rate):** Menos del 1% de las peticiones deben devolver errores 5xx.
4. **Saturación (Saturation):** El uso de CPU del pod principal no debe superar el 80% de media durante picos de carga.
