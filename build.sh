#!/bin/bash

cd core/
rm -rf target/
mvn clean install || exit $?
cd ../

cd spring/
rm -rf target/
mvn clean install || exit $?
cd ../

cd msgpush/
rm -rf target/
mvn clean compile || exit $?
cd ../


