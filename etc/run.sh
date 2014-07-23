#!/bin/bash
 
MY_APP_HOME="/var/www/mooviefish.com/app"
MY_APP_JAR="mooviefishsrv-0.1.2-beta-standalone.jar"
MY_LOG=$MY_APP_HOME/log/mooviefishsrv.log

java -Xms256m -Xmx512m -jar $MY_APP_HOME/$MY_APP_JAR > $MY_LOG &
echo $! > $MY_APP_HOME/tmp/pids/mooviefishsrv.pid

exit 0
