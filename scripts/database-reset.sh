#!/usr/bin/env bash

# Define default values
HOST='localhost'
PORT='5432'
SCHEMA='tst_eventify'
PASSWORD='postgres'
CONTEXTS='dev'
CONTAINER_NAME='timescaledb'
USE_DOCKER='auto'

# Parse command-line options
while getopts ":n:h:p:P:c:d:" opt; do
  case $opt in
    n) SCHEMA="$OPTARG" ;;               # Custom schema (database) name
    h) HOST="$OPTARG" ;;                 # Custom host
    p) PORT="$OPTARG" ;;                 # Custom port
    P) PASSWORD="$OPTARG" ;;             # Custom password
    c) CONTEXTS="$OPTARG" ;;             # Custom contexts
    d) USE_DOCKER="$OPTARG" ;;           # Use docker (yes/no/auto)
    \?) echo "Invalid option: -$OPTARG" >&2; exit 1 ;;
    :) echo "Option -$OPTARG requires an argument." >&2; exit 1 ;;
  esac
done
if [ -z "$SCHEMA" ] || [ -z "$HOST" ] || [ -z "$PORT" ] || [ -z "$PASSWORD" ]; then
  echo "Usage: $0 -n schema_name -h host -p port -P password [-d yes|no|auto]" >&2
  exit 1
fi

export TZ='Europe/Amsterdam'
export PGPASSWORD="$PASSWORD"

# Function to execute psql command
execute_psql() {
  local cmd="$1"
  if [ "$USE_DOCKER" = "yes" ] || ([ "$USE_DOCKER" = "auto" ] && ! command -v psql &> /dev/null); then
    docker exec -e PGPASSWORD="$PASSWORD" "$CONTAINER_NAME" psql -U root -c "$cmd"
  else
    psql -U root -h "$HOST" -p "$PORT" -c "$cmd"
  fi
}

# Check if docker container is running when using docker mode
if [ "$USE_DOCKER" = "yes" ] || ([ "$USE_DOCKER" = "auto" ] && ! command -v psql &> /dev/null); then
  if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "\e[91mError: Docker container '${CONTAINER_NAME}' is not running.\e[0m"
    echo -e "Please start it with: docker-compose up -d"
    exit 1
  fi
  echo -e "\e[96mUsing Docker container '${CONTAINER_NAME}' for database operations.\e[0m"
  echo -e ""
fi

echo -e "\e[32mDropping all objects in PostgreSQL owned by user '${SCHEMA}'... The postgreSQL output will follow below here: \e[0m"
execute_psql "REVOKE ALL PRIVILEGES ON DATABASE ${SCHEMA} FROM ${SCHEMA};"
execute_psql "DROP DATABASE ${SCHEMA} WITH (FORCE);"
execute_psql "REASSIGN OWNED BY ${SCHEMA} TO root;"
execute_psql "DROP OWNED BY ${SCHEMA};"
execute_psql "REVOKE ALL PRIVILEGES ON SCHEMA public FROM ${SCHEMA};"
execute_psql "DROP USER IF EXISTS ${SCHEMA};"
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
echo -e "\e[32mCreating a new user and database '${SCHEMA}'... The PostgreSQL output will follow below here: \e[0m"
execute_psql "CREATE ROLE ${SCHEMA} WITH LOGIN PASSWORD '${SCHEMA}';"
execute_psql "CREATE DATABASE ${SCHEMA};"
execute_psql "ALTER DATABASE ${SCHEMA} OWNER TO ${SCHEMA};"
execute_psql "GRANT ALL PRIVILEGES ON DATABASE ${SCHEMA} TO ${SCHEMA};"
execute_psql "GRANT CREATE, USAGE ON SCHEMA public TO ${SCHEMA};"
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
echo -e "\e[32mDatabase '${SCHEMA}' is ready!\e[0m"
echo -e "\e[96mLiquibase migrations will run automatically when you start the application with './gradlew bootRun'\e[0m"
echo -e ""
echo -e ""
# Disable script-wide error checking
unset PGPASSWORD
unset TZ
echo -e "\e[94mDone.\e[0m"
echo -e ""
echo -e ""
