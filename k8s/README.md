# Phase 15: Kubernetes Deployment Manifests

## Overview

This directory contains production-ready Kubernetes deployment manifests for the TCC Microservices project. The manifests deploy both Spring Boot (Java) and NestJS (TypeScript) implementations alongside PostgreSQL, with complete support for scaling, monitoring, and security.

**Project Status**: Phase 15 - Kubernetes Deployment Manifests (Part of 93% complete phases 1-13)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                       │
├─────────────────────────────────────────────────────────────┤
│  Namespace: tcc-microsservices                              │
│                                                             │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │   Ingress (nginx)│  │ NetworkPolicies  │                │
│  └────────┬─────────┘  └──────────────────┘                │
│           │                                                 │
│     ┌─────┴─────────────────────────────────┐              │
│     │                                       │              │
│  ┌──▼────────────────┐        ┌──────────────▼──┐          │
│  │ Spring Boot (HPA) │        │ NestJS (HPA)   │          │
│  │ 2-5 replicas     │        │ 3-8 replicas   │          │
│  │ 250m CPU/512Mi   │        │ 150m CPU/256Mi │          │
│  └──┬────────────────┘        └──────────┬─────┘          │
│     │                                    │                 │
│     │        ┌────────────────────┐     │                 │
│     │        │   PostgreSQL       │     │                 │
│     │        │   StatefulSet      │     │                 │
│     │        │   1 replica        │     │                 │
│     │        │   10Gi persistence │     │                 │
│     └────────┼────────────────────┼─────┘                 │
│              │                    │                       │
│              └────────────────────┘                       │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Monitoring & Logging                            │   │
│  │  • Prometheus Scraping                           │   │
│  │  • Alert Rules                                   │   │
│  │  • Loki Log Aggregation                          │   │
│  │  • Fluent Bit Forwarding                         │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

## File Structure

| File | Purpose |
|------|---------|
| `00-namespace-rbac.yaml` | Namespace, ServiceAccount, RBAC roles & bindings |
| `01-postgres-statefulset.yaml` | PostgreSQL database with persistent storage |
| `02-springboot-deployment.yaml` | Spring Boot API deployment with HPA |
| `03-nestjs-deployment.yaml` | NestJS API deployment with HPA |
| `04-network-policy.yaml` | Network policies for security |
| `05-ingress.yaml` | Ingress controller configuration |
| `06-monitoring-logging.yaml` | Prometheus, Loki, and Fluent Bit configs |
| `README.md` | This file |

## Deployment Instructions

### Prerequisites

- Kubernetes cluster (v1.24+)
- `kubectl` configured with cluster access
- Nginx Ingress Controller (optional, for external access)
- At least 4GB RAM available

### Step 1: Create Namespace and RBAC

```bash
kubectl apply -f 00-namespace-rbac.yaml
```

**Verification**:
```bash
kubectl get namespace tcc-microsservices
kubectl get serviceaccount -n tcc-microsservices
```

### Step 2: Deploy PostgreSQL

```bash
kubectl apply -f 01-postgres-statefulset.yaml
```

**Verification**:
```bash
kubectl get statefulset postgres -n tcc-microsservices
kubectl get pvc -n tcc-microsservices
```

Wait for PostgreSQL to be ready:
```bash
kubectl wait --for=condition=ready pod -l app=postgres -n tcc-microsservices --timeout=300s
```

### Step 3: Deploy Spring Boot API

```bash
kubectl apply -f 02-springboot-deployment.yaml
```

**Verification**:
```bash
kubectl get deployment springboot-api -n tcc-microsservices
kubectl get hpa springboot-api-hpa -n tcc-microsservices
```

Wait for Spring Boot to be ready:
```bash
kubectl wait --for=condition=available deployment/springboot-api -n tcc-microsservices --timeout=600s
```

### Step 4: Deploy NestJS API

```bash
kubectl apply -f 03-nestjs-deployment.yaml
```

**Verification**:
```bash
kubectl get deployment nestjs-api -n tcc-microsservices
kubectl get hpa nestjs-api-hpa -n tcc-microsservices
```

Wait for NestJS to be ready:
```bash
kubectl wait --for=condition=available deployment/nestjs-api -n tcc-microsservices --timeout=300s
```

### Step 5: Apply Network Policies

```bash
kubectl apply -f 04-network-policy.yaml
```

**Verification**:
```bash
kubectl get networkpolicies -n tcc-microsservices
```

### Step 6: Configure Ingress

```bash
kubectl apply -f 05-ingress.yaml
```

**Verification**:
```bash
kubectl get ingress -n tcc-microsservices
kubectl get ingressclass
```

### Step 7: Deploy Monitoring and Logging

```bash
kubectl apply -f 06-monitoring-logging.yaml
```

**Verification**:
```bash
kubectl get configmap -n tcc-microsservices | grep -E "prometheus|loki|fluent"
```

### Deploy All at Once

```bash
kubectl apply -f k8s/
```

## Configuration Reference

### Spring Boot Deployment

**Replicas**: 2-5 (controlled by HPA)
- **Min Replicas**: 2 (always maintain 2 instances for HA)
- **Max Replicas**: 5 (scale out under high load)

**Resource Requests**:
- CPU: 250m
- Memory: 512Mi

**Resource Limits**:
- CPU: 500m
- Memory: 1Gi

**Scaling Policy**:
- Scale up: 100% increase or +1 pod every 15 seconds
- Scale down: 50% reduction every 60 seconds
- Triggers: CPU > 70% or Memory > 80%

**Database Connection Pool**:
- Maximum Pool Size: 20
- Minimum Idle: 5

### NestJS Deployment

**Replicas**: 3-8 (controlled by HPA)
- **Min Replicas**: 3 (optimized for redundancy)
- **Max Replicas**: 8 (scales more aggressively due to lower footprint)

**Resource Requests**:
- CPU: 150m (56% lower than Spring Boot per benchmark)
- Memory: 256Mi (57.8% lower than Spring Boot per benchmark)

**Resource Limits**:
- CPU: 300m
- Memory: 512Mi

**Scaling Policy**:
- Scale up: 100% increase or +2 pods every 10 seconds (more aggressive)
- Scale down: 50% reduction every 60 seconds
- Triggers: CPU > 65% or Memory > 75%

**Node Memory Setting**:
- NODE_OPTIONS: `--max-old-space-size=512`

### PostgreSQL StatefulSet

**Replicas**: 1 (single instance)
**Storage**: 10Gi persistent volume

**Performance Tuning**:
- `shared_buffers`: 256MB
- `effective_cache_size`: 1GB
- `max_connections`: 100
- `wal_level`: replica

**Note**: For production HA, upgrade to 3-replica PostgreSQL with streaming replication.

## Performance Characteristics

Based on load testing (Phase 12-13):

| Metric | Spring Boot | NestJS |
|--------|-------------|--------|
| Avg Response Time | 312ms | 248ms |
| p(95) Response Time | 528ms | 462ms |
| CPU Usage (Peak) | 45.2% | 19.8% |
| Memory Usage (Peak) | 912 MB | 385 MB |
| Requests/sec | 4.62 | 5.01 |
| Success Rate | 98.2% | 98.7% |

**Deployment Recommendation**: Both services handle 50 concurrent users successfully. NestJS is recommended for resource-constrained environments.

## Networking

### Service Discovery

Services communicate within the cluster using Kubernetes DNS:
- Spring Boot: `springboot-api.tcc-microsservices.svc.cluster.local:8080`
- NestJS: `nestjs-api.tcc-microsservices.svc.cluster.local:3000`
- PostgreSQL: `postgres.tcc-microsservices.svc.cluster.local:5432`

### External Access

**Ingress Routes** (if Ingress controller is installed):
- `http://springboot.tcc.local` → Spring Boot API
- `http://nestjs.tcc.local` → NestJS API
- `http://api.tcc.local/springboot` → Spring Boot API
- `http://api.tcc.local/nestjs` → NestJS API

**Local Development**:
```bash
kubectl port-forward svc/springboot-api 8080:8080 -n tcc-microsservices
kubectl port-forward svc/nestjs-api 3000:3000 -n tcc-microsservices
kubectl port-forward svc/postgres 5432:5432 -n tcc-microsservices
```

### Network Policies

Security policies implemented:
- **Default Deny**: All ingress traffic blocked by default
- **PostgreSQL**: Only accepts connections from springboot-api and nestjs-api pods
- **APIs**: Accept traffic from Nginx Ingress Controller
- **DNS**: All pods can resolve DNS queries
- **Egress**: Apps can reach PostgreSQL and DNS

## Monitoring and Observability

### Prometheus Metrics

The deployment includes Prometheus scrape configuration for:
- **Spring Boot**: `/actuator/prometheus` endpoint
- **NestJS**: `/metrics` endpoint
- **PostgreSQL**: Via postgres_exporter

**Alert Rules**:
- High error rate (> 5% for 5 minutes)
- High latency (p95 > 1 second for 5 minutes)
- High CPU usage (> 90% for 5 minutes)
- High memory usage (> 90% for 5 minutes)
- PostgreSQL connection count > 80
- PostgreSQL down
- Pod restarting frequently (> 0.1 restarts/min)

### Log Aggregation

Logs are forwarded via Fluent Bit to Loki for centralized aggregation.

**Access logs**:
```bash
kubectl logs -f deployment/springboot-api -n tcc-microsservices
kubectl logs -f deployment/nestjs-api -n tcc-microsservices
kubectl logs -f statefulset/postgres -n tcc-microsservices
```

## Scaling Behavior

### Manual Scaling

```bash
# Scale Spring Boot
kubectl scale deployment springboot-api --replicas=4 -n tcc-microsservices

# Scale NestJS
kubectl scale deployment nestjs-api --replicas=6 -n tcc-microsservices
```

### Autoscaling

Horizontal Pod Autoscaling (HPA) is configured and will automatically:
1. Monitor CPU and memory utilization
2. Scale up when thresholds are exceeded
3. Scale down during low traffic periods

View HPA status:
```bash
kubectl get hpa -n tcc-microsservices
kubectl describe hpa springboot-api-hpa -n tcc-microsservices
kubectl describe hpa nestjs-api-hpa -n tcc-microsservices
```

## Troubleshooting

### Pod not starting

```bash
# Check pod status
kubectl get pods -n tcc-microsservices

# Describe the pod
kubectl describe pod <pod-name> -n tcc-microsservices

# View logs
kubectl logs <pod-name> -n tcc-microsservices
```

### Database connection issues

```bash
# Check if PostgreSQL is ready
kubectl get pod postgres-0 -n tcc-microsservices
kubectl logs postgres-0 -n tcc-microsservices

# Test connectivity from an app pod
kubectl exec -it <springboot-pod-name> -n tcc-microsservices -- \
  psql -h postgres -U postgres -d usp-tcc-microsservices -c "SELECT version();"
```

### Ingress not working

```bash
# Check ingress status
kubectl get ingress -n tcc-microsservices
kubectl describe ingress tcc-api-ingress -n tcc-microsservices

# Ensure ingress controller is running
kubectl get pods -n ingress-nginx
```

### High memory usage

```bash
# Check resource usage
kubectl top pods -n tcc-microsservices
kubectl top nodes

# Check for OOMKilled containers
kubectl describe pod <pod-name> -n tcc-microsservices
```

## Security Considerations

1. **Secrets Management**: Update credentials in `Secret` objects
2. **Network Policies**: Review and customize based on your needs
3. **RBAC**: Service account has minimal required permissions
4. **Container Security**: Non-root users, read-only filesystems where applicable
5. **Resource Limits**: Prevents resource exhaustion attacks
6. **TLS**: Configure certificates for production (cert-manager integration)

## Production Hardening (Phase 14 - Future)

The following items should be addressed in Phase 14:

- [ ] Configure certificate management (cert-manager)
- [ ] Implement Pod Security Policies
- [ ] Set up log encryption at rest
- [ ] Enable audit logging
- [ ] Configure network encryption (mTLS via Istio/Linkerd)
- [ ] Set up backup and disaster recovery
- [ ] Implement rate limiting and request throttling
- [ ] Configure resource quotas and limits
- [ ] Enable security scanning in CI/CD
- [ ] Implement secrets encryption at rest

## Cleanup

To remove all resources:

```bash
kubectl delete namespace tcc-microsservices
```

This will delete all resources in the namespace.

## References

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
- [Spring Boot on Kubernetes](https://spring.io/blog/2021/01/04/demystifying-kubernetes)
- [NestJS Deployment](https://docs.nestjs.com/deployment)
- [PostgreSQL on Kubernetes](https://www.postgresql.org/docs/)

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-12 | Initial Phase 15 deployment manifests |

## Support

For issues or questions, refer to the main project documentation in `agent-os/specs/`.
