resource "aws_secretsmanager_secret" "jwt_secret" {
  name        = "oficina-auth-jwt-secret"
  description = "Chave utilizada para assinatura dos tokens JWT da Oficina Dinoco"

  # LAB temporário: remove o secret imediatamente no destroy
  recovery_window_in_days = 0

  tags = {
    Name = "oficina-auth-jwt-secret"
  }
}