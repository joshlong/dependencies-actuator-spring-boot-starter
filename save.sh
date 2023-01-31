#!/usr/bin/env bash
mvn -DskipTests spring-javaformat:apply clean package && git commit -am polish && git push