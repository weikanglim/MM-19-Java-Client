#!/bin/bash
kill $(ps aux | grep "java -jar server.jar" | cut -d' ' -f4 | head -1)
java -jar server.jar &
sleep 2
./run.sh &
./run.sh &
