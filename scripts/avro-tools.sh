#!/usr/bin/env bash

set -eu

readonly DEBUG=${DEBUG:-"no"}

readonly version="1.10.0"
readonly jarFileName="avro-tools-${version}.jar"
readonly url="https://downloads.apache.org/avro/avro-${version}/java/${jarFileName}"
readonly sha512="130fbee74fd902d3ae280c15522a47e7e64ce2c43954d503ccf81510c527a8b2b3308a6d39855efcd15efe643129fff0220ecb064976a7379147aafa1bc8492e  ${jarFileName}"
readonly installPath="${HOME}/.local/opt/avro-tools"
readonly avroToolsJarFile="${installPath}/${jarFileName}"

[[ ${DEBUG} != "no" ]] && set -x

if [[ ! -r ${avroToolsJarFile} ]]; then
  mkdir -p "${installPath}"
  curl ${url} --output "${avroToolsJarFile}"
fi

cd "${installPath}"
readonly checksumResult=$(echo "${sha512}" | sha512sum --check --strict --status && echo "ok" || echo "err")
if [[ ${checksumResult} != "ok" ]]; then
  echo "* checksum failed, delete file: ${avroToolsJarFile} and try again"
  exit 1
fi
cd - >/dev/null

exec java -jar "${avroToolsJarFile}" "${@}"
