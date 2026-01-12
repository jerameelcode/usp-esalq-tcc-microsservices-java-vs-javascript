#!/bin/bash

# TCC Microservices Benchmark Runner
# Starts both Spring Boot and NestJS APIs, then runs k6 benchmarks

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Configuration
SPRINGBOOT_PORT=8081
NESTJS_PORT=3000
DB_NAME="usp-tcc-microsservices"
SCENARIO=${1:-smoke}  # Default to smoke test
LOG_DIR="/tmp/tcc-benchmark-$$"

# Create log directory
mkdir -p "$LOG_DIR"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     TCC Microservices Benchmark - Full Test Runner         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to cleanup on exit
cleanup() {
    echo -e "${YELLOW}🧹 Cleaning up processes...${NC}"
    
    # Kill Spring Boot
    lsof -ti:$SPRINGBOOT_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    
    # Kill NestJS
    lsof -ti:$NESTJS_PORT 2>/dev/null | xargs kill -9 2>/dev/null || true
    
    echo -e "${GREEN}✓ Cleanup complete${NC}"
}

# Set trap to cleanup on exit
trap cleanup EXIT

# Function to wait for port to be available
wait_for_port() {
    local port=$1
    local service=$2
    local timeout=120
    local elapsed=0
    
    echo -e "${YELLOW}⏳ Waiting for $service to start on port $port...${NC}"
    
    while ! nc -z localhost $port 2>/dev/null; do
        if [ $elapsed -gt $timeout ]; then
            echo -e "${RED}✗ Timeout waiting for $service${NC}"
            echo "Logs available at: $LOG_DIR/${service}.log"
            return 1
        fi
        sleep 2
        elapsed=$((elapsed + 2))
    done
    
    echo -e "${GREEN}✓ $service is ready${NC}"
    return 0
}

# Function to check if port is in use
check_port_in_use() {
    local port=$1
    if lsof -ti:$port 2>/dev/null | grep -q .; then
        echo -e "${YELLOW}⚠ Port $port is in use. Killing existing process...${NC}"
        lsof -ti:$port | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
}

# Step 1: Database Setup
echo -e "${YELLOW}Step 1: Setting up PostgreSQL database${NC}"
echo "─────────────────────────────────────────"

if psql -l | grep -q $DB_NAME; then
    echo -e "${YELLOW}  Dropping existing database...${NC}"
    dropdb $DB_NAME 2>/dev/null || true
fi

echo -e "${YELLOW}  Creating fresh database...${NC}"
createdb $DB_NAME

echo -e "${GREEN}✓ Database ready${NC}"
echo ""

# Step 2: Start Spring Boot
echo -e "${YELLOW}Step 2: Starting Spring Boot API${NC}"
echo "─────────────────────────────────────────"

check_port_in_use $SPRINGBOOT_PORT

echo -e "${YELLOW}  Building and starting Spring Boot...${NC}"
cd "$PROJECT_ROOT/java/springboot-microsservice"
mvn clean spring-boot:run > "$LOG_DIR/springboot.log" 2>&1 &
SPRINGBOOT_PID=$!
echo "  Process ID: $SPRINGBOOT_PID"

wait_for_port $SPRINGBOOT_PORT "Spring Boot" || exit 1
echo ""

# Step 3: Start NestJS
echo -e "${YELLOW}Step 3: Starting NestJS API${NC}"
echo "─────────────────────────────────────────"

check_port_in_use $NESTJS_PORT

echo -e "${YELLOW}  Building and starting NestJS...${NC}"
cd "$PROJECT_ROOT/javascript"
npm run start > "$LOG_DIR/nestjs.log" 2>&1 &
NESTJS_PID=$!
echo "  Process ID: $NESTJS_PID"

wait_for_port $NESTJS_PORT "NestJS" || exit 1
echo ""

# Step 4: Verify APIs
echo -e "${YELLOW}Step 4: Verifying APIs${NC}"
echo "─────────────────────────────────────────"

if curl -s http://localhost:$SPRINGBOOT_PORT/actuator/health | grep -q "UP"; then
    echo -e "${GREEN}✓ Spring Boot API is healthy${NC}"
else
    echo -e "${RED}✗ Spring Boot API health check failed${NC}"
    exit 1
fi

if curl -s http://localhost:$NESTJS_PORT/health | grep -q "ok"; then
    echo -e "${GREEN}✓ NestJS API is healthy${NC}"
else
    echo -e "${RED}✗ NestJS API health check failed${NC}"
    exit 1
fi
echo ""

# Step 5: Run k6 Benchmarks
echo -e "${YELLOW}Step 5: Running k6 Benchmarks${NC}"
echo "─────────────────────────────────────────"
echo -e "${YELLOW}  Scenario: $SCENARIO${NC}"
echo ""

cd "$PROJECT_ROOT/k6"
SCENARIO=$SCENARIO ./run-test.sh both

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}✓ Benchmark complete!${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Results:${NC}"
ls -lh results/*.txt 2>/dev/null | tail -10 || echo "No results found"
echo ""
echo -e "${YELLOW}Logs:${NC}"
echo "  Spring Boot: $LOG_DIR/springboot.log"
echo "  NestJS: $LOG_DIR/nestjs.log"
echo ""
