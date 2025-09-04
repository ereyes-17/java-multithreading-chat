#! /bin/bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar target/java-multithreading-chat-1.0-SNAPSHOT.jar server $1