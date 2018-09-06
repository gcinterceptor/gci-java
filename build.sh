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

# Build GciPlugin for ES 6.4.0
cd elasticsearch-6.4.0-plugin/
rm -rf target/
mvn clean install  || exit $?
cd ../
cp core/target/gci-core-0.1.jar elasticsearch-6.4.0-plugin/target/
cp elasticsearch-6.4.0-plugin/src/main/resources/plugin-descriptor.properties elasticsearch-6.4.0-plugin/target/
cd elasticsearch-6.4.0-plugin/target/
zip gci-elasticsearch-plugin.zip plugin-descriptor.properties gci-core-0.1.jar  gci-elasticsearch-6.4.0-plugin-0.0.1-SNAPSHOT.jar
cd ../../

# Build GciPlugin for ES 5.5.3
cd elasticsearch-5.5.3-plugin/
rm -rf target/
mvn clean install  || exit $?
cd ../
cp core/target/gci-core-0.1.jar elasticsearch-5.5.3-plugin/target/
cp elasticsearch-5.5.3-plugin/src/main/resources/plugin-descriptor.properties elasticsearch-5.5.3-plugin/target/
cd elasticsearch-5.5.3-plugin/target/
mkdir elasticsearch/
mv plugin-descriptor.properties gci-core-0.1.jar  gci-elasticsearch-5.5.3-plugin-0.0.1-SNAPSHOT.jar elasticsearch/







