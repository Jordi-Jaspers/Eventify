#!/usr/bin/env bash

if [ $# != 1 ]
then
  echo "Usage ${0} schema_name host port"
  exit 1
fi

DATABASE_USER=root
DATABASE_PASSWORD=root

SCHEMA=${1}

mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS ${SCHEMA}"
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "CREATE USER IF NOT EXISTS ${SCHEMA} IDENTIFIED BY '${SCHEMA}'"
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "CREATE USER IF NOT EXISTS '${SCHEMA}'@'*' IDENTIFIED BY '${SCHEMA}'"
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "GRANT USAGE ON ${SCHEMA}.* TO ${SCHEMA}"
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "GRANT ALL PRIVILEGES ON ${SCHEMA}.* TO ${SCHEMA}"
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "set global log_bin_trust_function_creators=1;"
