#!/bin/sh

aws lambda update-function-code --function-name "$FUNCTION_NAME" \
--zip-file fileb://function.zip

