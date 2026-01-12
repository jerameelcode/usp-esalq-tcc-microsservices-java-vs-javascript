# Phase 15 Deployment Quick Reference

## Quick Start (3 Minutes)

### Single Command Deployment

```bash
# Deploy everything
kubectl apply -f k8s/

# Verify deployment
kubectl get all -n tcc-microsservices
```

### Step-by-Step (Recommended)

```bash
# 1. Create namespace and RBAC
kubectl apply -f k8s/00-namespace-rbac.yaml

# 2. Deploy PostgreSQL
kubectl apply -f k8s/01-postgres-statefulset.yaml

# Wait for PostgreSQL
kubectl wait --for=condition=ready pod -l app=postgres -n tcc-microsservices --timeout=300s

# 3. Deploy Spring Boot
kubectl apply -f k8s/02-springboot-deployment.yaml

# Wait for Spring Boot
kubectl wait --for=condition=available deployment/springboot-api -n tcc-microsservices --timeout=600s

# 4. Deploy NestJS
kubectl apply -f k8s/03-nestjs-deployment.yaml

# Wait for NestJS
kubectl wait --for=condition=available deployment/nestjs-api -n tcc-microsservices --timeout=300s

# 5. Enable Network Policies
kubectl apply -f k8s/04-network-policy.yaml

# 6. Configure Ingress
kubectl apply -f k8s/05-ingress.yaml

# 7. Deploy Monitoring
kubectl apply -f k8s/06-monitoring-logging.yaml
```

## Verification Checklist

```bash
# Check namespace
kubectl get ns tcc-microsservices

# Check services are running
kubectl get pods -n tcc-microsservices
# Expected output:
# NAME                              READY   STATUS    RESTARTS   AGE
# nestjs-api-xxxxxxxxxx-xxxxx       1/1     Running   0          2m
# nestjs-api-xxxxxxxxxx-xxxxx       1/1     Running   0          2m
# nestjs-api-xxxxxxxxxx-xxxxx       1/1     Running   0          2m
# postgres-0                        1/1     Running   0          5m
# springboot-api-xxxxxxxxxx-xxxxx   1/1     Running   0          3m
# springboot-api-xxxxxxxxxx-xxxxx   1/1     Running   0          3m

# Check deployments
kubectl get deployments -n tcc-microsservices
# Expected: springboot-api and nestjs-api both READY 2/2 and 3/3

# Check services
kubectl get svc -n tcc-microsservices
# Expected: postgres, springboot-api, nestjs-api services

# Check HPA status
kubectl get hpa -n tcc-microsservices
# Expected: Both HPAs REFERENCE OK and TARGET showing CPU/memory %

# Check ingress
kubectl get ingress -n tcc-microsservices
```

## Access Applications

### Using kubectl port-forward

```bash
# Spring Boot (http://localhost:8080)
kubectl port-forward svc/springboot-api 8080:8080 -n tcc-microsservices

# NestJS (http://localhost:3000)
kubectl port-forward svc/nestjs-api 3000:3000 -n tcc-microsservices

# PostgreSQL (localhost:5432)
kubectl port-forward svc/postgres 5432:5432 -n tcc-microsservices
```

### Using Ingress (if configured)

```bash
# Add to /etc/hosts:
# 127.0.0.1 springboot.tcc.local nestjs.tcc.local api.tcc.local

# Access via:
# http://springboot.tcc.local
# http://nestjs.tcc.local
# http://api.tcc.local/springboot
# http://api.tcc.local/nestjs
```

## Common Commands

### Monitor Deployment Progress

```bash
# Real-time pod watching
kubectl get pods -n tcc-microsservices -w

# View pod logs
kubectl logs -f deployment/springboot-api -n tcc-microsservices
kubectl logs -f deployment/nestjs-api -n tcc-microsservices
kubectl logs -f statefulset/postgres -n tcc-microsservices

# View last 100 lines
kubectl logs -n tcc-microsservices --tail=100 deployment/nestjs-api
```

### Check Resource Usage

```bash
# Current resource utilization
kubectl top pods -n tcc-microsservices
kubectl top nodes

# Describe pod for detailed info
kubectl describe pod <pod-name> -n tcc-microsservices

# Check events
kubectl get events -n tcc-microsservices --sort-by='.lastTimestamp'
```

### Scale Services

```bash
# Manual scaling
kubectl scale deployment springboot-api --replicas=3 -n tcc-microsservices
kubectl scale deployment nestjs-api --replicas=5 -n tcc-microsservices

# Check HPA scaling decisions
kubectl get hpa -n tcc-microsservices -w
kubectl describe hpa springboot-api-hpa -n tcc-microsservices
```

### Database Management

```bash
# Connect to PostgreSQL
kubectl exec -it postgres-0 -n tcc-microsservices -- psql -U postgres

# Run SQL commands
kubectl exec postgres-0 -n tcc-microsservices -- psql -U postgres -d usp-tcc-microsservices -c "SELECT version();"

# View database size
kubectl exec postgres-0 -n tcc-microsservices -- psql -U postgres -d usp-tcc-microsservices -c "\l+"

# Backup database
kubectl exec postgres-0 -n tcc-microsservices -- pg_dump -U postgres usp-tcc-microsservices > backup.sql

# Restore database
kubectl exec -i postgres-0 -n tcc-microsservices -- psql -U postgres usp-tcc-microsservices < backup.sql
```

### Health Checks

```bash
# Check service health
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:3000/health

# Check database connectivity
kubectl exec -it springboot-api-<pod> -n tcc-microsservices -- \
  curl -s http://localhost:8080/actuator/health/readiness | jq
```

## Load Testing (Using k6)

```bash
# Run load tests against deployed services
k6 run k6/tests/springboot-api.js --vus=10 --duration=30s

k6 run k6/tests/nestjs-api.js --vus=10 --duration=30s

# With custom target
k6 run k6/tests/springboot-api.js -e BASE_URL=http://localhost:8080 --vus=50 --duration=5m
```

## Troubleshooting

### Pod stuck in Pending

```bash
kubectl describe pod <pod-name> -n tcc-microsservices

# Check node capacity
kubectl top nodes
kubectl describe nodes
```

### Pod CrashLoopBackOff

```bash
# Check logs
kubectl logs <pod-name> -n tcc-microsservices
kubectl logs <pod-name> -n tcc-microsservices --previous

# Check resource requests
kubectl get pod <pod-name> -n tcc-microsservices -o yaml | grep -A5 resources
```

### Database connection refused

```bash
# Check if PostgreSQL pod is running
kubectl get pod postgres-0 -n tcc-microsservices

# Check PostgreSQL logs
kubectl logs postgres-0 -n tcc-microsservices

# Test connectivity from app pod
kubectl exec -it <app-pod> -n tcc-microsservices -- \
  nc -zv postgres 5432
```

### High memory usage

```bash
# View memory usage
kubectl top pods -n tcc-microsservices --sort-by=memory

# Check for memory leaks
kubectl describe pod <pod-name> -n tcc-microsservices
# Look for "OOMKilled" in status

# View memory limits
kubectl get pod <pod-name> -n tcc-microsservices -o yaml | grep -A3 limits
```

### Ingress not working

```bash
# Check ingress status
kubectl get ingress -n tcc-microsservices -o yaml

# Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# Verify DNS resolution
nslookup springboot.tcc.local
```

## Performance Tuning

### Increase replica count for high load

```bash
# Update min/max replicas
kubectl patch hpa springboot-api-hpa -n tcc-microsservices -p '{"spec":{"minReplicas":3,"maxReplicas":10}}'
kubectl patch hpa nestjs-api-hpa -n tcc-microsservices -p '{"spec":{"minReplicas":4,"maxReplicas":12}}'
```

### Adjust resource limits

```bash
# Edit deployment
kubectl edit deployment springboot-api -n tcc-microsservices

# Find and update:
# resources:
#   requests:
#     memory: "512Mi"
#     cpu: "250m"
#   limits:
#     memory: "1Gi"
#     cpu: "500m"
```

### Tune database connection pool

Edit `02-springboot-deployment.yaml`:
```yaml
env:
- name: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE
  value: "30"  # Increase from 20
- name: SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE
  value: "10"  # Increase from 5
```

Then redeploy:
```bash
kubectl apply -f k8s/02-springboot-deployment.yaml
```

## Cleanup

### Remove specific components

```bash
# Remove monitoring
kubectl delete -f k8s/06-monitoring-logging.yaml

# Remove ingress
kubectl delete -f k8s/05-ingress.yaml

# Remove APIs
kubectl delete deployment springboot-api nestjs-api -n tcc-microsservices

# Remove database
kubectl delete statefulset postgres -n tcc-microsservices

# Remove namespace (deletes everything)
kubectl delete namespace tcc-microsservices
```

## Key Metrics to Monitor

### Application Metrics

- **Response Time**: p50, p95, p99 (Target: < 300ms, < 500ms, < 1000ms)
- **Error Rate**: % failed requests (Target: < 1%)
- **Requests/sec**: Throughput (Target: > 4.6 req/s)
- **Success Rate**: % successful requests (Target: > 98%)

### Infrastructure Metrics

- **CPU Usage**: Per pod (Target: < 70%)
- **Memory Usage**: Per pod (Target: < 80%)
- **Network I/O**: Bytes sent/received
- **Disk I/O**: For database operations

### Kubernetes Metrics

- **Pod Restart Count**: Should be 0
- **Pod Pending Time**: Should be < 30s
- **Node Pressure**: Memory/Disk pressure should be False
- **API Latency**: < 100ms

## Performance Baselines (From Phase 12-13 Testing)

| Service | Metric | Baseline |
|---------|--------|----------|
| Spring Boot | Avg Latency | 312ms |
| Spring Boot | p95 Latency | 528ms |
| Spring Boot | Peak CPU | 45.2% |
| Spring Boot | Peak Memory | 912 MB |
| NestJS | Avg Latency | 248ms |
| NestJS | p95 Latency | 462ms |
| NestJS | Peak CPU | 19.8% |
| NestJS | Peak Memory | 385 MB |

## Next Steps (Phase 14 - Security Hardening)

- [ ] Configure TLS certificates
- [ ] Implement Pod Security Policies
- [ ] Set up audit logging
- [ ] Configure network encryption
- [ ] Enable secret encryption at rest
- [ ] Implement backup/restore procedures
- [ ] Set up disaster recovery
