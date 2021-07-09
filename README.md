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
4. [HTTP/1.0 specification](https://www.w3.org/Protocols/HTTP/1.0/spec.html)
5. [HTTP/1.1 specification](https://httpwg.org/specs/rfc7230.html)

## Tips to know:
1. Connection reuse only works since HTTP 1.1 (in curl at least). See [Persistence section](https://httpwg.org/specs/rfc7230.html#persistent.connections)
2. It seems that it is better to build a connection pool based on Java NIO (ServerSocketChannel, SocketChannel), where we can register a selector that listens on channels in one thread.
   When using Java standard IO, we basically have a thread for each socket that is listening on it. This method may be ineffective with a large number of connections.