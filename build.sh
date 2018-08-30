#!/bin/bash

date
set -x

# Compile libgc.so
cd core/src/main/java/
javac -classpath . com/gcinterceptor/core/GC.java  || exit $?
javac -h . com/gcinterceptor/core/GC.java  || exit $?
gcc -shared -fpic -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" ./com_gcinterceptor_core_GC.c -o ./libgc.so || exit $?
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
cd ../

# Build GciPlugin
cd elasticsearch-plugin/
rm -rf target/
mvn clean install  || exit $?
cd ../
cp core/target/gci-core-0.1.jar elasticsearch-plugin/target/
cp elasticsearch-plugin/src/main/resources/plugin-descriptor.properties elasticsearch-plugin/target/
cd elasticsearch-plugin/target/
zip gci-elasticsearch-plugin.zip plugin-descriptor.properties gci-core-0.1.jar  gci-elasticsearch-plugin-0.0.1-SNAPSHOT.jar
