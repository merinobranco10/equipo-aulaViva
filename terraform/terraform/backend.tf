
terraform {
  backend "s3" {
    bucket       = "REEMPLAZAR-POR-BUCKET-REAL"
    key          = "aulaviva/terraform.tfstate"
    region       = "REEMPLAZAR-POR-REGION-AWS"
    use_lockfile = true
    encrypt      = true
  }
}
