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

variable "env" {
  description = "Entorno de despliegue (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "llm_api_key" {
  description = "API Key del proveedor LLM (Groq) para el tutor IA"
  type        = string
  sensitive   = true
}


