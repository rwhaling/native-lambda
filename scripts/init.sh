#!/bin/sh
set -o nounset

RUNTIME_ARN=$(aws lambda publish-layer-version --layer-name "$RUNTIME_NAME" --zip-file fileb:///build/runtime/runtime.zip | jq -r ".LayerVersionArn")

echo "RUNTIME ARN: $RUNTIME_ARN"

aws lambda create-function --function-name "$FUNCTION_NAME" \
--zip-file fileb://function.zip --handler function.handler --runtime provided \
--role $LAMBDA_ROLE_ARN --layers $RUNTIME_ARN
