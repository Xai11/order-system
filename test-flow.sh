#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8081"
ANALYTICS_URL="http://localhost:8084"

echo "=== Posting 10 orders ==="
for i in $(seq 1 10); do
  AMOUNT=$(( RANDOM % 451 + 50 ))
  RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
    -H "Content-Type: application/json" \
    -d "{\"userId\":\"user-$i\",\"amount\":$AMOUNT}")
  ORDER_ID=$(echo "$RESPONSE" | grep -o '"orderId":"[^"]*"' | cut -d'"' -f4)
  echo "Order $i: orderId=$ORDER_ID amount=$AMOUNT"
done

echo ""
echo "=== Waiting 8 seconds for processing ==="
sleep 8

echo ""
echo "=== Analytics Summary ==="
SUMMARY=$(curl -s "$ANALYTICS_URL/analytics/summary")
echo "$SUMMARY"

echo ""
echo "=== Validation ==="
PAID=$(echo "$SUMMARY" | grep -o '"PAID":[0-9]*' | cut -d: -f2)
FAILED=$(echo "$SUMMARY" | grep -o '"FAILED":[0-9]*' | cut -d: -f2)

PAID=${PAID:-0}
FAILED=${FAILED:-0}

echo "PAID count:   $PAID"
echo "FAILED count: $FAILED"

if [ "$PAID" -gt 0 ] && [ "$FAILED" -gt 0 ]; then
  echo "SUCCESS: Both PAID and FAILED events observed."
  exit 0
else
  echo "WARNING: Expected both PAID > 0 and FAILED > 0. Got PAID=$PAID FAILED=$FAILED"
  echo "(This can happen by chance — 10 orders x 80% success. Re-run to confirm.)"
  exit 1
fi
