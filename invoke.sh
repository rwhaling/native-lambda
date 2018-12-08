#!/bin/bash
docker run -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY -e AWS_DEFAULT_REGION -e LAMBDA_ROLE_ARN -it native-lambda:latest ./invoke.sh "$@"