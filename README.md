# KotlinEchoRMI

My implementation of a RMI server that exposes a "echo" 
function

The server also deals with replication, broadcasting the echo
message to other instances of itself (TODO)


## How to run

First, run  `gradlew build`, this will create
two files in the `app` folder that you can run with:

* Server: `jar -jar app/EchoServer-1.0.jar`
* Client: `jar -jar app/EchoClient-1.0.jar`

(Remember to run the server first)

