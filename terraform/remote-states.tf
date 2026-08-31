# Lambda le informações exportadas pelos outros states:
data "terraform_remote_state" "k8s" {
  backend = "s3"

  config = {
    bucket = "oficina-state-priess951"
    key    = "infra/k8s/terraform.tfstate"
    region = "us-east-1"
  }
}

data "terraform_remote_state" "db" {
  backend = "s3"

  config = {
    bucket = "oficina-state-priess951"
    key    = "infra/db/terraform.tfstate"
    region = "us-east-1"
  }
}