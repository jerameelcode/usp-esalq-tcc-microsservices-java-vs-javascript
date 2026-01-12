#!/bin/bash

# k6 Benchmark Runner for TCC Microservices Comparison
# Runs load tests against both Spring Boot and NestJS APIs

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
SCENARIO=${SCENARIO:-"load"}
SPRINGBOOT_URL=${SPRINGBOOT_URL:-"http://localhost:8080"}
NESTJS_URL=${NESTJS_URL:-"http://localhost:3000"}
OUTPUT_DIR="./results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Create results directory
mkdir -p "$OUTPUT_DIR"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║       TCC Microservices Benchmark - k6 Load Tests          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Configuration:${NC}"
echo -e "  Scenario:     ${GREEN}$SCENARIO${NC}"
echo -e "  Spring Boot:  ${GREEN}$SPRINGBOOT_URL${NC}"
echo -e "  NestJS:       ${GREEN}$NESTJS_URL${NC}"
echo -e "  Output:       ${GREEN}$OUTPUT_DIR${NC}"
echo ""

# Function to run test
run_test() {
    local framework=$1
    local test_file=$2
    local base_url=$3
    
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}Running $framework benchmark...${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    local output_json="$OUTPUT_DIR/${framework}_${SCENARIO}_${TIMESTAMP}.json"
    local output_summary="$OUTPUT_DIR/${framework}_${SCENARIO}_${TIMESTAMP}_summary.txt"
    
    # Run k6 test (with Docker volume mount support)
    k6 run \
        --env SCENARIO="$SCENARIO" \
        --env SPRINGBOOT_URL="$SPRINGBOOT_URL" \
        --env NESTJS_URL="$NESTJS_URL" \
        --out json="$output_json" \
        --summary-trend-stats="avg,min,med,max,p(90),p(95),p(99)" \
        "$test_file" 2>&1 | tee "$output_summary"
    
    # Alternative: If running from Docker, use absolute path
    # docker run -v $(pwd):/scripts grafana/k6 run \
    #     --env SCENARIO="$SCENARIO" \
    #     --env SPRINGBOOT_URL="$SPRINGBOOT_URL" \
    #     --env NESTJS_URL="$NESTJS_URL" \
    #     --out json="$output_json" \
    #     --summary-trend-stats="avg,min,med,max,p(90),p(95),p(99)" \
    #     "/scripts/$test_file"
    
    echo -e "${GREEN}✓ $framework benchmark completed${NC}"
    echo -e "  Results: $output_json"
    echo ""
}

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}Error: k6 is not installed${NC}"
    echo "Install k6: https://k6.io/docs/getting-started/installation/"
    exit 1
fi

# Parse command line arguments
case "$1" in
    springboot)
        run_test "springboot" "./tests/springboot-api.js" "$SPRINGBOOT_URL"
        ;;
    nestjs)
        run_test "nestjs" "./tests/nestjs-api.js" "$NESTJS_URL"
        ;;
    both|all|"")
        run_test "springboot" "./tests/springboot-api.js" "$SPRINGBOOT_URL"
        sleep 5  # Cool-down period between tests
        run_test "nestjs" "./tests/nestjs-api.js" "$NESTJS_URL"
        ;;
    *)
        echo -e "${RED}Usage: $0 [springboot|nestjs|both]${NC}"
        echo ""
        echo "Environment variables:"
        echo "  SCENARIO        - Test scenario: smoke, load, stress, spike (default: load)"
        echo "  SPRINGBOOT_URL  - Spring Boot API URL (default: http://localhost:8080)"
        echo "  NESTJS_URL      - NestJS API URL (default: http://localhost:3000)"
        exit 1
        ;;
esac

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}All benchmarks completed!${NC}"
echo -e "Results saved to: ${YELLOW}$OUTPUT_DIR${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
