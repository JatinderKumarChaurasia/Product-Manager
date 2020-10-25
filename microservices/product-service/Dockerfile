FROM openjdk:15
EXPOSE 7004
ADD ./build/libs/*.jar my-app.jar
ENTRYPOINT ["java","-jar","/my-app.jar"]