#!/bin/bash
a=$SECONDS

for i in {1..10000}
do
   curl http://localhost:8081/public/rate4
   #sleep 0.3s
   echo "($i)"
   echo ""
done
elapsedseconds=$(( SECONDS - a ))
echo "done $elapsedseconds"