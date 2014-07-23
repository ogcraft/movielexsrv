#!/bin/bash
 
MY_APP_HOME="/var/www/mooviefish.com/app"
MY_APP_JAR="mooviefishsrv-0.1.2-beta-standalone.jar"
 
java -Xms256m -Xmx512m -jar $MY_APP_HOME/$MY_APP_JAR &
echo $! > $MY_APP_HOME/tmp/pids/mooviefishsrv.pid
exit 0
