firstly build the project<br>
`docker-compose build` to build the docker-compose<br>
`docker images` - to see all the images <br>
`docker-compose up -d` - to start all the services<br>
`docker-compose logs -f` - to see the logs for docker-services<br>
`docker-compose down` - to shut down all the services<br>
`docker-compose ps` - to see all the services that are running
`docker-compose logs product` - will show all the logs for product service
`docker system prune -f --volumes` - if disk is running out of space - use this to reclaim the disk space

if microservice is missing due to failure - restart it with this command
`docker-compose up -d --scale product=0
 docker-compose up -d --scale product=1
`
    ./gradlew clean build && docker-compose build && ./test-em-all.bash start stop



    curl -d http://localhost:7004/product-composite/123
    curl http://localhost:7004/product-composite/123 -s | jq .
    
 For Swagger-UI
 --------------------------
 localhost:7004/swagger-ui.html is not working right now
 localhost:7004/v2/api-docs - working
 localhost:7004/swagger-resources - working
 
