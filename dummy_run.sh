#!/bin/bash
kill $(ps aux | grep "java -jar server.jar" | cut -d' ' -f4 | head -1)
sleep 2
java -jar server.jar &
sleep 2
./run.sh &
java -jar ./sam.jar &
