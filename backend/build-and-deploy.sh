#!/bin/sh
if [ -z "$AWS_REGION" ] ; then
    AWS_REGION="$AWS_DEFAULT_REGION"
fi
cd "$(dirname "$0")"
(echo "accessKey = $AWS_ACCESS_KEY_ID" ; echo "secretKey = $AWS_SECRET_ACCESS_KEY") > extractor/src/main/resources/AwsCredentials.properties
(echo "accessKey = $AWS_ACCESS_KEY_ID" ; echo "secretKey = $AWS_SECRET_ACCESS_KEY") > api/src/main/resources/AwsCredentials.properties
git submodule update && . ../pull_cache && cd api && sbt clean && (sbt updateLambda || sbt createLambda) && aws lambda invoke --function-name ExtractedEntryHandler test && . ../cache_deps
