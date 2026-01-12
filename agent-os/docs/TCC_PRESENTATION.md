# Adoção de Microsserviços com JavaScript (NestJS) e Java (Spring Boot)
## Um Estudo Prático

**Jerameel João Gonga**

MBA em Engenharia de Software – USP/Esalq – 2026

---

# Slide 1: Título

## Adoção de Microsserviços com JavaScript (NestJS) e Java (Spring Boot): Um Estudo Prático

**Autor:** Jerameel João Gonga

**Orientador:** João Choma Neto

**Instituição:** MBA em Engenharia de Software – USP/Esalq

**Ano:** 2026

---

# Slide 2: Agenda

1. **Introdução** - Contexto e motivação
2. **Objetivos** - O que buscamos responder
3. **Metodologia** - Como foi feito
4. **Arquitetura** - Estrutura dos microsserviços
5. **Implementação** - Comparativo técnico
6. **Benchmarks** - Resultados dos testes
7. **Análise** - Discussão dos resultados
8. **Conclusões** - Recomendações finais

---

# Slide 3: Introdução

## O Desafio da Escolha Tecnológica

### Contexto
- Arquitetura de microsserviços em crescimento
- Múltiplas opções de frameworks no mercado
- Decisão estratégica com impacto de longo prazo

### O Problema
> "Qual framework escolher para meu projeto de microsserviços: **NestJS** ou **Spring Boot**?"

### Gap Identificado
- Poucos estudos práticos comparativos
- Benchmarks existentes são teóricos ou desatualizados
- Falta de análise com funcionalidades reais (auth, CRUD, etc.)

---

# Slide 4: Objetivos

## Objetivo Geral

Comparar **NestJS** e **Spring Boot** no desenvolvimento de microsserviços backend, com foco em:

| Critério | Descrição |
|----------|-----------|
| 🚀 **Desempenho** | Tempo de resposta e throughput |
| 📈 **Escalabilidade** | Comportamento sob carga |
| 💾 **Consumo de Recursos** | CPU e memória |
| 🔐 **Segurança** | Autenticação JWT |

## Objetivos Específicos

1. Implementar microsserviços **funcionalmente equivalentes**
2. Executar testes de carga **controlados e reproduzíveis**
3. Fornecer **subsídios técnicos** para tomada de decisão

---

# Slide 5: Metodologia

## Abordagem Experimental

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Implementação  │ -> │   Testes de     │ -> │    Análise      │
│  Equivalente    │    │   Carga (k6)    │    │   Comparativa   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Ambiente de Testes
- **Containerização:** Docker + Docker Compose
- **Banco de Dados:** PostgreSQL 16
- **Ferramenta de Carga:** Grafana k6
- **Cenários:** 50, 100 e 200 usuários virtuais (VUs)

### Métricas Coletadas
- Tempo de resposta (avg, median, p95, p99)
- Throughput (requisições/segundo)
- Taxa de erros
- Consumo de recursos

---

# Slide 6: Arquitetura dos Microsserviços

## Estrutura Implementada

```
┌──────────────────────────────────────────────────────────┐
│                      API Gateway                         │
└─────────────────────────┬────────────────────────────────┘
                          │
        ┌─────────────────┴─────────────────┐
        │                                   │
        ▼                                   ▼
┌───────────────────┐             ┌───────────────────┐
│   Spring Boot     │             │     NestJS        │
│   (Java 17)       │             │   (TypeScript)    │
│   Port: 8080      │             │   Port: 3000      │
└────────┬──────────┘             └────────┬──────────┘
         │                                 │
         └─────────────┬───────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   PostgreSQL    │
              │   Port: 5432    │
              └─────────────────┘
```

---

# Slide 7: Funcionalidades Implementadas

## Features Equivalentes em Ambos Frameworks

| Feature | Spring Boot | NestJS |
|---------|-------------|--------|
| **User Entity** | JPA + Hibernate | TypeORM |
| **CRUD REST API** | Spring MVC | Controllers |
| **JWT Auth** | Spring Security + JJWT | Passport.js |
| **Validação** | Bean Validation | class-validator |
| **DTO Mapping** | MapStruct | class-transformer |
| **Paginação** | Pageable | Query params |
| **HATEOAS** | Spring HATEOAS | Custom links |
| **Exception Handler** | @ControllerAdvice | ExceptionFilter |
| **Password Hash** | BCrypt (cost 10) | bcrypt (cost 10) |

---

# Slide 8: Modelo de Dados

## Entidade User - Estrutura Idêntica

```
┌─────────────────────────────────────┐
│              USER                    │
├─────────────────────────────────────┤
│ id         : UUID (auto-generated)  │
│ name       : String (required)      │
│ email      : String (unique)        │
│ password   : String (BCrypt hash)   │
│ role       : Enum (USER, ADMIN)     │
│ status     : Enum (ACTIVE, INACTIVE)│
│ createdAt  : Timestamp (auto)       │
│ updatedAt  : Timestamp (auto)       │
└─────────────────────────────────────┘
```

## Endpoints da API

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | /auth/register | Criar conta | Público |
| POST | /auth/login | Autenticar | Público |
| POST | /auth/refresh | Renovar token | Público |
| GET | /users | Listar (paginado) | JWT |
| GET | /users/:id | Buscar por ID | JWT |
| POST | /users | Criar usuário | JWT |
| PUT | /users/:id | Atualizar | JWT |
| DELETE | /users/:id | Remover | JWT |

---

# Slide 9: Configuração JWT

## Tokens de Autenticação

### Access Token (15 minutos)
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1736648400,
  "exp": 1736649300
}
```

### Refresh Token (7 dias)
```json
{
  "sub": "user-uuid",
  "type": "refresh",
  "iat": 1736648400,
  "exp": 1737253200
}
```

### Configuração Compartilhada
- **Algoritmo:** HS256
- **Secret:** Mesma chave para ambos (JWT_SECRET)
- **BCrypt Cost:** 10 (padrão de segurança)

---

# Slide 10: Cenários de Teste

## Testes de Carga com k6

### Fluxo de Teste (por VU)
```
1. Register (criar novo usuário)
      ↓
2. Login (obter tokens)
      ↓
3. Refresh Token (renovar acesso)
      ↓
4. GET /users (listar usuários - paginado)
      ↓
5. Think Time (simular usuário real)
```

### Níveis de Carga Testados

| Cenário | VUs | Duração | Objetivo |
|---------|-----|---------|----------|
| **Moderado** | 50 | 60s | Carga normal |
| **Alto** | 100 | 60s | Pico de uso |
| **Extremo** | 200 | 60s | Breaking point |

---

# Slide 11: Resultados - Carga Moderada (50 VUs)

## Performance com 50 Usuários Simultâneos

| Métrica | Spring Boot | NestJS | Vencedor |
|---------|-------------|--------|----------|
| **Tempo Médio** | 69.95ms | 52.97ms | 🏆 NestJS (-24%) |
| **Mediana** | 41ms | 28ms | 🏆 NestJS (-32%) |
| **p(95)** | 237.77ms | 144.55ms | 🏆 NestJS (-39%) |
| **Throughput** | 41.77 req/s | 42.59 req/s | 🏆 NestJS (+2%) |
| **Erros** | 0.00% | 0.00% | 🤝 Empate |

### ✅ Ambos Passaram nos Thresholds

```
✓ http_req_duration p(95) < 500ms
✓ http_req_failed < 1%
✓ http_reqs > 10 req/s
```

---

# Slide 12: Resultados - Carga Alta (100 VUs)

## Performance com 100 Usuários Simultâneos

| Métrica | Spring Boot | NestJS | Vencedor |
|---------|-------------|--------|----------|
| **Tempo Médio** | 138.43ms | 112.00ms | 🏆 NestJS (-19%) |
| **Mediana** | 82ms | 68ms | 🏆 NestJS (-17%) |
| **p(95)** | 624.04ms | 583.94ms | 🏆 NestJS (-6%) |
| **Throughput** | 78.91 req/s | 80.83 req/s | 🏆 NestJS (+2%) |
| **Erros** | 0.00% | 0.00% | 🤝 Empate |

### ⚠️ Ambos Cruzaram o Threshold de p(95)

```
✗ http_req_duration p(95) < 500ms  ← Excedido
✓ http_req_failed < 1%
✓ http_reqs > 10 req/s
```

---

# Slide 13: Resultados - Carga Extrema (200 VUs)

## Performance com 200 Usuários Simultâneos

| Métrica | Spring Boot | NestJS | Vencedor |
|---------|-------------|--------|----------|
| **Tempo Médio** | 735.14ms | 628.72ms | 🏆 NestJS (-14%) |
| **Mediana** | 534.31ms | 74.67ms | 🏆 NestJS (-86%) |
| **Máximo** | 4.66s | 7.17s | 🏆 Spring Boot (-35%) |
| **p(95)** | 1.96s | 2.04s | 🏆 Spring Boot (-4%) |
| **Throughput** | 107.4 req/s | 111.2 req/s | 🏆 NestJS (+4%) |
| **Erros** | 0.00% | 0.00% | 🤝 Empate |

### 🔍 Observação Importante
- NestJS: Mediana muito baixa, mas outliers altos
- Spring Boot: Mais consistente em latências de cauda

---

# Slide 14: Gráfico - Throughput vs VUs

## Escalabilidade de Throughput

```
Requisições/segundo
     │
 120 ┤                                    ● NestJS (111.2)
     │                                    ■ Spring Boot (107.4)
 100 ┤
     │
  80 ┤              ● NestJS (80.83)
     │              ■ Spring Boot (78.91)
  60 ┤
     │
  40 ┤  ● NestJS (42.59)
     │  ■ Spring Boot (41.77)
  20 ┤
     │
   0 ┼──────────────┬──────────────┬──────────────
           50 VUs        100 VUs       200 VUs
```

**Conclusão:** NestJS mantém throughput ~4% superior em todos os níveis

---

# Slide 15: Gráfico - Tempo de Resposta vs VUs

## Degradação de Performance

```
Tempo Médio (ms)
     │
 800 ┤                                    ■ Spring Boot (735ms)
     │                                    ● NestJS (629ms)
 600 ┤
     │
 400 ┤
     │
 200 ┤              ■ Spring Boot (138ms)
     │              ● NestJS (112ms)
 100 ┤  ■ Spring Boot (70ms)
     │  ● NestJS (53ms)
   0 ┼──────────────┬──────────────┬──────────────
           50 VUs        100 VUs       200 VUs
```

**Conclusão:** Ambos degradam similarmente, NestJS consistentemente mais rápido

---

# Slide 16: Análise - Autenticação

## Performance do Fluxo de Auth (Login + Register + Refresh)

### Tempo Médio de Autenticação

| VUs | Spring Boot | NestJS | Diferença |
|-----|-------------|--------|-----------|
| 50 | ~90ms | ~65ms | NestJS 28% mais rápido |
| 100 | ~150ms | ~120ms | NestJS 20% mais rápido |
| 200 | ~350ms | ~280ms | NestJS 20% mais rápido |

### Motivos da Diferença
1. **Node.js Event Loop** - Eficiente para I/O bound
2. **JVM Warm-up** - Spring Boot precisa de aquecimento
3. **Spring Security Overhead** - Mais robusto, porém mais pesado

---

# Slide 17: Análise - CRUD Operations

## Performance de Operações de Banco

### Tempo Médio de CRUD (GET /users paginado)

| VUs | Spring Boot | NestJS | Diferença |
|-----|-------------|--------|-----------|
| 50 | ~35ms | ~25ms | NestJS 29% mais rápido |
| 100 | ~60ms | ~45ms | NestJS 25% mais rápido |
| 200 | ~200ms | ~180ms | NestJS 10% mais rápido |

### Observações
- Ambos usam pool de conexões
- Paginação implementada (20 itens/página)
- TypeORM e Hibernate com performance similar
- Diferença reduz sob carga extrema

---

# Slide 18: Análise - Consistência

## Variabilidade nas Respostas (200 VUs)

```
                    Spring Boot          NestJS
                    ───────────         ────────
Mínimo:             2.32ms              2.54ms
Mediana:            534.31ms            74.67ms    ← Grande diferença!
Média:              735.14ms            628.72ms
p(95):              1.96s               2.04s
Máximo:             4.66s               7.17s      ← Spring Boot mais estável
```

### Interpretação
- **NestJS:** Maioria das requests muito rápidas, mas outliers severos
- **Spring Boot:** Distribuição mais uniforme, picos menores

### Implicação Prática
- **NestJS:** Melhor para UX médio, mas pode ter spikes
- **Spring Boot:** Mais previsível para SLAs rigorosos

---

# Slide 19: Confiabilidade

## Taxa de Erros em Todos os Testes

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│     Spring Boot: 0.00% de erros                        │
│     NestJS:      0.00% de erros                        │
│                                                         │
│     ✅ AMBOS SÃO PRODUCTION-READY                      │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Nenhuma Falha Registrada
- 50 VUs: ✅ 0 erros
- 100 VUs: ✅ 0 erros
- 200 VUs: ✅ 0 erros

### Conexões com Banco
- Sem timeouts de conexão
- Pool de conexões funcionando corretamente
- Transações concluídas com sucesso

---

# Slide 20: Comparativo de Ecossistemas

## Stack Tecnológico

| Aspecto | Spring Boot | NestJS |
|---------|-------------|--------|
| **Linguagem** | Java 17 | TypeScript |
| **Runtime** | JVM | Node.js |
| **Gerenciador** | Maven | npm/pnpm |
| **ORM** | Hibernate/JPA | TypeORM |
| **Validação** | Bean Validation | class-validator |
| **Security** | Spring Security | Passport.js |
| **Maturidade** | 10+ anos | 6+ anos |
| **Curva de Aprendizado** | Moderada-Alta | Moderada |
| **Comunidade** | Enterprise | Startup/Web |

---

# Slide 21: Prós e Contras

## Spring Boot (Java)

### ✅ Vantagens
- Ecossistema maduro e estável
- Excelente para sistemas corporativos
- Spring Security muito robusto
- Melhor comportamento em tail latencies
- Grande base de desenvolvedores Java

### ❌ Desvantagens
- JVM warm-up time
- Maior consumo de memória
- Configuração mais verbosa
- Curva de aprendizado inicial

---

# Slide 22: Prós e Contras

## NestJS (TypeScript)

### ✅ Vantagens
- Melhor performance em cargas baixas-médias
- Menor consumo de recursos
- Rápido para desenvolver (TypeScript)
- Event loop eficiente para I/O
- Familiar para devs frontend

### ❌ Desvantagens
- Outliers em cargas extremas
- Ecossistema menos maduro
- Single-threaded (precisa clustering para CPU-bound)
- Menos opções enterprise-ready

---

# Slide 23: Recomendações

## Quando Usar Cada Framework?

### 🎯 Escolha **NestJS** quando:
- Startups e MVPs
- Equipe com background JavaScript/TypeScript
- Microsserviços leves e APIs REST
- Carga esperada: baixa a média (< 100 VUs)
- Time-to-market é prioridade

### 🎯 Escolha **Spring Boot** quando:
- Sistemas corporativos/enterprise
- Requisitos de SLA rigorosos
- Integração com sistemas legados Java
- Carga esperada: alta (> 100 VUs)
- Segurança e compliance críticos

---

# Slide 24: Limitações do Estudo

## Escopo e Restrições

### Ambiente de Teste
- ❗ Executado em ambiente local (não nuvem)
- ❗ Máquina única (sem clustering/load balancing)
- ❗ PostgreSQL compartilhado entre ambos

### Funcionalidades
- ❗ CRUD básico + autenticação
- ❗ Sem cache (Redis)
- ❗ Sem filas de mensagens (Kafka/RabbitMQ)
- ❗ Sem métricas de CPU/memória detalhadas

### Trabalhos Futuros
- Kubernetes com autoscaling
- Testes em cloud (AWS/GCP/Azure)
- Cache distribuído
- Métricas APM (Prometheus/Grafana)

---

# Slide 25: Conclusão

## Principais Descobertas

### 1️⃣ Não Existe "Melhor" Universal
> A escolha depende do **contexto** do projeto

### 2️⃣ NestJS é Mais Rápido em Cargas Moderadas
> 24-39% mais rápido até 50 VUs

### 3️⃣ Spring Boot é Mais Consistente Sob Estresse
> Menores picos de latência em 200 VUs

### 4️⃣ Ambos São Production-Ready
> 0% de erros em todos os cenários

### 5️⃣ A Arquitetura Importa Mais que o Framework
> Paginação, caching, e design correto são cruciais

---

# Slide 26: Resumo Final

## Tabela Comparativa Final

| Critério | Vencedor | Margem |
|----------|----------|--------|
| Performance (50 VUs) | 🏆 NestJS | 24% mais rápido |
| Performance (100 VUs) | 🏆 NestJS | 19% mais rápido |
| Performance (200 VUs) | 🏆 NestJS | 14% mais rápido |
| Consistência | 🏆 Spring Boot | Menos outliers |
| Throughput | 🏆 NestJS | 2-4% maior |
| Confiabilidade | 🤝 Empate | 0% erros |
| Maturidade | 🏆 Spring Boot | 10+ anos |
| Velocidade Dev | 🏆 NestJS | TypeScript |

---

# Slide 27: Obrigado!

## Perguntas?

### Contato
**Jerameel João Gonga**
📧 jerameelcode@gmail.com

### Repositório do Projeto
🔗 github.com/jerameelcode/usp-esalq-tcc-microsservices-java-vs-javascript

### Tecnologias Utilizadas
- Spring Boot 4.0 + Java 17
- NestJS 11 + TypeScript
- PostgreSQL 16
- Docker + k6

---

# Slide 28: Referências

## Bibliografia

1. **Newman, Sam.** Building Microservices. 2nd ed. O'Reilly Media, 2021.

2. **Fowler, Martin.** Microservices: a definition of this new architectural term. 2014.

3. **Richardson, Chris.** Microservices Patterns: With examples in Java. Manning Publications, 2018.

4. **Spring.** Spring Boot Documentation. Pivotal Software, 2023.

5. **NestJS.** NestJS Documentation. https://docs.nestjs.com/

6. **Grafana.** k6 Documentation. https://k6.io/docs/

7. **Microsoft.** Designing secure microservices. 2023.

---

# Apêndice A: Código-fonte

## Estrutura do Projeto

```
usp-esalq-tcc-microsservices/
├── java/springboot-microsservice/
│   ├── src/main/java/.../
│   │   ├── controller/
│   │   ├── service/
│   │   ├── domain/
│   │   └── config/
│   └── Dockerfile
├── javascript/
│   ├── src/
│   │   ├── user/
│   │   ├── auth/
│   │   └── common/
│   └── Dockerfile
├── k6/
│   ├── tests/
│   └── lib/
└── docker-compose.yml
```

---

# Apêndice B: Comandos de Execução

## Como Reproduzir os Testes

```bash
# 1. Clonar repositório
git clone https://github.com/jerameelcode/usp-esalq-tcc-microsservices

# 2. Iniciar containers
docker-compose up -d

# 3. Executar benchmarks
cd k6
./run-benchmarks.sh

# 4. Testes individuais
k6 run --vus 50 --duration 60s tests/springboot-api.js
k6 run --vus 50 --duration 60s tests/nestjs-api.js

# 5. Parar containers
docker-compose down
```

---

*Apresentação gerada em Janeiro de 2026*
*MBA em Engenharia de Software - USP/Esalq*
