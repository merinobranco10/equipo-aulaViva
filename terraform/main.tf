locals {
  common_labels = {
    project       = "aulaviva"
    env           = var.env
    owner         = "equipo-aulaviva"
    "cost-center" = "taller-ingsw"
  }
}

resource "kubernetes_namespace_v1" "aulaviva" {
  metadata {
    name   = var.namespace
    labels = local.common_labels
  }
}

resource "kubernetes_secret_v1" "app_secrets" {
  metadata {
    name      = "aulaviva-app-secrets"
    namespace = kubernetes_namespace_v1.aulaviva.metadata[0].name
    labels    = local.common_labels
  }

  type = "Opaque"

  data = {
    LLM_API_KEY = var.llm_api_key
  }
}
