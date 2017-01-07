#!/usr/bin/env bash

do_start() {
    NW=${NUM_WORKERS:=2}

    for i in $(seq ${NW}); do
        twistd --pidfile=/var/run/bench/bench${i}.pid cyclone -u /var/run/bench/bench${i}.sock bench.py
    done
    return 0
}

do_stop() {
    for pid in $(ls /var/run/bench/bench[0-9].pid); do
        cat ${pid} | xargs kill
    done
    return 0
}

case "$1" in
    start)
        do_start
        ;;
    stop)
        do_stop
        ;;
    restart)
        do_stop
        do_start
        ;;
esac
