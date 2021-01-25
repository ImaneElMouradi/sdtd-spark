#!/usr/bin/env bash

# # LAUNCH MASTER NODE
# cd $HOME/spark/sbin && ./start-master.sh --host 172.17.0.2 --properties-file ha.conf

# # LAUNCH A WORKER NODE
# # ./start-slave.sh spark://127.0.0.1:7077, --host 127.0.0.1

# BUILD SPARK APP AND SUBMIT IT
sleep 1m
cd $HOME/StreamHandler && sbt package && spark-submit --class StreamHandler --master "spark://172.31.48.37:7077,172.31.48.38:7077" --packages "org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.1,com.datastax.spark:spark-cassandra-connector_2.12:3.0.0,com.datastax.cassandra:cassandra-driver-core:4.0.0" target/scala-2.12/stream-handler_2.12-1.0.jar > $HOME/spark.log

