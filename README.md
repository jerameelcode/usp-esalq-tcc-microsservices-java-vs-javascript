# usp-esalq-tcc-microsservices-java-vs-javascript
Estudo prático sobre a adoção de microserviços utilizando NestJS (JavaScript/TypeScript) e Spring Boot (Java). O repositório contém implementações equivalentes de microserviços CRUD, configurados com autenticação JWT, integração com PostgreSQL e preparados para execução em containers.


# 📊 Microservices Comparison: NestJS vs Spring Boot

Este repositório apresenta um estudo prático comparando a implementação de **microserviços backend** utilizando dois frameworks modernos e amplamente utilizados:

- **NestJS** (Node.js + TypeScript)  
- **Spring Boot** (Java)  

O objetivo é avaliar **desempenho, escalabilidade, consumo de recursos, segurança** e **experiência de desenvolvimento** em ambos os ambientes, fornecendo subsídios técnicos para desenvolvedores e organizações na escolha da tecnologia mais adequada.

---

## 📌 Contexto do Projeto

A arquitetura de microserviços possibilita a construção de sistemas distribuídos e modulares, nos quais cada serviço é independente e comunicável via APIs.  
Neste estudo foram implementados **dois microserviços equivalentes** para gerenciamento de usuários, com as seguintes funcionalidades:

- Operações CRUD (`Create`, `Read`, `Update`, `Delete`) sobre usuários.  
- Autenticação e autorização utilizando **JWT (JSON Web Token)**.  
- Persistência de dados em **PostgreSQL**.  
- Implantação em **containers Docker** para padronização do ambiente.  

---

## 📂 Estrutura do Repositório

## 📂 Estrutura do Repositório

```
├── nestjs-microservice/        # Implementação em NestJS
│   ├── src/                    # Código-fonte (controllers, services, modules)
│   ├── test/                   # Testes automatizados (Jest)
│   ├── ormconfig.json          # Configuração TypeORM
│   └── Dockerfile              # Containerização
│
├── springboot-microservice/    # Implementação em Spring Boot
│   ├── src/main/java/...       # Código-fonte (controllers, services, repos)
│   ├── src/test/java/...       # Testes automatizados (JUnit)
│   ├── application.properties  # Configuração da aplicação
│   └── Dockerfile              # Containerização
│
└── tests/                      # Scripts de testes de desempenho
    ├── k6-scripts/             # Simulações de carga com k6
    └── apache-bench/           # Exemplos de uso do Apache Benchmark
```


---

## ⚙️ Tecnologias Utilizadas

- **NestJS** (TypeScript, Node.js)  
- **Spring Boot** (Java 17+)  
- **PostgreSQL** (Banco de dados relacional)  
- **Docker & Docker Compose** (Containerização)  
- **JWT (JSON Web Token)** (Autenticação)  
- **Apache Benchmark (ab)** e **k6** (Testes de carga)  

---

## 🛠️ Como Executar

### Pré-requisitos
- [Node.js](https://nodejs.org/) v18+  
- [Java JDK](https://adoptium.net/) 17+  
- [Docker](https://www.docker.com/)  
- [PostgreSQL](https://www.postgresql.org/)  

### 1. Executando o NestJS Microservice
```bash
cd nestjs-microservice
npm install
npm run start:dev
```

### 2. Executando o Spring Boot Microservice
```bash
cd springboot-microservice
./mvnw spring-boot:run
```
### 3. Executando com Docker Compose
```bash
docker-compose up --build
```

## 🔐 Autenticação
Ambos os serviços utilizam JWT para autenticação.

Enviar credenciais de login:
```bash
POST /auth/login
```
Retorno: token JWT.

Usar o token nas rotas protegidas:

```makefile
Authorization: Bearer <token>
```

## 📊 Testes de Desempenho
Apache Benchmark

Exemplo de teste com 1000 requisições e 50 usuários simultâneos:

```bash
ab -n 1000 -c 50 http://localhost:3000/users
````

k6

Exemplo de execução de script de carga:

```bash
k6 run tests/k6-scripts/load-test.js
```

## Resultados esperados:

Tempo de resposta médio (latência).

Throughput (requisições por segundo).

Consumo de recursos (CPU, memória).



## 📸 Exemplos de Execução

Inicialização do NestJS e Spring Boot.

Geração e uso de token JWT.

Resultados de testes de carga (ab e k6).

As capturas de tela estão disponíveis na pasta /docs.