#!/bin/sh

echo "Traffic generator starting — targeting movie-client at ${TARGET_URL:-http://movie-client:8080}"
TARGET="${TARGET_URL:-http://movie-client:8080}"

# Wait for movie-client to become available
echo "Waiting for movie-client to be ready..."
until curl -sf "${TARGET}/movies" > /dev/null 2>&1; do
  sleep 2
done
echo "movie-client is up — starting traffic loop"

while true; do
  # Fetch all movies (~60% of requests)
  # Fetch individual movies by random ID (~40% of requests)
  roll=$((RANDOM % 10))

  if [ "$roll" -lt 6 ]; then
    status=$(curl -s -o /dev/null -w "%{http_code}" "${TARGET}/movies")
    echo "[$(date +%T)] GET /movies -> ${status}"
  else
    id=$((RANDOM % 12 + 1))  # IDs 1-12; 11-12 will be 404s since only 10 movies exist
    status=$(curl -s -o /dev/null -w "%{http_code}" "${TARGET}/movies/${id}")
    echo "[$(date +%T)] GET /movies/${id} -> ${status}"
  fi

  # Sleep 1-2 seconds (random)
  sleep_time=$((RANDOM % 2 + 1))
  sleep "${sleep_time}"
done
