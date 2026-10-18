variable "kubeconfig_path" {
  description = "Ruta al archivo kubeconfig del clúster Kubernetes"
  type        = string
  default     = "~/.kube/config"
}

variable "namespace" {
  description = "Namespace de Kubernetes para AulaViva"
  type        = string
  default     = "equipo-aulaviva"
}
