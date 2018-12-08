#!/bin/bash
set -o nounset

echo "Updating runtime"
RUNTIME_CREATE_RESULT=$(aws lambda publish-layer-version --layer-name "$RUNTIME_NAME" --zip-file fileb:///build/runtime/runtime.zip)
echo "$RUNTIME_CREATE_RESULT"
RUNTIME_ARN=$(echo "$RUNTIME_CREATE_RESULT" | jq -r ".LayerVersionArn")
echo "Checking for Function existence"
GET_FUNCTION=$(aws lambda get-function --function-name "$FUNCTION_NAME")
GET_FUNCTION_RESULT=$?
echo "Function lookup returned $GET_FUNCTION_RESULT"

if [ "$GET_FUNCTION_RESULT" -eq "0" ]; then
    echo "Function exists, updating code"
    aws lambda update-function-code --function-name "$FUNCTION_NAME" \
    --zip-file fileb://function.zip
    echo "Updating config with new runtime"
    aws lambda update-function-configuration --function-name "$FUNCTION_NAME" \
    --layers $RUNTIME_ARN
else 
    echo "Function does not exist, creating"
    aws lambda create-function --function-name "$FUNCTION_NAME" \
    --zip-file fileb://function.zip --handler function.handler --runtime provided \
    --role $LAMBDA_ROLE_ARN --layers $RUNTIME_ARN
fi
