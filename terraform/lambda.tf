data "aws_caller_identity" "current" {}

locals {
  prefix = "oficina-auth"

  lab_role_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/LabRole"
}

resource "aws_lambda_function" "auth" {
  function_name = "${local.prefix}-lambda"

  role    = local.lab_role_arn
  runtime = "java21"

  # Indica que quando essa Lambda for invocada, deve ser executado o arquivo JAR que está no S3
  handler = "com.dinoco.oficina.auth.handler.ApiGatewayAuthHandler::handleRequest"

  filename = "../target/oficina-auth-lambda-1.0.0.jar"

  # Indica que o arquivo JAR está no S3 e deve ser baixado para execução, usa isso para identificar mudancas no arquivo JAR e atualizar a Lambda
  source_code_hash = filebase64sha256(
    "../target/oficina-auth-lambda-1.0.0.jar"
  )

  timeout     = 10
  memory_size = 512

  # A configuração VPC de uma Lambda recebe subnets e Security Groups
  vpc_config {
    subnet_ids = [
      data.terraform_remote_state.k8s.outputs.public_subnet_a_id,
      data.terraform_remote_state.k8s.outputs.public_subnet_b_id
    ]

    security_group_ids = [
      aws_security_group.lambda.id
    ]
  }

  environment {
    variables = {
      JWT_SECRET_ARN = aws_secretsmanager_secret.jwt_secret.arn

      DB_HOST       = local.db_host
      DB_PORT       = local.db_port
      DB_NAME       = data.terraform_remote_state.db.outputs.database_name
      DB_SECRET_ARN = data.terraform_remote_state.db.outputs.database_secret_arn
    }
  }

  tags = {
    Name = "${local.prefix}-lambda"
  }
}