#!/bin/sh

echo "Traffic generator starting — targeting movie-client at ${TARGET_URL:-http://movie-client:8080}"
TARGET="${TARGET_URL:-http://movie-client:8080}"

# Sample data for best-match queries
TITLES="dark god matrix pulp fight club knight inception parasite"
GENRES="Drama Action Sci-Fi Crime Thriller"
YEARS="1994 1999 2008 2010 2014 2019"

# Helper to pick random item from space-separated list
pick_random() {
  echo "$1" | tr ' ' '\n' | shuf -n 1
}

# Wait for movie-client to become available
echo "Waiting for movie-client to be ready..."
until curl -sf "${TARGET}/movies" > /dev/null 2>&1; do
  sleep 2
done
echo "movie-client is up — starting traffic loop"

while true; do
  # Traffic distribution: 50% all movies, 30% by ID, 20% best-match
  roll=$((RANDOM % 10))

  if [ "$roll" -lt 5 ]; then
    # Fetch all movies (50%)
    status=$(curl -s -o /dev/null -w "%{http_code}" "${TARGET}/movies")
    echo "[$(date +%T)] GET /movies -> ${status}"

  elif [ "$roll" -lt 8 ]; then
    # Fetch by ID (30%)
    id=$((RANDOM % 12 + 1))  # IDs 1-12; 11-12 will be 404s since only 10 movies exist
    status=$(curl -s -o /dev/null -w "%{http_code}" "${TARGET}/movies/${id}")
    echo "[$(date +%T)] GET /movies/${id} -> ${status}"

  else
    # Best-match search (20%) - randomly include/exclude each param
    params=""
    param_desc=""

    if [ $((RANDOM % 2)) -eq 1 ]; then
      title=$(pick_random "$TITLES")
      params="${params}title=${title}&"
      param_desc="${param_desc}title=${title} "
    fi

    if [ $((RANDOM % 2)) -eq 1 ]; then
      year=$(pick_random "$YEARS")
      params="${params}year=${year}&"
      param_desc="${param_desc}year=${year} "
    fi

    if [ $((RANDOM % 2)) -eq 1 ]; then
      genre=$(pick_random "$GENRES")
      params="${params}genre=${genre}&"
      param_desc="${param_desc}genre=${genre} "
    fi

    # Remove trailing &
    params=$(echo "$params" | sed 's/&$//')

    if [ -n "$params" ]; then
      status=$(curl -s -o /dev/null -w "%{http_code}" "${TARGET}/movies/best-match?${params}")
      echo "[$(date +%T)] GET /movies/best-match?${params} -> ${status}"
    else
      # No params - still make the request
      status=$(curl -s -o /dev/null -w "%{http_code}" "${TARGET}/movies/best-match")
      echo "[$(date +%T)] GET /movies/best-match -> ${status}"
    fi
  fi

  # Sleep 1-2 seconds (random)
  sleep_time=$((RANDOM % 2 + 1))
  sleep "${sleep_time}"
done
