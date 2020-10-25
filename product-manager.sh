#!/usr/bin/env bash
#
# Sample usage:
#
#
#
: ${HOST=localhost}
: ${PORT=7004}

echo "HOST=${HOST}"
echo "PORT=${PORT}"

function test_url() {
  url=$*
  echo "curling url :$url"
  if curl "$url" -ks -f -o /dev/null; then
    echo "Ok"
    return 0
  else
    echo -n "not yet"
    return 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}
set -e

function waitForService() {
  url=$*
  echo -n "wait for url: $url..."
  n=0
  until test_url "$url"; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo "Give Up"
      exit 1
    else
      sleep 6
      echo -n ",retry #$n "
    fi
  done
}

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    if [ "$httpCode" = "200" ]; then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
    echo "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo "- Failing command: $curlCmd"
    echo "- Response Body: $RESPONSE"
    exit 1
  fi
}

if [[ $* == *"start"* ]]; then
  echo "Restarting the test environment..."
  echo "$ docker-compose down"
  docker-compose down
  echo "$ docker-compose up -d"
  docker-compose up -d

  echo "Testing the url using curl"
  waitForService "http://$HOST:${PORT}/product-composite/1"
fi

if [[ $* == *"stop"* ]]; then
  echo "We are done, stopping the test environment..."
  echo "$ docker-compose down"
  docker-compose down
fi
