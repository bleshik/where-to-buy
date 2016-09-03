#!/bin/sh
if [ -z "$AWS_REGION" ] ; then
    AWS_REGION="$AWS_DEFAULT_REGION"
fi
cd "$(dirname "$0")"
(echo "accessKey = $AWS_ACCESS_KEY_ID" ; echo "secretKey = $AWS_SECRET_ACCESS_KEY") > ./src/main/resources/AwsCredentials.properties
. ../../pull_cache && sbt clean && sbt updateLambda && . ../../cache_deps
