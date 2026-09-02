resource "aws_vpc_endpoint" "secretsmanager" {
  vpc_id            = data.terraform_remote_state.k8s.outputs.vpc_id
  service_name      = "com.amazonaws.us-east-1.secretsmanager"
  vpc_endpoint_type = "Interface"

  subnet_ids = [
    data.terraform_remote_state.k8s.outputs.public_subnet_a_id,
    data.terraform_remote_state.k8s.outputs.public_subnet_b_id
  ]

  security_group_ids = [
    aws_security_group.secretsmanager_endpoint.id
  ]

  private_dns_enabled = true

  tags = {
    Name = "oficina-secretsmanager-endpoint"
  }
}

resource "aws_security_group" "secretsmanager_endpoint" {
  name        = "oficina-secretsmanager-endpoint-sg"
  description = "Permite acesso da Lambda ao Secrets Manager via HTTPS"
  vpc_id      = data.terraform_remote_state.k8s.outputs.vpc_id

  ingress {
    description     = "HTTPS da Lambda"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.lambda.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "oficina-secretsmanager-endpoint-sg"
  }
}