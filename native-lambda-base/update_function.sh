#!/usr/bin/bash
set -o nounset
set -e

LAYER_VERSIONS=$(aws lambda list-layer-versions --layer-name $RUNTIME_NAME --max-items 1)
RUNTIME_ARN=$(echo $LAYER_VERSIONS | jq ".LayerVersions[0].LayerVersionArn")

echo "RUNTIME ARN: \"$RUNTIME_ARN\""

if [ $RUNTIME_ARN = "null" ] then
    echo "couldn't find runtime layer, exiting"
    exit 1
fi

aws lambda update-function-code --function-name "$FUNCTION_NAME" \
--zip-file fileb://function.zip
