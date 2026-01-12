#!/bin/bash

# Simple k6 benchmark runner
# Usage: ./run-test.sh [springboot|nestjs|both] [smoke|load|stress|spike]

set -e

FRAMEWORK=${1:-both}
SCENARIO=${2:-load}

# Get absolute paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SPRINGBOOT_TEST="$SCRIPT_DIR/tests/springboot-api.js"
NESTJS_TEST="$SCRIPT_DIR/tests/nestjs-api.js"
OUTPUT_DIR="$SCRIPT_DIR/results"

# Create results directory
mkdir -p "$OUTPUT_DIR"

echo "📊 k6 Benchmark Runner"
echo "Framework: $FRAMEWORK"
echo "Scenario: $SCENARIO"
echo "Working directory: $SCRIPT_DIR"
echo ""

# Function to run test
run_test() {
    local name=$1
    local test_file=$2
    
    echo "🚀 Running $name benchmark..."
    echo "Test file: $test_file"
    
    k6 run \
        --env SCENARIO="$SCENARIO" \
        --env SPRINGBOOT_URL="http://localhost:8081" \
        --env NESTJS_URL="http://localhost:3000" \
        "$test_file"
    
    echo "✅ $name benchmark completed"
    echo ""
}

# Run tests
case "$FRAMEWORK" in
    springboot)
        run_test "Spring Boot" "$SPRINGBOOT_TEST"
        ;;
    nestjs)
        run_test "NestJS" "$NESTJS_TEST"
        ;;
    both)
        run_test "Spring Boot" "$SPRINGBOOT_TEST"
        echo "⏸️  Cool-down for 5 seconds..."
        sleep 5
        run_test "NestJS" "$NESTJS_TEST"
        ;;
    *)
        echo "Usage: $0 [springboot|nestjs|both] [smoke|load|stress|spike]"
        exit 1
        ;;
esac

echo "✨ All tests completed!"
