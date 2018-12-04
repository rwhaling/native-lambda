#!/bin/bash

aws lambda invoke --function-name $FUNCTION_NAME --payload '{"text":"Test Native Payload?"}' response.txt
cat response.txt