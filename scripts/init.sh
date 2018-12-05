#!/bin/sh
set -o nounset

RUNTIME_CREATE_RESULT=$(aws lambda publish-layer-version --layer-name "$RUNTIME_NAME" --zip-file fileb:///build/runtime/runtime.zip)
echo "$RUNTIME_CREATE_RESULT"
RUNTIME_ARN=$(echo "$RUNTIME_CREATE_RESULT" | jq -r ".LayerVersionArn")
# RUNTIME_ARN=$(aws lambda publish-layer-version --layer-name "$RUNTIME_NAME" --zip-file fileb:///build/runtime/runtime.zip | jq -r ".LayerVersionArn")

echo "RUNTIME ARN: $RUNTIME_ARN"

aws lambda create-function --function-name "$FUNCTION_NAME" \
--zip-file fileb://function.zip --handler function.handler --runtime provided \
--role $LAMBDA_ROLE_ARN --layers $RUNTIME_ARN
