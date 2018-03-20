#!/bin/bash

cd core/
mvn clean
mvn install
cd ../

cd spring/
mvn clean
mvn install
cd ../

cd msgpush/
mvn clean
mvn install
cd ../


