# http-server based on Java Sockets

### I believe that every backender should write their own server

#### Main goal: understand how a connection pooling works with Java Sockets  

Run server:
```bash
./gradlew clean build && java -jar build/libs/http-server.jar --port=8080 --threadsNumber=2 --keepAliveTime=30000
```

## Useful links:
1. Sun http server implementation: [sun/net/httpserver/ServerImpl.java:386](https://github.com/JetBrains/jdk8u_jdk/blob/master/src/share/classes/sun/net/httpserver/ServerImpl.java#L375)
2. [Java NIO: Non-blocking Server](http://tutorials.jenkov.com/java-nio/non-blocking-server.html)
3. [Java NIO ServerSocketChannel](http://tutorials.jenkov.com/java-nio/server-socket-channel.html)