#!/bin/bash

# mvn exec:java -Dexec.mainClass="at.lagerfeuer.lox.Lox" -Dexec.args="$@"

java -jar "$(dirname ${0})/target/jlox.jar" $@

