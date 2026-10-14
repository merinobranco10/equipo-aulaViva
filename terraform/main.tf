
terraform {
  required_version = ">= 1.10.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.38"
    }
  }
}

provider "kubernetes" {
  config_path = var.kubeconfig_path
}

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

resource "kubernetes_namespace_v1" "aulaviva" {
  metadata {
    name = var.namespace
  }
}

resource "kubernetes_secret_v1" "app_secrets_placeholder" {
  metadata {
    name      = "aulaviva-app-secrets"
    namespace = kubernetes_namespace_v1.aulaviva.metadata[0].name
  }

  type = "Opaque"

  data = {
    PLACEHOLDER = "replace-with-secret-manager-reference"
  }

  lifecycle {
    ignore_changes = [data]
  }
}

