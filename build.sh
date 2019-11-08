#!/bin/bash


if [[ $1 == "" ]]; then
  echo "./build.sh <env_name>"
  exit 1
fi

ENV_NAME=$1
cp src/main/resources/META-INF/MANIFEST.MF src/main/resources/META-INF/.MANIFEST.MF

cp src/main/resources/META-INF/MANIFEST.MF.${ENV_NAME} src/main/resources/META-INF/MANIFEST.MF

mvn clean jaxb2:xjc compile install

cp target/*.jar ~/jitterbit/connectors/env/${ENV_NAME}/.