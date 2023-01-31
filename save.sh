#!/usr/bin/env bash
mvn -DskipTests spring-javaformat:apply clean install && git commit -am polish && git push
