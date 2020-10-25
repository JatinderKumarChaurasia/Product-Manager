from root of product-manager
do
  gradle build
  
then create a `Dockerfile` in product-service<br>
> you can see the Dockerfile in microservices/product-service

from microservices/product-service
run
  > docker build -t product-service . <br>
  > docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product-service

it will create a container with name `my-product-service` <br>

`docker ps` to see the running container

to see the logs of my-product-service container
run <br>
 `docker logs my-product-service -f`
 
 to stop the `my-product-service container` do <br>
  `docker rm -f my-product-service`