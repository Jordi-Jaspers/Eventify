#!/usr/bin/env bash

# Define default values
HOST='localhost'
PORT='5432'
SCHEMA='tst_eventify'
PASSWORD='postgres'

# Parse command-line options
while getopts ":n:h:p:P:" opt; do
  case $opt in
    n) SCHEMA="$OPTARG" ;;               # Custom schema (database) name
    h) HOST="$OPTARG" ;;                 # Custom host
    p) PORT="$OPTARG" ;;                 # Custom port
    P) PASSWORD="$OPTARG" ;;             # Custom password
    \?) echo "Invalid option: -$OPTARG" >&2; exit 1 ;;
    :) echo "Option -$OPTARG requires an argument." >&2; exit 1 ;;
  esac
done
if [ -z "$SCHEMA" ] || [ -z "$HOST" ] || [ -z "$PORT" ] || [ -z "$PASSWORD" ]; then
  echo "Usage: $0 -n schema_name -h host -p port -P password" >&2
  exit 1
fi

export TZ='Europe/Amsterdam'
export PGPASSWORD="$PASSWORD"

echo -e "\e[32mDropping all objects in PostgreSQL owned by user '${SCHEMA}'... The postgreSQL output will follow below here: \e[0m"
psql -U root -h "$HOST" -p "$PORT" -c "REVOKE ALL PRIVILEGES ON DATABASE ${SCHEMA} FROM ${SCHEMA};"
psql -U root -h "$HOST" -p "$PORT" -c "DROP DATABASE ${SCHEMA} WITH (FORCE);"
psql -U root -h "$HOST" -p "$PORT" -c "REASSIGN OWNED BY ${SCHEMA} TO root;"
psql -U root -h "$HOST" -p "$PORT" -c "DROP OWNED BY ${SCHEMA};"
psql -U root -h "$HOST" -p "$PORT" -c "REVOKE ALL PRIVILEGES ON SCHEMA public FROM ${SCHEMA};"
psql -U root -h "$HOST" -p "$PORT" -c "DROP USER IF EXISTS ${SCHEMA};"
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
echo -e "\e[32mCreating a new user and database '${SCHEMA}'... The PostgreSQL output will follow below here: \e[0m"
psql -U root -h $HOST -p $PORT -c "CREATE ROLE ${SCHEMA} WITH LOGIN PASSWORD '${SCHEMA}';"
psql -U root -h $HOST -p $PORT -c "CREATE DATABASE ${SCHEMA};"
psql -U root -h $HOST -p $PORT -c "ALTER DATABASE ${SCHEMA} OWNER TO ${SCHEMA};"
psql -U root -h $HOST -p $PORT -c "GRANT ALL PRIVILEGES ON DATABASE ${SCHEMA} TO ${SCHEMA};"
psql -U root -h $HOST -p $PORT -c "GRANT CREATE, USAGE ON SCHEMA public TO ${SCHEMA};"
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
echo -e "\e[32mStarting Liquibase with Gradle to provision database... The Liquibase output will follow below here: \e[0m"
cd "$(dirname "$0")/../liquibase" || { echo "Failed to find liquibase directory"; exit 1; }
./gradlew -Denv=custom -Dcontexts=test -DdbUrl=jdbc:postgresql://${HOST}:${PORT}/${SCHEMA} -DdbUsername=${SCHEMA} -DdbPassword=${SCHEMA} -DchangelogFile=database/db.changelog.yaml -DoutputFile=build/liquibaseChanges.sql --no-daemon --stacktrace update
cd -
echo -e ""
echo -e ""
# Disable script-wide error checking
unset PGPASSWORD
unset TZ
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
