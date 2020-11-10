#!/usr/bin/env bash

set -eu

APP_NAME="topic-reloader"

readonly DEBUG=${DEBUG:-no}
readonly ID=$(date -u +'%Y%m%d%M%S')
readonly S3_JAR_DIR="s3://dev-creditas-data-lake-us-east-1/${APP_NAME}/jar"
readonly HDFS_BASEDIR='/home/hadoop'

function usage() {
  cat <<USAGE
help!
USAGE
}

#############################
# begin command line parser #
#############################
cluster_id=""
jar=""
topic_name=""
usage="false"
unknown=("")
for param in "${@}"; do
  case ${param} in
  --cluster-id=*) cluster_id=${param#*=} ;;
  --jar=*) jar=${param#*=} ;;
  --topic-name=*) topic_name=${param#*=} ;;
  help | -h | --help) usage="true" ;;
  *) unknown+=("${param}") ;;
  esac
  shift
done

[[ ${#unknown} -gt 1 ]] && echo "unknown parameters: ${unknown[*]}" && exit 1
[[ ${#unknown} -gt 0 ]] && echo "unknown parameter: ${unknown[*]}" && exit 1
[[ ${usage} == true ]] && usage && exit 0

errors=0
[[ ${cluster_id} == "" ]] && errors=$((errors + 1)) && echo '--cluster-id=<String> must be provided'
[[ ${jar} == "" ]] && errors=$((errors + 1)) && echo '--jar=<Path> must be provided'
[[ ${topic_name} == "" ]] && errors=$((errors + 1)) && echo '--topic-name=<String> must be provided'

[[ ${errors} -ne 0 ]] && printf '\n-> Please, fix above errors and try again, %d errors(s) found\n' ${errors} && exit 1
###########################
# end command line parser #
###########################

[[ ${DEBUG} != 'no' ]] && set -x

readonly CLUSTER_ID=${cluster_id}
readonly JAR_PATH=${jar}
readonly HDFS_PATH="${HDFS_BASEDIR}/${ID}"
readonly JAR=$(basename "${JAR_PATH}")

readonly stepsFile="/tmp/event-processor-steps-${ID}.json"


cat <<EOF >"${stepsFile}"
[
  {
    "Type": "CUSTOM_JAR",
    "ActionOnFailure": "CONTINUE",
    "Name": "copy ${ID}",
    "Jar": "command-runner.jar",
    "Args": [
      "s3-dist-cp",
      "--s3Endpoint=s3.amazonaws.com",
      "--src=${S3_JAR_DIR}",
      "--dest=hdfs://${HDFS_PATH}"
    ]
  },
  {
    "Type": "Spark",
    "ActionOnFailure": "CONTINUE",
    "Name": "run ${ID}",
    "Args": [
      "--class",
      "topic.reloader.main.TopicReloaderApp",
      "hdfs://${HDFS_PATH}/${JAR}",
      "${ID}",
      "dev",
      "${topic_name}"
    ]
  }
]
EOF

aws s3 cp "${JAR_PATH}" "${S3_JAR_DIR}/"

aws emr add-steps \
  --cluster-id "${CLUSTER_ID}" \
  --steps "file://${stepsFile}" | jq
