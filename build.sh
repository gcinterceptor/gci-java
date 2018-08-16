#!/bin/bash

# Compile libgc.so
cd core/src/main/java/
javac -classpath . com/gcinterceptor/core/GC.java
javac -h c com/gcinterceptor/core/GC.java
gcc -shared -fpic -I"/usr/jdk-10.0.1/include" -I"/usr/jdk-10.0.1/include/linux" ./com_gcinterceptor_core_GC.c -o ./libgc.so
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
