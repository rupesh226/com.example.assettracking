#export JAVA_HOME=`/usr/libexec/java_home -v 17`
docker rmi -f assettracking
mvn clean package -DskipTests
docker build -t assettracking .
docker rm -f assettracking
#docker run --name assettracking -p 8080:8080 assettracking


docker tag assettracking rupesh226/assettracking:1.0.1
docker push rupesh226/assettracking:1.0.1
 
 docker run -d --name dynamodb -p 8000:8000 amazon/dynamodb-local
 
 docker run --name dynamodb -p 8000:8000 -v /Users/rupesh/Documents/workspace-spring-tool-suite/com.example.assettracking/dynamodblocal/data:/home/dynamodblocal/data
 
 
 java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb /Users/rupesh/Documents/workspace-spring-tool-suite/com.example.assettracking/dynamodblocal/data
 
 
http://localhost:8080/api/events/asset?asset=0&trip=125&page=1&size=1000
http://localhost:8080/api/events/latest?page=1&size=1000
http://localhost:8080/api/events/3d8a289f-2a4a-4fdd-ab33-dc326857290e
http://localhost:8080/api/events/asset/0?start=2019-09-02T19%3A51%3A00Z&end=2019-09-03T19%3A51%3A00Z&page=1&size=100
