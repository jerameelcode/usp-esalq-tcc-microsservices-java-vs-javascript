# Phase 15: Kubernetes Architecture & Configuration Reference

## System Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         KUBERNETES CLUSTER                              │
│                     (Namespace: tcc-microsservices)                      │
└─────────────────────────────────────────────────────────────────────────┘
         │
         ├─────────────────────────────────────────────┐
         │                                             │
         ▼                                             ▼
    ┌─────────────┐                            ┌──────────────┐
    │   INGRESS   │                            │ NETWORK      │
    │  (nginx)    │                            │ POLICIES     │
    └─────────────┘                            └──────────────┘
         │
         ├──────────────────────┬──────────────────────┐
         │                      │                      │
         ▼                      ▼                      ▼
    ┌─────────────┐      ┌──────────────┐      ┌─────────────┐
    │ Spring Boot │      │   NestJS     │      │ PostgreSQL  │
    │   Service   │      │   Service    │      │  StatefulSet│
    └─────────────┘      └──────────────┘      └─────────────┘
         │                      │                      │
    ┌────┴────┐            ┌────┴────┐           ┌────┴────┐
    │ 2-5     │            │ 3-8     │           │ PVC     │
    │ Pods    │            │ Pods    │           │ 10Gi    │
    │ (HPA)   │            │ (HPA)   │           │         │
    └─────────┘            └─────────┘           └─────────┘
         │                      │                      │
         └──────────────────────┴──────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
                ▼                               ▼
           ┌──────────────┐            ┌────────────────┐
           │ Prometheus   │            │  Loki/         │
           │ Scraping     │            │  Fluent Bit    │
           │ & Alerting   │            │  (Logging)     │
           └──────────────┘            └────────────────┘
```

### Component Breakdown

#### 1. Ingress Layer
- **Type**: Nginx Ingress Controller
- **Purpose**: External traffic routing and SSL termination
- **Features**:
  - Path-based routing
  - Host-based routing
  - Rate limiting (100 req/min)
  - CORS support
  - SSL/TLS termination

#### 2. API Services

**Spring Boot API**
- **Framework**: Spring Boot 3.x (Java 17)
- **Replicas**: 2-5 (HPA controlled)
- **Port**: 8080
- **Health Endpoints**:
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
  - `/actuator/prometheus` (metrics)

**NestJS API**
- **Framework**: NestJS (TypeScript/Node.js 20)
- **Replicas**: 3-8 (HPA controlled)
- **Port**: 3000
- **Health Endpoints**:
  - `/health`
  - `/metrics` (Prometheus format)

#### 3. Database Layer
- **Type**: PostgreSQL 16 (Alpine)
- **Replicas**: 1 (StatefulSet)
- **Storage**: 10Gi persistent volume
- **Port**: 5432
- **Features**:
  - Persistent volume claim
  - Headless service for DNS
  - Health probes (pg_isready)
  - Configuration tuning for performance

#### 4. Networking
- **Model**: Kubernetes native networking
- **Network Policies**: Implemented for security
  - Default deny ingress
  - Selective allow for services
  - DNS access for all pods
- **Service Discovery**: Kubernetes DNS
- **Traffic Patterns**:
  - Ingress ↔ APIs (external traffic)
  - APIs ↔ PostgreSQL (internal)
  - APIs ↔ Prometheus (metrics)

#### 5. Monitoring & Observability
- **Metrics**: Prometheus scraping
- **Logging**: Fluent Bit → Loki
- **Alerting**: Prometheus Alert Manager rules
- **Dashboards**: Configurable (Grafana recommended)

## Deployment Configuration Details

### Namespace & RBAC

```yaml
Namespace: tcc-microsservices
ServiceAccount: tcc-deployment-sa
ClusterRole: tcc-pod-reader
Permissions:
  - pods, pods/log (get, list)
  - services (get, list, watch)
  - deployments, statefulsets (get, list, watch)
```

### Spring Boot Deployment Specification

```yaml
Kind: Deployment
Replicas: 2 (min) to 5 (max)

Container:
  Image: openjdk:17-slim-bullseye
  EntryPoint: Maven build + Java runtime
  
  Environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/...
    SPRING_PROFILES_ACTIVE: prod
    JAVA_OPTS: -Xms512m -Xmx1024m
    
  Ports:
    - 8080 (HTTP)
    
  Health Checks:
    Liveness: /actuator/health/liveness (90s delay, 10s interval)
    Readiness: /actuator/health/readiness (60s delay, 5s interval)
    
  Resource Requests:
    CPU: 250m
    Memory: 512Mi
    
  Resource Limits:
    CPU: 500m
    Memory: 1Gi
    
  Database Pool:
    Max Size: 20
    Min Idle: 5
    
  Lifecycle:
    PreStop: 15s sleep (graceful shutdown)
    
  Security:
    runAsNonRoot: true
    runAsUser: 1000
    allowPrivilegeEscalation: false
```

### NestJS Deployment Specification

```yaml
Kind: Deployment
Replicas: 3 (min) to 8 (max)

Container:
  Image: node:20-alpine
  EntryPoint: npm install → npm run build → npm run start:prod
  
  Environment:
    NODE_ENV: production
    LOG_LEVEL: info
    NODE_OPTIONS: --max-old-space-size=512
    
  Ports:
    - 3000 (HTTP)
    
  Health Checks:
    Liveness: /health (60s delay, 10s interval)
    Readiness: /health (30s delay, 5s interval)
    
  Resource Requests:
    CPU: 150m (56% lower than Spring Boot)
    Memory: 256Mi (57.8% lower than Spring Boot)
    
  Resource Limits:
    CPU: 300m
    Memory: 512Mi
    
  Lifecycle:
    PreStop: 10s sleep (graceful shutdown)
    
  Security:
    runAsNonRoot: true
    runAsUser: 1000
    allowPrivilegeEscalation: false
```

### PostgreSQL StatefulSet Specification

```yaml
Kind: StatefulSet
Replicas: 1
ServiceName: postgres (headless service)

Container:
  Image: postgres:16-alpine
  
  Environment:
    POSTGRES_DB: usp-tcc-microsservices
    POSTGRES_USER: postgres
    
  Ports:
    - 5432 (PostgreSQL)
    
  Volume Mounts:
    /var/lib/postgresql/data (persisted via PVC)
    
  Health Checks:
    Liveness: pg_isready (30s delay, 10s interval)
    Readiness: pg_isready (10s delay, 5s interval)
    
  Resource Requests:
    CPU: 100m
    Memory: 256Mi
    
  Resource Limits:
    CPU: 500m
    Memory: 512Mi
    
  Performance Config:
    shared_buffers: 256MB
    effective_cache_size: 1GB
    max_connections: 100
    
  Security:
    fsGroup: 999
    runAsUser: 999
    runAsNonRoot: true
```

## Horizontal Pod Autoscaler Configuration

### Spring Boot HPA

```yaml
Kind: HorizontalPodAutoscaler
Target: springboot-api Deployment
Min Replicas: 2
Max Replicas: 5

Metrics:
  - CPU Utilization: 70%
  - Memory Utilization: 80%

Scale-Up Behavior:
  Period: 15 seconds
  Policies:
    - 100% increase (double replicas)
    - +1 pod increment
  Select: Max (most aggressive)

Scale-Down Behavior:
  Stabilization Window: 300 seconds (5 minutes)
  Period: 60 seconds
  Policy: 50% reduction

Decision Logic:
  Scale Up If:     CPU > 70% OR Memory > 80% (immediate)
  Scale Down If:   CPU < 70% AND Memory < 80% (wait 5 min)
  
Example Scaling:
  2 pods → 3 pods → 5 pods (up)
  5 pods → 3 pods → 2 pods (down)
```

### NestJS HPA

```yaml
Kind: HorizontalPodAutoscaler
Target: nestjs-api Deployment
Min Replicas: 3
Max Replicas: 8

Metrics:
  - CPU Utilization: 65% (more aggressive threshold)
  - Memory Utilization: 75% (more aggressive threshold)

Scale-Up Behavior:
  Period: 10 seconds (faster than Spring Boot)
  Policies:
    - 100% increase (double replicas)
    - +2 pod increment (more pods per scale event)
  Select: Max

Scale-Down Behavior:
  Stabilization Window: 300 seconds
  Period: 60 seconds
  Policy: 50% reduction

Decision Logic:
  Scale Up If:     CPU > 65% OR Memory > 75% (immediate)
  Scale Down If:   CPU < 65% AND Memory < 75% (wait 5 min)
  
Example Scaling:
  3 pods → 5 pods → 7 pods (up, +2 each time)
  7 pods → 5 pods → 3 pods (down, -50% each time)
```

## Network Policy Configuration

### Security Model

```
Default Behavior: DENY ALL INGRESS

Allowed Flows:

1. External → Ingress Controller
   Source: Internet
   Destination: Ingress pods
   Protocol: HTTP/HTTPS
   
2. Ingress → Spring Boot
   Source: ingress-nginx namespace
   Destination: springboot-api pods
   Port: 8080
   
3. Ingress → NestJS
   Source: ingress-nginx namespace
   Destination: nestjs-api pods
   Port: 3000
   
4. Spring Boot/NestJS → PostgreSQL
   Source: springboot-api, nestjs-api pods
   Destination: postgres pods
   Port: 5432
   
5. All Pods → DNS
   Source: All pods
   Destination: kube-system namespace
   Port: UDP 53
```

## Persistent Storage Configuration

### PostgreSQL PVC

```yaml
Kind: PersistentVolumeClaim
Access Mode: ReadWriteOnce
Storage Class: standard
Storage Size: 10Gi

Volume Claim Template:
  Name: postgres-storage
  Mounted At: /var/lib/postgresql/data
  Subpath: postgres

Persistence:
  - Data survives pod restarts
  - Data survives node failures (via storage backend)
  - Backed up separately (not included in manifests)
```

## Monitoring Configuration

### Prometheus Scrape Targets

```yaml
Spring Boot:
  Endpoint: /actuator/prometheus
  Port: 8080
  Interval: 15s
  
NestJS:
  Endpoint: /metrics
  Port: 3000
  Interval: 15s
  
PostgreSQL:
  Endpoint: (requires postgres_exporter)
  Port: 9187
  Interval: 15s
```

### Alert Rules

```yaml
Category: API Performance
  - APIHighErrorRate: Error rate > 5% for 5 minutes
  - APIHighLatency: p95 latency > 1 second for 5 minutes

Category: Resource Utilization
  - PodCPUHigh: CPU > 90% for 5 minutes
  - PodMemoryHigh: Memory > 90% for 5 minutes

Category: Database
  - PostgresConnectionHigh: Active connections > 80
  - PostgresDown: PostgreSQL unreachable for 1 minute

Category: Pod Health
  - PodRestartingFrequently: Restart rate > 0.1/min
```

## Environment Variables by Component

### Spring Boot

```
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/usp-tcc-microsservices
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=[from secret]
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_PROFILES_ACTIVE=prod
SERVER_SERVLET_CONTEXT_PATH=/api
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
JWT_SECRET=[from secret]
JAVA_OPTS=-Xms512m -Xmx1024m
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
```

### NestJS

```
NODE_ENV=production
LOG_LEVEL=info
NODE_OPTIONS=--max-old-space-size=512
PORT=3000
PG_HOST=postgres
PG_PORT=5432
PG_USER=postgres
PG_PASSWORD=[from secret]
PG_DATABASE=usp-tcc-microsservices
DATABASE_URL=[from secret]
JWT_SECRET=[from secret]
```

### PostgreSQL

```
POSTGRES_DB=usp-tcc-microsservices
POSTGRES_USER=postgres
POSTGRES_PASSWORD=[from secret]
POSTGRES_INITDB_ARGS=--encoding=UTF8 --locale=C
PGDATA=/var/lib/postgresql/data/pgdata
```

## Load Distribution

### Current Configuration

```
With 50 Concurrent Users:

Spring Boot:
  Total Replicas: 2-5 (typically 3-4 under load)
  Requests per Pod: ~16-25 req/s
  Total Throughput: ~50-100 req/s

NestJS:
  Total Replicas: 3-8 (typically 5-6 under load)
  Requests per Pod: ~10-20 req/s
  Total Throughput: ~50-120 req/s

Database:
  Connection Pool: ~20-25 total connections
  Active Connections: ~15-18
  Query Concurrency: Handled efficiently
```

## Failure Scenarios & Recovery

### Pod Failure
```
Scenario: springboot-api pod crashes
Action: 
  1. Kubernetes detects pod unhealthy (liveness probe fails)
  2. Immediately restarts pod
  3. Readiness probe confirms pod ready
  4. Traffic routed back to pod
Timeline: ~15 seconds total
```

### Node Failure
```
Scenario: Kubernetes node goes offline
Action:
  1. Kubernetes detects unreachable node (90s timeout)
  2. Evicts pods from node
  3. Reschedules pods on healthy nodes
  4. HPA may scale up if resources tight
Timeline: ~120 seconds total
Status: Potential brief service disruption
Mitigation: Use pod anti-affinity, min replicas > 1
```

### Database Failure
```
Scenario: PostgreSQL pod crashes
Action:
  1. StatefulSet detects pod unhealthy
  2. Restarts postgres pod
  3. Volume reattached automatically
  4. Liveness probe confirms database ready
Timeline: ~30 seconds total
Status: Brief database unavailability
Mitigation: Set up replication (Phase 14)
```

### Network Partition
```
Scenario: Network between API and database fails
Action:
  1. API pods attempt database connection
  2. Connections timeout (readiness probe fails)
  3. Pods marked not ready
  4. Ingress stops routing traffic to pods
Timeline: ~30 seconds total
Status: Service returns errors, HPA scales up
Mitigation: Network policies prevent cascading failures
```

## Performance Optimization Tips

### For High Throughput
```
1. Increase NestJS replicas (lower resource overhead)
2. Increase database connection pool
3. Enable caching (Redis, optional)
4. Optimize database queries (Phase 14)
5. Enable HTTP/2 and compression
```

### For Low Latency
```
1. Ensure pods distributed across nodes
2. Use SSD persistent volumes for database
3. Enable query result caching
4. Configure database connection pooling
5. Tune JVM GC settings (Spring Boot)
```

### For Cost Optimization
```
1. Use NestJS (lower resource requirements)
2. Set appropriate min/max replicas
3. Use spot instances (if cloud provider supports)
4. Optimize database queries
5. Clean up unused resources
```

## Security Best Practices Implemented

✅ **Pod Security**
- Non-root users
- Read-only root filesystem where possible
- Capability dropping
- Security contexts applied

✅ **Network Security**
- Network policies enforced
- Service isolation
- No public database access

✅ **Data Security**
- Secrets management (Kubernetes secrets)
- TLS support (via Ingress)
- Environment variable isolation

✅ **RBAC**
- Minimal permissions
- Service account isolation
- Role-based access control

## Future Enhancements (Phase 14+)

- [ ] PostgreSQL replication (HA)
- [ ] Pod Security Policies
- [ ] Network encryption (mTLS)
- [ ] Secret encryption at rest
- [ ] Backup automation
- [ ] Disaster recovery procedures
- [ ] Advanced monitoring (Grafana dashboards)
- [ ] Cost optimization analysis
- [ ] Multi-region deployment
- [ ] Service mesh integration (Istio/Linkerd)
