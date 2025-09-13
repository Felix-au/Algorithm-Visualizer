#!/bin/bash

echo "Building and running Algorithm Visualizer..."
echo

echo "Compiling the project..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo
echo "Running the application..."
mvn javafx:run
