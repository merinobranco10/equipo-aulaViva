terraform {
  backend "kubernetes" {
    secret_suffix = "aulaviva"
    config_path   = "~/.kube/config"
    namespace     = "default"
  }
}
