# Oficina Dinoco - Auth Lambda

Function Serverless responsável pelo fluxo de autenticação dos **clientes da Oficina Dinoco**.

Este repositório faz parte da Fase 3 do projeto de pós-graduação em Arquitetura de Software e complementa a aplicação principal `oficina-dinoco`, que continua responsável pelas regras de negócio e pelas rotas administrativas utilizadas por funcionários.

## Requisitos atendidos

A implementação foi criada para atender aos requisitos de autenticação e arquitetura serverless da fase:

- Implementar autenticação do cliente utilizando **CPF**.
- Validar matematicamente o CPF informado.
- Consultar a existência do cliente na base de dados.
- Verificar o status do cliente (`ativo` / `inativo`).
- Gerar e devolver um **JWT válido** para consumo das APIs protegidas.
- Executar esse fluxo por meio de uma **Function Serverless AWS Lambda**.
- Integrar o fluxo com o **AWS API Gateway**.

Os funcionários da oficina continuam utilizando o fluxo já existente na aplicação principal:

```text
Funcionário
   ↓
e-mail + senha
   ↓
Spring Boot
   ↓
JWT de funcionário
```

Já os clientes utilizam:

```text
Cliente
   ↓
CPF
   ↓
API Gateway
   ↓
AWS Lambda
   ↓
validação do CPF
   ↓
consulta PostgreSQL
   ↓
validação do cliente
   ↓
JWT de cliente
```

## Arquitetura

Fluxo atual:

```text
                     Internet
                        |
                        v
                 AWS API Gateway
                        |
                 POST /auth/cliente
                        |
                        v
                oficina-auth-lambda
                  /             \
                 /               \
        Secrets Manager        PostgreSQL RDS
        JWT Secret             tabela cliente
                 \               /
                  \             /
                   v           v
                      JWT
```

A Lambda é executada dentro da mesma VPC utilizada pela infraestrutura da aplicação, permitindo acesso privado ao PostgreSQL.

Como uma Lambda associada à VPC não possui acesso direto à internet, foi criado um **VPC Endpoint para AWS Secrets Manager**, permitindo que a função obtenha os secrets sem utilizar NAT Gateway.

## Fluxo de autenticação do cliente

A autenticação recebe:

```json
{
  "cpf": "52998224725"
}
```

A Lambda executa, resumidamente:

1. Normaliza o CPF.
2. Valida os dígitos verificadores.
3. Consulta o cliente no PostgreSQL.
4. Garante que o registro corresponde a uma pessoa física.
5. Verifica se o cliente está ativo.
6. Obtém a chave JWT no AWS Secrets Manager.
7. Gera o JWT.
8. Retorna o token para o cliente.

## JWT do cliente

O token utiliza assinatura `HMAC256`.

Exemplo de payload:

```json
{
  "iss": "oficina-api",
  "sub": "11",
  "tipo": "CLIENTE",
  "iat": 1788141203,
  "exp": 1788148403
}
```

Principais claims:

- `iss`: emissor do token.
- `sub`: ID interno do cliente.
- `tipo`: identifica o token como pertencente a um cliente.
- `iat`: instante de emissão.
- `exp`: instante de expiração.

O CPF não é armazenado no JWT. Após a autenticação, o cliente é identificado internamente pelo seu `id`.

A aplicação principal pode utilizar o `sub` para verificar se o cliente autenticado possui acesso à ordem de serviço solicitada.

## Segurança

Os dados sensíveis não são armazenados diretamente no código ou no Terraform.

São utilizados dois secrets:

- Secret JWT da aplicação.
- Secret de credenciais do PostgreSQL gerenciado pelo RDS.

A Lambda recebe apenas referências e configurações por variáveis de ambiente:

```text
JWT_SECRET_ARN
DB_SECRET_ARN
DB_HOST
DB_PORT
DB_NAME
```

Os valores reais de senha e chave JWT são obtidos em tempo de execução pelo AWS Secrets Manager.

O Secret JWT é compartilhado entre:

```text
Auth Lambda
   ↓
assina JWT

Spring Boot
   ↓
valida JWT
```

Isso garante compatibilidade entre os tokens emitidos pela função serverless e o Spring Security da aplicação principal.

> Para fins acadêmicos, o CPF é utilizado como mecanismo de autenticação conforme solicitado no requisito. Em um ambiente produtivo, seria recomendado adicionar um segundo fator, como OTP enviado por e-mail ou SMS, pois CPF isoladamente não é uma credencial secreta.

## Principais componentes

```text
src/main/java/com/dinoco/oficina/auth/
├── handler/
│   ├── AuthHandler.java
│   └── ApiGatewayAuthHandler.java
├── model/
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   └── Cliente.java
├── repository/
│   └── ClienteRepository.java
├── security/
│   ├── JwtService.java
│   ├── SecretProvider.java
│   └── DatabaseSecretProvider.java
├── validation/
│   └── CpfValidator.java
└── exception/
```

Responsabilidades principais:

- `ApiGatewayAuthHandler`: adapta a requisição HTTP recebida pelo API Gateway.
- `AuthHandler`: coordena o fluxo de autenticação.
- `CpfValidator`: normaliza e valida o CPF.
- `ClienteRepository`: consulta o PostgreSQL via JDBC.
- `DatabaseSecretProvider`: obtém as credenciais do banco no Secrets Manager.
- `SecretProvider`: obtém a chave de assinatura JWT.
- `JwtService`: gera o JWT do cliente.

## Tecnologias

- Java 21
- AWS Lambda
- AWS API Gateway HTTP API
- AWS Secrets Manager
- AWS VPC
- AWS PrivateLink / VPC Endpoint
- PostgreSQL / Amazon RDS
- Terraform
- Maven
- Auth0 Java JWT
- JDBC PostgreSQL
- JUnit 5
- Mockito

## Infraestrutura Terraform

A infraestrutura específica da Lambda está em:

```text
terraform/
```

O Terraform é responsável por recursos como:

- AWS Lambda.
- Security Group da Lambda.
- Secret utilizado para assinatura JWT.
- VPC Endpoint para Secrets Manager.
- Associação da Lambda às subnets da VPC.
- Variáveis de ambiente necessárias para conexão com RDS e Secrets Manager.

O state utiliza o backend S3:

```text
infra/auth-lambda/terraform.tfstate
```

A infraestrutura lê outputs de outros states para reutilizar a VPC, subnets e informações do banco, sem recriar esses recursos.

## API Gateway

O API Gateway é gerenciado separadamente no repositório `oficina-infra-k8s`.

Ele funciona como porta de entrada da solução e decide o destino de acordo com a rota.

Exemplo:

```text
POST /auth/cliente
        ↓
Auth Lambda

ANY /api/*
        ↓
LoadBalancer
        ↓
Spring Boot / EKS
```

Assim, a Lambda de autenticação só é executada quando necessária.

## Build

Na raiz do projeto:

```bash
mvn clean test
mvn clean package
```

O projeto utiliza Maven Shade Plugin para gerar um JAR com as dependências necessárias para execução na AWS Lambda.

Arquivo gerado:

```text
target/oficina-auth-lambda-1.0.0.jar
```

## Deploy com Terraform

Configure primeiro as credenciais do AWS LAB.

Exemplo no PowerShell:

```powershell
$env:AWS_PROFILE="pos"
```

Depois:

```bash
cd terraform

terraform fmt
terraform init
terraform validate
terraform plan
terraform apply
```

## Teste pelo API Gateway

Exemplo:

```http
POST /auth/cliente
Content-Type: application/json
```

Body:

```json
{
  "cpf": "52998224725"
}
```

Para um cliente existente e ativo, a resposta esperada é:

```json
{
  "token": "eyJ..."
}
```

## Respostas esperadas

Fluxos tratados:

```text
CPF inválido         → HTTP 400
Cliente inexistente  → HTTP 401
Cliente inativo      → HTTP 403
Sucesso              → HTTP 200 + JWT
Erro interno         → HTTP 500
```

## Observabilidade

A Lambda utiliza logs para facilitar diagnóstico e monitoramento.

Devem ser registrados eventos como:

- início da autenticação;
- CPF validado;
- cliente localizado;
- cliente inativo;
- autenticação concluída;
- JWT gerado;
- falhas de acesso ao banco;
- falhas de acesso ao Secrets Manager;
- erros internos.

Dados sensíveis não devem ser registrados nos logs, especialmente:

- CPF completo;
- JWT;
- senha do banco;
- chave de assinatura JWT.

Os logs são disponibilizados no AWS CloudWatch e poderão posteriormente ser integrados ao **New Relic** para dashboards, consultas, métricas e alertas.

## Integração com a aplicação principal

O repositório `oficina-dinoco` continua responsável pela API Spring Boot.

Funcionários:

```text
e-mail + senha
      ↓
Spring Boot
      ↓
JWT funcionário
```

Clientes:

```text
CPF
 ↓
Auth Lambda
 ↓
JWT CLIENTE
 ↓
API Spring Boot
```

As rotas utilizadas pelo cliente deverão validar:

1. se o JWT é válido;
2. se o token é do tipo `CLIENTE`;
3. qual é o `clienteId` presente no `sub`;
4. se o recurso solicitado realmente pertence ao cliente autenticado.

Exemplo:

```text
JWT
sub = 11
tipo = CLIENTE
        ↓
GET ordem de serviço
        ↓
OS pertence ao cliente 11?
     /             \
   SIM             NÃO
    ↓               ↓
  permite          403
```

Essa verificação representa a autorização do recurso e impede que um cliente acesse informações pertencentes a outro cliente.
