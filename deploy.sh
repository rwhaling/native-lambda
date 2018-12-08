#!/bin/bash
set -o nounset
docker run -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY -e AWS_DEFAULT_REGION -e LAMBDA_ROLE_ARN -it native-lambda:latest ./init.sh