#!/bin/bash
 
MY_APP_HOME="/var/www/movielex.com/app"
MY_APP_JAR="movielexsrv-0.1.2-beta-standalone.jar"
MY_LOG=$MY_APP_HOME/log/movielexsrv.log

java -Xms256m -Xmx384m -jar $MY_APP_HOME/$MY_APP_JAR > $MY_LOG &
echo $! > $MY_APP_HOME/tmp/pids/movielexsrv.pid

exit 0
