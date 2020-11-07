firstly build the project<br>
`docker-compose build` to build the docker-compose<br>
`docker images` - to see all the images <br>
`docker rmi imageID` - to delete the particular image <br>
`docker ps -a` - to see all the containers currently on docker
`docker rm containerID`  to delete the container <br>
`docker-compose up -d` - to start all the services<br>
`docker-compose logs -f` - to see the logs for docker-services <br>
`docker-compose down` - to shut down all the services <br>
`docker-compose ps` - to see all the services that are running <br>
`docker-compose logs product` - will show all the logs for product service <br>
`docker system prune -f --volumes` - if disk is running out of space - use this to reclaim the disk space<br>

###### if microservice is missing due to failure - restart it with this command: <br>
`docker-compose up -d --scale product=0
 docker-compose up -d --scale product=1
`

    ./gradlew clean build && docker-compose build && ./test-em-all.bash start stop
    
    
> while setting mysql - getting error with latest mysql version so using 5.7 <br>
> docker mongo: latest using port: 27017 

##### Database Actions

    POST http://localhost:7004/product-composite
      {
          "name":"shivani",
          "productID":1,
          "weight": 56.5,
          "recommendations": [
              {
                  "author": "shivani",
                  "content": "journal",
                  "rate": 2.3,
                  "subject": "shivani_subject"
              }
          ],
          "reviews": [
              {
                  "author": "shivani",
                  "subject": "Review",
                  "content": "Good"
              }
          ]
      }
    
    GET http://localhost:7004/product-composite/1 to get the product details
    DELETE http://localhost:7004/product-composite/1 to delete the product
    
    use those command to get the data
    ----------------------------------------------------------------
    docker-compose exec mongodb mongo recommendation-db --quiet --eval "db.recommendations.find()"
    docker-compose exec mongodb mongo product-db --quiet --eval "db.products.find()"
    docker-compose exec mysql mysql -uuser -p review-db -e "select * from reviews"
 `



    curl -d http://localhost:7004/product-composite/123
    curl http://localhost:7004/product-composite/123 -s | jq .
    
 For Swagger-UI
 --------------------------
 localhost:7004/swagger-ui.html is not working right now
 localhost:7004/v2/api-docs - working
 localhost:7004/swagger-resources - working
 
