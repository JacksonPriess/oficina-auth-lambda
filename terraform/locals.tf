locals {
  postgres_endpoint_parts = split(
    ":",
    data.terraform_remote_state.db.outputs.postgres_endpoint
  )

  db_host = local.postgres_endpoint_parts[0]
  db_port = local.postgres_endpoint_parts[1]
}