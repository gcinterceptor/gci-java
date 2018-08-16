#!/bin/bash

# Build GCI core 
cd core/
rm -rf target/
mvn clean install || exit $?
cd ../

# Build SpringGcInterceptor
cd spring/
rm -rf target/
mvn clean install  || exit $?

# Compile libgc.so
gcc -shared -fpic -I"/usr/jdk-10.0.1/include" -I"/usr/jdk-10.0.1/include/linux" core/src/main/java/com_gcinterceptor_core_GC.c -o core/src/main/java/libgc.so