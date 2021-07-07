# http-server based on Java Sockets

### I believe that every backender should write their own server

Run server:
```bash
./gradlew clean build && java -jar build/libs/http-server.jar --port=8081 --threadsNumber=2 --ttl=5000
```