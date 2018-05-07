#!/bin/bash

cd core/
rm -rf target/
mvn clean install || exit $?
cd ../

cd spring/
rm -rf target/
mvn clean install  || exit $?
