#!/bin/sh

path=`pwd`
echo $path
nohup java -jar $path/schedulerMaster-1.0-SNAPSHOT.jar &
