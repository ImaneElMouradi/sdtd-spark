#!/usr/bin/env bash

# BUILD SPARK APP AND SUBMIT IT
cd $HOME/StreamHandler && sbt package && spark-submit --class StreamHandler --master "local[*]" --packages "org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.1,com.datastax.spark:spark-cassandra-connector_2.12:3.0.0,com.datastax.cassandra:cassandra-driver-core:4.0.0" target/scala-2.12/stream-handler_2.12-1.0.jar > $HOME/spark.log

