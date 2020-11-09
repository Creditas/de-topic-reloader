#!/usr/bin/env bash

set -eu

APP_NAME="topic-reloader"


readonly DEBUG=${DEBUG:-"no"}
readonly ID=$(date -u +'%Y%m%d%M%S')
readonly configurationFile="/tmp/${APP-NAME}-configurations-${ID}.json"
readonly ec2AttributesFile="/tmp/${APP-NAME}-ec2Attributes-${ID}.json"
readonly instanceGroupsFile="/tmp/${APP-NAME}-instanceGroups-${ID}.json"

cat <<EOF >"${configurationFile}"
[
  {
    "Classification": "spark-defaults",
    "Properties": {
      "spark.jars.packages": "org.apache.hadoop:hadoop-aws:3.0.0",
      "spark.serializer": "org.apache.spark.serializer.KryoSerializer"
    }
  },
  {
    "Classification": "spark",
    "Properties": {
      "maximizeResourceAllocation": "true"
    }
  }
]
EOF

cat <<EOF >"${ec2AttributesFile}"
{
  "KeyName": "kafka-management-key",
  "InstanceProfile": "EMR_EC2_DefaultRole",
  "SubnetId": "subnet-2e5ca949",
  "EmrManagedSlaveSecurityGroup": "sg-0613d3d2276cdb0fe",
  "EmrManagedMasterSecurityGroup": "sg-0fa38ac5521e3c4cf"
}
EOF

cat <<EOF >"${instanceGroupsFile}"
[
  {
    "Name":"EventProcessorMaster",
    "InstanceCount": 1,
    "InstanceGroupType": "MASTER",
    "InstanceType": "r5.xlarge"
  },
  {
    "Name": "EventProcessorCore",
    "InstanceCount": 2,
    "InstanceGroupType": "CORE",
    "InstanceType": "r5.xlarge"
  }
]
EOF

readonly me="$(whoami)"
readonly name="${APP_NAME}-${ID}-${me}"

[[ ${DEBUG} != "no" ]] && set -x

aws emr create-cluster \
  --name "${name}" \
  --applications '[ { "Name": "Hadoop" }, { "Name": "Spark" }, { "Name": "Ganglia" } ]' \
  --release-label "emr-6.1.0" \
  --service-role "EMR_DefaultRole" \
  --ec2-attributes "file://${ec2AttributesFile}" \
  --instance-groups "file://${instanceGroupsFile}" \
  --configurations "file://${configurationFile}" \
  --log-uri "s3://dev-creditas-data-lake-us-east-1/${APP_NAME}/log" \
  --enable-debugging \
  --tags Name="${name}" Application="${name}" Environment="dev" Owner="${me}" Squad="Data Platform" | jq
