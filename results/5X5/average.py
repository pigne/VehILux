import os

path="."

outputfilename="average.dat"

averages=[]

filescounter = 0

dirList=os.listdir(path)

for fname in dirList:
    f = open(fname, "r") # open file for reading
    linecount=0;
    for line in f:    
        if '#' not in line:
            fitness=line[3:]
            if filescounter==0:
                averages.append(fitness)
            else:
                averages.insert(linecount,averages[linecount]+fitness)
                linecount+=1
    filescounter+=1

outputfile = open(outputfilename,"w")
outputfile.write("\n".join(averages))

