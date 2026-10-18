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
