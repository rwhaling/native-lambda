#!/bin/bash

DEFAULT_PAYLOAD='{"text":"Lambda Native Payload"}'
PAYLOAD=${1:-$DEFAULT_PAYLOAD}
aws lambda invoke --function-name $FUNCTION_NAME --payload "$PAYLOAD" response.txt
cat response.txt
