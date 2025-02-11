#!/bin/bash

# Compile Java files
javac *.java

# Check if compilation was successful
if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

# Run tests
for i in {1..8}
do
    echo "Running test$i.minc"
    java -cp Program "test$i.minc" > "output$i.txt"

    # Compare output with solution
    if diff -q "output$i.txt" "testsolu$i.txt" > /dev/null; then
        echo "Test $i passed"
    else
        echo "Test $i failed"
        echo "Differences:"
        diff "output$i.txt" "testsolu$i.txt"
    fi

    # Clean up output file
    rm "output$i.txt"
done

