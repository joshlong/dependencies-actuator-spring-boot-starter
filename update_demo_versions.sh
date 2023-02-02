#!/usr/bin/env bash

function update_demos(){
  NV=$1
  GRADLE_DEMO=samples/gradle-demo
  MAVEN_DEMO=samples/maven-demo
  cd $MAVEN_DEMO && ./mvnw versions:set -DnewVersion=$NV
  cd $GRADLE_DEMO && echo $NV > version.txt
}

update_demos 1.0