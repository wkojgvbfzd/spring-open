#!/bin/bash

ulimit -c unlimited
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${HOME}/ramcloud/bindings/java/edu/stanford/ramcloud:${HOME}/ramcloud/obj.blueprint-java

# Set paths
ONOS_HOME=`dirname $0`
RAMCLOUD_DIR=${HOME}/ramcloud
LOGDIR=${ONOS_HOME}/onos-logs
RAMCLOUD_LOG=${LOGDIR}/ramcloud.server.`hostname`.log
coordinatorip=`grep coordinatorIp ${ONOS_HOME}/conf/ramcloud.conf | cut -d "=" -f 2,3`
coordinatorport=`grep coordinatorPort ${ONOS_HOME}/conf/ramcloud.conf | cut -d "=" -f 2,3`
RAMCLOUD_COORDINATOR=`echo $coordinatorip","$coordinatorport`
serverip=`grep serverIp ${ONOS_HOME}/conf/ramcloud.conf | cut -d "=" -f 2,3`
serverport=`grep serverPort ${ONOS_HOME}/conf/ramcloud.conf | cut -d "=" -f 2,3`
RAMCLOUD_SERVER=`echo $serverip","$serverport`

function lotate {
    logfile=$1
    nr_max=${2:-10}
    if [ -f $logfile ]; then
	for i in `seq $(expr $nr_max - 1) -1 1`; do
	    if [ -f ${logfile}.${i} ]; then
		mv -f ${logfile}.${i} ${logfile}.`expr $i + 1`
	    fi
	done
	mv $logfile $logfile.1
    fi
}

function start {
  if [ ! -d ${LOGDIR} ]; then
    mkdir -p ${LOGDIR}
  fi
  echo "rotate log: $log"
  if [ -f $RAMCLOUD_LOG ]; then
    lotate $RAMCLOUD_LOG
  fi

  # Run ramcloud 
  echo "Starting ramcloud"
  $RAMCLOUD_DIR/obj.blueprint-java/server -M -L $RAMCLOUD_SERVER -C $RAMCLOUD_COORDINATOR --detectFailures 0 > $RAMCLOUD_LOG 2>&1 &
}

function stop {
  # Kill the existing processes
  capid=`pgrep -f obj.blueprint-java/server | awk '{print $1}'`
  pids="$capid"
  for p in ${pids}; do
    if [ x$p != "x" ]; then
      kill -KILL $p
      echo "Killed existing prosess (pid: $p)"
    fi
  done
}

function deldb {
#   # Delete the berkeley db database
   if [ -d "/tmp/ramcloud.conf" ]; then
      echo "deleting berkeley db dir"
      sudo rm -rf /tmp/ramcloud.conf
   fi
}

case "$1" in
  start)
    deldb
    cp $ONOS_HOME/conf/ramcloud.conf /tmp
    stop
    start 
    ;;
  stop)
    stop
    ;;
#  deldb)
#    deldb
#    ;;
  status)
    n=`pgrep -f obj.blueprint-java/server | wc -l`
    echo "$n ramcloud server running"
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
esac