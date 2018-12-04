#!/usr/bin/bash

# export FUNCTION_BASE_NAME=native-test

aws lambda delete-function --function-name $FUNCTION_NAME
aws lambda delete-layer-version --layer-name $RUNTIME_NAME --version-number 1