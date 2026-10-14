# Validación QA — Despliegue en Kubernetes

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 14 de octubre de 2026
**Sesión:** S07
**Alcance:** Manifiestos `k8s/` aplicados sobre clúster Kubernetes

## 1. Objetivo

Validar que el despliegue del backend de AulaViva en Kubernetes funciona correctamente: pods en ejecución, service accesible, HPA configurado y health checks activos.

## 2. Comandos ejecutados

```bash
kubectl apply -k ./k8s
kubectl get all -n equipo-aulaviva
kubectl describe deployment aulaviva-backend -n equipo-aulaviva
kubectl get hpa -n equipo-aulaviva
```

## 3. Resultado de la validación

| Criterio | Resultado |
|---|---|
| Namespace creado | ✅ `equipo-aulaviva` |
| Pods en ejecución | ✅ 2/2 pods en estado `Running` |
| Pods Ready | ✅ `1/1` en cada pod |
| Deployment disponible | ✅ `2/2` réplicas |
| Service ClusterIP | ✅ Puerto 80 → targetPort `http` |
| HPA configurado | ✅ Min 2 / Max 5 réplicas |
| Liveness probe | ✅ `/actuator/health/liveness` |
| Readiness probe | ✅ `/actuator/health/readiness` |
| Resource limits | ✅ CPU 500m / Memoria 512Mi |
| Resource requests | ✅ CPU 100m / Memoria 256Mi |
| RollingUpdate strategy | ✅ Max unavailable 25% / Max surge 25% |
| ConfigMap montado | ✅ `aulaviva-config` |

## 4. Evidencia

- `docs/qa/evidence/deployment-apply.png`
- `docs/qa/evidence/deployment-get-all.png`
- `docs/qa/evidence/deployment-describe.png`
- `docs/qa/evidence/deployment-describe-2.png`
- `docs/qa/evidence/deployment-hpa.png`

## 5. Hallazgos

**H-01 — HPA sin métricas activas en entorno local**

El HPA muestra `<unknown>/60%` en el target de CPU y `<unknown>/500m` en la métrica de latencia. Esto ocurre porque el clúster de pruebas local (kind/minikube) no tiene `metrics-server` instalado. En un clúster de producción (EKS) con metrics-server activo, las métricas se reportarán correctamente.

**Impacto:** bajo. No afecta el funcionamiento del despliegue.
**Acción:** validar HPA en entorno EKS durante S08.

**H-02 — Segunda métrica del HPA requiere adaptador externo**

La métrica `http_request_duration_p95_seconds` requiere un adaptador de métricas personalizadas (Prometheus Adapter o KEDA). No está activa en el entorno local.

**Impacto:** medio para producción. Requiere configuración adicional antes del despliegue definitivo.
**Acción:** documentar en ADR de observabilidad o en S08.

## 6. Conclusión

El despliegue del backend AulaViva en Kubernetes fue **exitoso**. La corrección previa del namespace en Terraform (de `aulaviva` a `equipo-aulaviva`) fue clave para que el despliegue funcionara sin errores.

Los hallazgos identificados (H-01, H-02) están relacionados con el entorno local de pruebas y no con la configuración de los manifiestos en sí.

**Validado por:** Alexander Ruiz-Tagle — QA Lead
