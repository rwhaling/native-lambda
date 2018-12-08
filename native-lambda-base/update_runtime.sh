#!/bin/bash
set -o nounset
RUNTIME_CREATE_RESULT=$(aws lambda publish-layer-version --layer-name "$RUNTIME_NAME" --zip-file fileb:///build/runtime/runtime.zip)
echo "$RUNTIME_CREATE_RESULT"
