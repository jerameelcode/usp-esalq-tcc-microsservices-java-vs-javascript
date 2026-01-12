# Phase 15: Kubernetes Deployment Testing & Validation

## Pre-Deployment Validation

### Prerequisites Check

```bash
#!/bin/bash
# save as: validate-prerequisites.sh

echo "=== Kubernetes Environment Validation ==="

# Check kubectl version
echo "1. Checking kubectl version..."
kubectl version --client || exit 1

# Check cluster connectivity
echo "2. Checking cluster connectivity..."
kubectl cluster-info || exit 1

# Check node availability
echo "3. Checking available nodes..."
NODES=$(kubectl get nodes -o jsonpath='{.items[].metadata.name}')
NODE_COUNT=$(echo "$NODES" | wc -w)
echo "   Found $NODE_COUNT nodes: $NODES"

if [ $NODE_COUNT -lt 1 ]; then
  echo "ERROR: No nodes available!"
  exit 1
fi

# Check node resources
echo "4. Checking node resources..."
kubectl top nodes || echo "   WARNING: Metrics server not installed"

# Check storage classes
echo "5. Checking storage classes..."
kubectl get storageclass || echo "   WARNING: No storage classes found"

# Check ingress controller (optional)
echo "6. Checking ingress controller..."
kubectl get ingressclass 2>/dev/null || echo "   INFO: Ingress controller not yet installed"

echo ""
echo "✅ Prerequisites validation complete!"
```

### Manifest Validation

```bash
#!/bin/bash
# save as: validate-manifests.sh

echo "=== Kubernetes Manifest Validation ==="

MANIFESTS=(
  "00-namespace-rbac.yaml"
  "01-postgres-statefulset.yaml"
  "02-springboot-deployment.yaml"
  "03-nestjs-deployment.yaml"
  "04-network-policy.yaml"
  "05-ingress.yaml"
  "06-monitoring-logging.yaml"
)

for manifest in "${MANIFESTS[@]}"; do
  echo "Validating $manifest..."
  kubectl apply --dry-run=client -f "$manifest" || exit 1
done

echo ""
echo "✅ All manifests are valid!"
```

## Deployment Validation Steps

### Step 1: Verify Namespace Creation

```bash
# Check namespace exists
kubectl get namespace tcc-microsservices

# Expected output:
# NAME                    STATUS   AGE
# tcc-microsservices      Active   1m

# Verify RBAC
kubectl get serviceaccount -n tcc-microsservices
kubectl get clusterrole tcc-pod-reader
kubectl get clusterrolebinding tcc-pod-reader-binding
```

### Step 2: Verify PostgreSQL Deployment

```bash
# Check StatefulSet
kubectl get statefulset postgres -n tcc-microsservices

# Expected output:
# NAME       READY   AGE
# postgres   1/1     2m

# Check PVC
kubectl get pvc -n tcc-microsservices

# Expected output:
# NAME                     STATUS   VOLUME   CAPACITY
# postgres-storage-pod-0   Bound    ...      10Gi

# Check pod is running
kubectl get pod postgres-0 -n tcc-microsservices

# Expected output:
# NAME       READY   STATUS    RESTARTS   AGE
# postgres-0 1/1     Running   0          2m

# Verify database connectivity
kubectl exec -it postgres-0 -n tcc-microsservices -- \
  psql -U postgres -d usp-tcc-microsservices -c "SELECT version();"

# Expected output:
# PostgreSQL 16.x on x86_64-pc-linux-gnu...
```

### Step 3: Verify Spring Boot Deployment

```bash
# Check deployment
kubectl get deployment springboot-api -n tcc-microsservices

# Expected output:
# NAME             READY   UP-TO-DATE   AVAILABLE   AGE
# springboot-api   2/2     2            2           3m

# Check pods
kubectl get pods -l app=springboot-api -n tcc-microsservices

# Expected output:
# NAME                              READY   STATUS    RESTARTS   AGE
# springboot-api-xxxxxxxx-xxxxx     1/1     Running   0          3m
# springboot-api-xxxxxxxx-xxxxx     1/1     Running   0          3m

# Check service
kubectl get svc springboot-api -n tcc-microsservices

# Expected output:
# NAME             TYPE        CLUSTER-IP     PORT(S)   AGE
# springboot-api   ClusterIP   10.x.x.x       8080/TCP  3m

# Test application health
kubectl port-forward svc/springboot-api 8080:8080 -n tcc-microsservices &
sleep 2
curl http://localhost:8080/actuator/health
kill %1
```

### Step 4: Verify NestJS Deployment

```bash
# Check deployment
kubectl get deployment nestjs-api -n tcc-microsservices

# Expected output:
# NAME         READY   UP-TO-DATE   AVAILABLE   AGE
# nestjs-api   3/3     3            3           3m

# Check pods
kubectl get pods -l app=nestjs-api -n tcc-microsservices

# Expected output:
# NAME                          READY   STATUS    RESTARTS   AGE
# nestjs-api-xxxxxxxx-xxxxx     1/1     Running   0          3m
# nestjs-api-xxxxxxxx-xxxxx     1/1     Running   0          3m
# nestjs-api-xxxxxxxx-xxxxx     1/1     Running   0          3m

# Check service
kubectl get svc nestjs-api -n tcc-microsservices

# Expected output:
# NAME        TYPE        CLUSTER-IP     PORT(S)  AGE
# nestjs-api  ClusterIP   10.x.x.x       3000/TCP 3m

# Test application health
kubectl port-forward svc/nestjs-api 3000:3000 -n tcc-microsservices &
sleep 2
curl http://localhost:3000/health
kill %1
```

### Step 5: Verify Network Policies

```bash
# List network policies
kubectl get networkpolicies -n tcc-microsservices

# Expected output showing all policies created:
# NAME                           POD-SELECTOR   AGE
# default-deny-ingress           <none>         2m
# postgres-allow-from-apps       app=postgres   2m
# springboot-allow-ingress       app=springboot-api 2m
# nestjs-allow-ingress           app=nestjs-api 2m
# allow-dns-egress               <none>         2m
# allow-egress-to-postgres       tier=backend   2m

# Describe a policy to verify rules
kubectl describe networkpolicy postgres-allow-from-apps -n tcc-microsservices
```

### Step 6: Verify HPA Configuration

```bash
# Check HPA status
kubectl get hpa -n tcc-microsservices

# Expected output:
# NAME                    REFERENCE                   TARGETS            MINPODS  MAXPODS  REPLICAS  AGE
# springboot-api-hpa      Deployment/springboot-api   0%/70%, 0%/80%     2        5        2         3m
# nestjs-api-hpa          Deployment/nestjs-api       0%/65%, 0%/75%     3        8        3         3m

# Describe HPA for detailed info
kubectl describe hpa springboot-api-hpa -n tcc-microsservices
kubectl describe hpa nestjs-api-hpa -n tcc-microsservices
```

### Step 7: Verify Ingress Configuration

```bash
# Check ingress resources
kubectl get ingress -n tcc-microsservices

# Expected output:
# NAME                 CLASS   HOSTS                              ADDRESS    PORTS   AGE
# tcc-api-ingress      nginx   springboot.tcc.local...            1.2.3.4    80/443  3m
# tcc-api-ingress-local nginx  localhost                          1.2.3.4    80      3m

# Describe ingress
kubectl describe ingress tcc-api-ingress -n tcc-microsservices
```

## Integration Testing

### Test Database Connectivity

```bash
# From Spring Boot pod
kubectl exec -it $(kubectl get pod -l app=springboot-api -n tcc-microsservices -o jsonpath='{.items[0].metadata.name}') -n tcc-microsservices -- \
  psql -h postgres -U postgres -d usp-tcc-microsservices -c "SELECT COUNT(*) as user_count FROM users;"

# From NestJS pod
kubectl exec -it $(kubectl get pod -l app=nestjs-api -n tcc-microsservices -o jsonpath='{.items[0].metadata.name}') -n tcc-microsservices -- \
  psql -h postgres -U postgres -d usp-tcc-microsservices -c "SELECT COUNT(*) as user_count FROM users;"
```

### Test Service-to-Service Communication

```bash
# From Spring Boot to NestJS
kubectl exec -it $(kubectl get pod -l app=springboot-api -n tcc-microsservices -o jsonpath='{.items[0].metadata.name}') -n tcc-microsservices -- \
  curl -s http://nestjs-api:3000/health | jq

# From NestJS to Spring Boot
kubectl exec -it $(kubectl get pod -l app=nestjs-api -n tcc-microsservices -o jsonpath='{.items[0].metadata.name}') -n tcc-microsservices -- \
  curl -s http://springboot-api:8080/actuator/health | jq
```

### Test API Endpoints

```bash
# Port-forward services
kubectl port-forward svc/springboot-api 8080:8080 -n tcc-microsservices &
kubectl port-forward svc/nestjs-api 3000:3000 -n tcc-microsservices &

# Test Spring Boot registration
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'

# Test NestJS registration
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'

# Cleanup port forwarding
kill %1 %2
```

## Load Testing

### Quick Load Test (5 minutes)

```bash
# Using k6 - test Spring Boot
k6 run -e BASE_URL=http://localhost:8080 \
       -e SETUP_DURATION=1m \
       -e STEADY_STATE_DURATION=3m \
       -e TEARDOWN_DURATION=1m \
       --vus=10 \
       k6/tests/springboot-api.js

# Using k6 - test NestJS
k6 run -e BASE_URL=http://localhost:3000 \
       -e SETUP_DURATION=1m \
       -e STEADY_STATE_DURATION=3m \
       -e TEARDOWN_DURATION=1m \
       --vus=10 \
       k6/tests/nestjs-api.js
```

### Extended Load Test (15 minutes)

```bash
# Spring Boot - gradual ramp up to 50 VUs
k6 run -e BASE_URL=http://localhost:8080 \
       --stage 2m:10 \
       --stage 5m:30 \
       --stage 5m:50 \
       --stage 2m:0 \
       k6/tests/springboot-api.js

# NestJS - gradual ramp up to 100 VUs
k6 run -e BASE_URL=http://localhost:3000 \
       --stage 2m:20 \
       --stage 5m:50 \
       --stage 5m:100 \
       --stage 2m:0 \
       k6/tests/nestjs-api.js
```

### Spike Test (Test HPA Triggers)

```bash
# Spring Boot - sudden spike to 100 VUs
k6 run -e BASE_URL=http://localhost:8080 \
       --stage 1m:0 \
       --stage 1m:100 \
       --stage 2m:100 \
       --stage 1m:0 \
       k6/tests/springboot-api.js

# Monitor HPA during spike:
kubectl get hpa -n tcc-microsservices -w
kubectl top pods -n tcc-microsservices -w
```

## Resource Monitoring

### Real-Time Metrics

```bash
# Watch pods in real-time
kubectl get pods -n tcc-microsservices -w

# View resource usage (requires metrics-server)
kubectl top pods -n tcc-microsservices
kubectl top nodes

# Watch HPA decisions
kubectl get hpa -n tcc-microsservices -w

# Continuous monitoring
watch kubectl get all -n tcc-microsservices
```

### Detailed Diagnostics

```bash
# Pod resource allocation
kubectl get pods -n tcc-microsservices -o custom-columns=\
NAME:.metadata.name,\
CPU_REQ:.spec.containers[0].resources.requests.cpu,\
CPU_LIM:.spec.containers[0].resources.limits.cpu,\
MEM_REQ:.spec.containers[0].resources.requests.memory,\
MEM_LIM:.spec.containers[0].resources.limits.memory

# Pod restart count
kubectl get pods -n tcc-microsservices -o custom-columns=\
NAME:.metadata.name,\
RESTARTS:.status.containerStatuses[0].restartCount,\
STATUS:.status.phase

# Pod events (recent issues)
kubectl get events -n tcc-microsservices --sort-by='.lastTimestamp'
```

## Failure Recovery Testing

### Test Pod Restart

```bash
# Delete a Spring Boot pod
POD=$(kubectl get pod -l app=springboot-api -n tcc-microsservices -o jsonpath='{.items[0].metadata.name}')
kubectl delete pod $POD -n tcc-microsservices

# Verify it restarts
kubectl get pod $POD -n tcc-microsservices -w

# Verify service still responds
curl -s http://localhost:8080/actuator/health
```

### Test Deployment Update

```bash
# Update deployment (e.g., change replica count)
kubectl patch deployment springboot-api -n tcc-microsservices -p '{"spec":{"replicas":4}}'

# Watch rollout
kubectl rollout status deployment/springboot-api -n tcc-microsservices -w

# Verify no service disruption
for i in {1..10}; do
  echo "Request $i:"
  curl -s http://localhost:8080/actuator/health | jq .status
  sleep 1
done
```

### Test Database Failover

```bash
# Delete database pod
kubectl delete pod postgres-0 -n tcc-microsservices

# StatefulSet will recreate it
kubectl get pod postgres-0 -n tcc-microsservices -w

# Verify data persists
kubectl exec -it postgres-0 -n tcc-microsservices -- \
  psql -U postgres -d usp-tcc-microsservices -c "SELECT COUNT(*) FROM users;"
```

## Health Check Validation

### Spring Boot Health Endpoints

```bash
# Liveness probe endpoint
curl http://localhost:8080/actuator/health/liveness | jq
# Expected: {"status":"UP"}

# Readiness probe endpoint
curl http://localhost:8080/actuator/health/readiness | jq
# Expected: {"status":"UP","components":{...}}

# Full health check
curl http://localhost:8080/actuator/health | jq
# Expected: Detailed health information
```

### NestJS Health Endpoint

```bash
# Health endpoint
curl http://localhost:3000/health | jq
# Expected: {"status":"ok"}

# Verify database connectivity via health
curl http://localhost:3000/health | jq '.database'
```

## Comprehensive Validation Checklist

```markdown
## Pre-Deployment
- [ ] kubectl version compatible (v1.24+)
- [ ] Cluster has 4GB+ available RAM
- [ ] All manifests pass dry-run validation
- [ ] Network policies syntax valid
- [ ] Image pull secrets configured (if using private registry)

## Namespace & RBAC
- [ ] Namespace created: tcc-microsservices
- [ ] ServiceAccount created: tcc-deployment-sa
- [ ] ClusterRole created: tcc-pod-reader
- [ ] ClusterRoleBinding created: tcc-pod-reader-binding
- [ ] RBAC permissions verified

## PostgreSQL
- [ ] StatefulSet running: 1/1 ready
- [ ] PVC bound and mounted
- [ ] Pod is Running with 0 restarts
- [ ] Database connectivity tested
- [ ] Data persistence verified
- [ ] Liveness probe passing
- [ ] Readiness probe passing

## Spring Boot
- [ ] Deployment running: 2/2 ready
- [ ] All replicas are Running
- [ ] Service endpoint accessible
- [ ] Pod restart count = 0
- [ ] Health endpoints responding
- [ ] CPU/memory requests met
- [ ] HPA monitoring metrics
- [ ] Database connection pool working
- [ ] Load test passing

## NestJS
- [ ] Deployment running: 3/3 ready
- [ ] All replicas are Running
- [ ] Service endpoint accessible
- [ ] Pod restart count = 0
- [ ] Health endpoint responding
- [ ] CPU/memory requests met
- [ ] HPA monitoring metrics
- [ ] Database connections working
- [ ] Load test passing

## Networking
- [ ] Network policies all created
- [ ] Service-to-service communication working
- [ ] DNS resolution working
- [ ] Ingress configured (if enabled)
- [ ] External access working (if Ingress enabled)

## Monitoring
- [ ] Prometheus ConfigMap created
- [ ] Alert rules ConfigMap created
- [ ] Loki config available
- [ ] Fluent Bit config available
- [ ] Metrics collection working

## Performance
- [ ] Average response time < 500ms
- [ ] p95 response time acceptable
- [ ] Error rate < 2%
- [ ] HPA scaling works correctly
- [ ] Pod affinity working

## Security
- [ ] No privileged containers
- [ ] All containers running as non-root
- [ ] Network policies enforced
- [ ] Secrets not exposed in logs
- [ ] RBAC permissions minimal
```

## Validation Report Template

Save as `VALIDATION_REPORT.md`:

```markdown
# Deployment Validation Report

**Date**: [DATE]
**Kubernetes Cluster**: [CLUSTER_INFO]
**Manifest Version**: Phase 15

## Environment
- Cluster API Version: [VERSION]
- Available Nodes: [COUNT]
- Available CPU: [TOTAL_CPU]
- Available Memory: [TOTAL_MEMORY]

## Deployment Results
- Namespace: ✅ / ❌
- RBAC: ✅ / ❌
- PostgreSQL: ✅ / ❌
- Spring Boot: ✅ / ❌
- NestJS: ✅ / ❌
- Network Policies: ✅ / ❌
- Ingress: ✅ / ❌
- Monitoring: ✅ / ❌

## Performance Baselines (from Phase 12-13)
- Spring Boot Avg Latency: 312ms
- NestJS Avg Latency: 248ms
- Overall Success Rate: 98%+

## Current Performance
- Spring Boot Avg Latency: [MEASURED]
- NestJS Avg Latency: [MEASURED]
- Success Rate: [MEASURED]

## Issues Found
1. [ISSUE 1]
2. [ISSUE 2]

## Recommendations
1. [RECOMMENDATION 1]
2. [RECOMMENDATION 2]

## Sign-Off
- Validated By: [NAME]
- Date: [DATE]
- Status: Ready for Production / Needs Review
```

## Next Steps

After validation passes:
1. Deploy to staging environment
2. Run extended load tests (24+ hours)
3. Verify monitoring and alerting
4. Document any configuration adjustments
5. Create runbooks for common operations
6. Proceed to Phase 14 (Security Hardening)
