#!/bin/sh

java -classpath lib/*:bin/.:/home/cis455/workspace/HW1/src/edu/upenn/cis/cis455/webserver edu.upenn.cis.cis455.webserver.HttpServer 8080 /home/cis455/workspace/HW1/www /home/cis455/workspace/HW1/src/edu/upenn/cis/cis455/webserver/web.xml &
sleep 2
curl http://localhost:8080/control
curl http://localhost:8080/shutdown
