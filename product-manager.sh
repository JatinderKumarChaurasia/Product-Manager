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

function composite_add() {
  local productID=$1
  local productComposite=$2
  #  assertCurl 200 "curl -X DELETE http://$HOST:$PORT/product-composite/${productID} -s"
  curl -X POST http://$HOST:$PORT/product-composite -H "Content-Type: application/json" --data "$productComposite"
}

function setting_test_data() {
  productComposite1='{"productID":1,"name":"product 1","weight":5,"recommendations":[{"recommendationID":1,"author":"author 1","rate":1,"content":"content 1"},{"recommendationID":2,"author":"author 2","rate":2,"content":"content 2"},{"recommendationID":3,"author":"author 3","rate":3,"content":"content 3"}],"reviews":[{"reviewID":1,"author":"author 1","subject":"subject 1","content":"content 1"},{"reviewID":2,"author":"author 2","subject":"subject 2","content":"content 2"},{"reviewID":3,"author":"author 3","subject":"subject 3","content":"content 3"}]}'

  composite_add 1 "${productComposite1}"

  productComposite2='{"productID":113,"name":"product 113","weight":113,"reviews":[{"reviewID":1,"author":"author 1","subject":"subject 1","content":"content 1"},{"reviewID":2,"author":"author 2","subject":"subject 2","content":"content 2"},{"reviewID":3,"author":"author 3","subject":"subject 3","content":"content 3"}]}'
  composite_add 113 "${productComposite2}"

  productComposite3='{
       "productID":213,"name":"product 213","weight":213.3,"recommendations":[{"recommendationID":1,"author":"author 1","rate":1,"content":"content 1"},
          {"recommendationID":2,"author":"author 2","rate":2,"content":"content 2"},{"recommendationID":3,"author":"author 3","rate":3,"content":"content 3"}]}'
  composite_add 213 "${productComposite3}"
}

set -e

echo "Start On: :" $(date)

if [[ $* == *"start"* ]]; then
  echo "Restarting the test environment..."
  echo "$ docker-compose down"
  docker-compose down
  echo "$ docker-compose up -d"
  docker-compose up -d

fi

echo "Setting the Test Data"
setting_test_data
echo "Test Data Set"
echo "Testing the url using curl"
waitForService "http://$HOST:$PORT/product-composite/213"
### Testing URL:
echo "Verifications : "
# Verify that a normal request works, expect three recommendations and three reviews
assertCurl 200 "curl http://$HOST:$PORT/product-composite/1 -s"
assertEqual 1 $(echo $RESPONSE | jq .productID)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify that a 404 (Not Found) error is returned for a non existing productID (13)
assertCurl 404 "curl http://$HOST:$PORT/product-composite/1 -s"

# Verify that no recommendations are returned for productID 113
assertCurl 200 "curl http://$HOST:$PORT/product-composite/113 -s"
assertEqual 113 $(echo $RESPONSE | jq .productID)
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify that no reviews are returned for productID 213
assertCurl 200 "curl http://$HOST:$PORT/product-composite/213 -s"
assertEqual 213 $(echo $RESPONSE | jq .productID)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a productID that is out of range (-1)
assertCurl 422 "curl http://$HOST:$PORT/product-composite/-1 -s"
assertEqual "\"Invalid productID: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a productId that is not a number, i.e. invalid format
assertCurl 400 "curl http://$HOST:$PORT/product-composite/invalidProductID -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

if [[ $* == *"stop"* ]]; then
  echo "We are done, stopping the test environment..."
  echo "$ docker-compose down"
  docker-compose down
fi
