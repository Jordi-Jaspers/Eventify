#!/usr/bin/env bash

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

# Default values
HOST='localhost'
PORT='5432'
SCHEMA='tst_eventify'
PASSWORD='postgres'
USERNAME='root'
CONTAINER_NAME='timescaledb'
USE_DOCKER='auto'

while getopts ":n:h:p:P:u:d:C:" opt; do
  case $opt in
    n) SCHEMA="$OPTARG" ;;
    h) HOST="$OPTARG" ;;
    p) PORT="$OPTARG" ;;
    P) PASSWORD="$OPTARG" ;;
    u) USERNAME="$OPTARG" ;;
    d) USE_DOCKER="$OPTARG" ;;
    C) CONTAINER_NAME="$OPTARG" ;;
    \?) echo "Invalid option: -$OPTARG" >&2; exit 1 ;;
    :) echo "Option -$OPTARG requires an argument." >&2; exit 1 ;;
  esac
done

export TZ='Europe/Amsterdam'
export PGPASSWORD="$PASSWORD"

execute_psql() {
  local cmd="$1"
  if [ "$USE_DOCKER" = "yes" ] || ([ "$USE_DOCKER" = "auto" ] && ! command -v psql &> /dev/null); then
    docker exec -e PGPASSWORD="$PASSWORD" "$CONTAINER_NAME" psql -U "$USERNAME" -c "$cmd" > /dev/null 2>&1
  else
    psql -U "$USERNAME" -h "$HOST" -p "$PORT" -c "$cmd" > /dev/null 2>&1
  fi
}

# Check docker container
if [ "$USE_DOCKER" = "yes" ] || ([ "$USE_DOCKER" = "auto" ] && ! command -v psql &> /dev/null); then
  if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "${RED}Error: Docker container '${CONTAINER_NAME}' is not running.${RESET}"
    echo -e "Please start it with: docker-compose up -d"
    exit 1
  fi
  info "Using Docker container '${CONTAINER_NAME}'"
fi

section "Dropping existing database '${SCHEMA}'"
step "Revoking privileges"; execute_psql "REVOKE ALL PRIVILEGES ON DATABASE ${SCHEMA} FROM ${SCHEMA};" && ok || ok
step "Dropping database"; execute_psql "DROP DATABASE ${SCHEMA} WITH (FORCE);" && ok || ok
step "Reassigning owned objects"; execute_psql "REASSIGN OWNED BY ${SCHEMA} TO ${USERNAME};" && ok || ok
step "Dropping owned objects"; execute_psql "DROP OWNED BY ${SCHEMA};" && ok || ok
step "Revoking schema privileges"; execute_psql "REVOKE ALL PRIVILEGES ON SCHEMA public FROM ${SCHEMA};" && ok || ok
step "Dropping user"; execute_psql "DROP USER IF EXISTS ${SCHEMA};" && ok || ok

section "Creating new database '${SCHEMA}'"
step "Creating role"; execute_psql "CREATE ROLE ${SCHEMA} WITH LOGIN PASSWORD '${SCHEMA}';" && ok || fail
step "Creating database"; execute_psql "CREATE DATABASE ${SCHEMA};" && ok || fail
step "Setting owner"; execute_psql "ALTER DATABASE ${SCHEMA} OWNER TO ${SCHEMA};" && ok || fail
step "Granting database privileges"; execute_psql "GRANT ALL PRIVILEGES ON DATABASE ${SCHEMA} TO ${SCHEMA};" && ok || fail
step "Granting schema privileges"; execute_psql "GRANT CREATE, USAGE ON SCHEMA public TO ${SCHEMA};" && ok || fail

section "Complete"
info "Database '${SCHEMA}' is ready"
info "Liquibase migrations will run on application start"

unset PGPASSWORD
unset TZ
