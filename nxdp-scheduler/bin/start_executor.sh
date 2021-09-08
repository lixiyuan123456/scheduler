#!/bin/sh

path=`pwd`
echo $path
nohup java -jar $path/schedulerExecutor-1.0-SNAPSHOT.jar &