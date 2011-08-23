#!/bin/bash

n=0
IFS=' '

for file in `ls | grep Route`
do
    tail +38 $file | cut -f2 



        #for line in $(cat temp.txt)
        #do
            #echo $line | read arr1[1]
            #echo $line|cut  -d '<space>' -f2 
            #echo ${line:$IFS} >> average.dat
            
        #done
    #n=`expr $n + 1`
done

#for file in `ls | grep temp`
