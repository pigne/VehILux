import os,subprocess

path="."
popsize = 100

outputfilename="average.dat"


allvalues=[]
sums=[]

filescounter = 0

dirList=os.listdir(path)

for fname in dirList:
    
    if 'Route' in fname:            # find files containing 'Route'
        f = open(fname, "r")        # open file for reading
        values=[]      
          
        for line in f:    
        
            if '#' not in line:                 # skips lines starting witn "#"
                fitness=line[2:].strip()        # removes blank spaces
                values.append(float(fitness))   # adds values in the temporary array

        allvalues.append(values)                # adds the temporary array to the allvalues array
                

outputfile = open(outputfilename,"w")

for column in zip(*allvalues):
    sums.append(str(sum(column)/len(allvalues)))    # sums each array values at every index 
                                                    # and computes the averages


for index, item in enumerate(sums):
    outputfile.write(str(popsize*index)+ " " + sums[index] + "\n")  # prints each line "Number_of_eval  fitness"
    


#######GNUPLOT Call############################

    
proc = subprocess.Popen(['gnuplot','-p'], 
                        shell=True,
                        stdin=subprocess.PIPE,
                        )

proc.stdin.write('set term postscript eps color \n')
proc.stdin.write('set output \'result.eps\' \n')
proc.stdin.write('plot \'average.dat\' using 1:2 with lines  linewidth 3 title \'Mobility Model Optimization\'\n')
proc.stdin.write('quit\n') #close the gnuplot window






####TRASH#########################################

#outputfile.write(str(popsize) + "\n".join(sums))

#sums=map(sum, zip(*allvalues)) # Sums all values in a single list


