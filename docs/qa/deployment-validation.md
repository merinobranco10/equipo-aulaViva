# Validación QA — Despliegue en Kubernetes

**Responsable:** Alexander Ruiz-Tagle — QA Lead
**Fecha:** 14 de octubre de 2026
**Sesión:** S07
**Alcance:** Manifiestos `k8s/` aplicados sobre clúster Kubernetes

## 1. Objetivo

Validar que el despliegue del backend de AulaViva en Kubernetes funciona
correctamente: pods en ejecución, service accesible, HPA configurado y
health checks activos.

## 2. Comandos ejecutados

```bash
kubectl apply -k ./k8s
kubectl get all -n equipo-aulaviva
kubectl describe deployment aulaviva-backend -n equipo-aulaviva
kubectl get hpa -n equipo-aulaviva
