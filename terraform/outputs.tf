output "lambda_function_name" {
  description = "Nome da Lambda de autenticacao"
  value       = aws_lambda_function.auth.function_name
}

output "lambda_function_arn" {
  description = "ARN da Lambda de autenticacao"
  value       = aws_lambda_function.auth.arn
}

output "jwt_secret_arn" {
  description = "ARN do segredo utilizado para assinatura JWT"
  value       = aws_secretsmanager_secret.jwt_secret.arn
}

output "lambda_invoke_arn" {
  value = aws_lambda_function.auth.invoke_arn
}