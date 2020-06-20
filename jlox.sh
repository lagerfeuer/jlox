#!/bin/bash

# java -jar target/jlox.jar $@


mvn exec:java -Dexec.mainClass="at.lagerfeuer.lox.Lox" -Dexec.args="$@"
