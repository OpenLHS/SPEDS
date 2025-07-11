#!/bin/bash
declare -a projects=("speds-link-library"
                     "speds-network-library"
                     "speds-transport-library"
                     "speds-session-library"
                     "speds-presentation-library"
                     "speds-application-library"
                     "speds-library")

## now loop through the above array
for i in "${projects[@]}"
do
   echo "Building $i"
   cd $i
   ./gradlew build
   ./gradlew publishToMavenLocal
   cd ..
   # or do whatever with individual element of the array
done
