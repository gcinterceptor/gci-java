#!/bin/bash

date
set -x

# Compile libgc.so
cd core/src/main/java/
javac -classpath . com/gcinterceptor/core/GC.java
javac -h c com/gcinterceptor/core/GC.java
gcc -shared -fpic -I"${JAVA_HOME}include" -I"${JAVA_HOME}/include/linux" ./com_gcinterceptor_core_GC.c -o ./libgc.so
cd ../../../../

# Build GCI core 
cd core/
rm -rf target/
mvn clean install || exit $?
cd ../

# Build SpringGcInterceptor
cd spring/
rm -rf target/
mvn clean install  || exit $?
