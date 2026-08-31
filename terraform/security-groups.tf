resource "aws_security_group" "lambda" {
  name        = "oficina-auth-lambda-sg"
  description = "Security Group da Lambda de autenticacao"
  vpc_id      = data.terraform_remote_state.k8s.outputs.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "oficina-auth-lambda-sg"
  }
}