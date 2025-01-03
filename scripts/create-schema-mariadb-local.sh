#!/usr/bin/env bash

# Define default values
HOST='localhost'
PORT='3306'
SCHEMA='db_cgc'
PASSWORD='root'
USER='root'

# Parse command-line options
while getopts ":n:h:p:P:" opt; do
  case $opt in
    n) SCHEMA="$OPTARG" ;;               # Custom schema (database) name
    h) HOST="$OPTARG" ;;                 # Custom host
    p) PORT="$OPTARG" ;;                 # Custom port
    u) USER="$OPTARG" ;;                 # Custom user
    P) PASSWORD="$OPTARG" ;;             # Custom password
    \?) echo "Invalid option: -$OPTARG" >&2; exit 1 ;;
    :) echo "Option -$OPTARG requires an argument." >&2; exit 1 ;;
  esac
done
if [ -z "$SCHEMA" ] || [ -z "$HOST" ] || [ -z "$PORT" ] || [ -z "$PASSWORD" ] || [ -z "$USER" ]; then
  echo "Usage: $0 -n schema_name -h host -p port -P password -u user" >&2
  exit 1
fi

export TZ='Europe/Amsterdam'

set -e
echo -e "\e[32mStep 1/3: Dropping all objects in MariaDB owned by user '${SCHEMA}'... The MariaDB output will follow below here: \e[0m"
mysql -u "$USER" -p "$PASSWORD" -e "REVOKE ALL PRIVILEGES ON ${SCHEMA}.* FROM  '${SCHEMA}'@'%';"
mysql -u "$USER" -p "$PASSWORD" -e "DROP DATABASE IF EXISTS ${SCHEMA};"
mysql -u "$USER" -p "$PASSWORD" -e "DROP USER IF EXISTS '${SCHEMA}';"
mysql -u "$USER" -p "$PASSWORD" -e "FLUSH PRIVILEGES;"
mysql -u "$USER" -p "$PASSWORD" -e "REVOKE ALL PRIVILEGES ON ${SCHEMA}.* FROM  '${SCHEMA}'@'%';"
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
echo -e "\e[32mStep 2/3: Creating a new user and database '${SCHEMA}'... The mysql output will follow below here: \e[0m"
mysql -u "$USER" -p "$PASSWORD" -e "DROP DATABASE IF EXISTS ${SCHEMA};"
mysql -u "$USER" -p "$PASSWORD" -e "CREATE USER IF NOT EXISTS '${SCHEMA}'@'%' IDENTIFIED BY '${SCHEMA}';"
mysql -u "$USER" -p "$PASSWORD" -e "GRANT USAGE ON ${SCHEMA}.* TO '${SCHEMA}'@'%';"
mysql -u "$USER" -p "$PASSWORD" -e "GRANT ALL PRIVILEGES ON ${SCHEMA}.* TO '${SCHEMA}'@'%';"
mysql -u "$USER" -p "$PASSWORD" -e "FLUSH PRIVILEGES;"
mysql -u "$USER" -p "$PASSWORD" -e "SET GLOBAL log_bin_trust_function_creators=1;"
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
echo -e "\e[32mStarting Liquibase with Gradle to provision database... The Liquibase output will follow below here: \e[0m"
cd "$(dirname "$0")/../liquibase" || { echo "Failed to find liquibase directory"; exit 1; }
./gradlew -Denv=custom -Dcontexts=test -DdbUrl=jdbc:mariadb://${HOST}:${PORT}/${SCHEMA} -DdbUsername=${SCHEMA} -DdbPassword=${SCHEMA} -DchangelogFile=database/db.changelog.yaml -DoutputFile=build/liquibaseChanges.sql --no-daemon --stacktrace update
cd -
echo -e ""
echo -e ""
unset TZ
echo -e "\e[94mDone.\e[0m"
set +e

