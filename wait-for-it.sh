#!/bin/sh
set -e

host="$1"
shift
cmd="$@"

until nc -z "$host"; do
  >&2 echo "[$(date)] Waiting for $host..."
  sleep 1
done

>&2 echo "[$(date)] $host is up - executing $cmd"
exec $cmd