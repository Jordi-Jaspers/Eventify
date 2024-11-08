#!/usr/bin/env bash

if [ $# != 1 ]
then
  echo "Usage ${0} schema_name"
  exit 1
fi

if [ $EUID != 0 ]; then
  echo "Please run this as root:"
  echo -n "   sudo "
  echo -n `basename ${0}`
  echo " $*"
  exit 1
fi

DATABASE_USER=root
DATABASE_PASSWORD=root

SCHEMA=${1}
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "DROP DATABASE IF EXISTS ${SCHEMA}" 2> /dev/null
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "DROP USER ${SCHEMA}" 2> /dev/null
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "DROP USER '${SCHEMA}'@'*'" 2> /dev/null
mysql -u ${DATABASE_USER} -p"${DATABASE_PASSWORD}" -e "FLUSH PRIVILEGES"
