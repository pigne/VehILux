set term postscript eps color
set output 'Comparison.eps'
set yrange[17000:18400]
plot './10X10-Spx/average.dat' using 1:2 with lines linewidth 3 title 'cGA-SpX', './10X10-Dpx/average.dat' using 1:2 with lines linewidth 3 title 'cGA-DpX-Tournament', './10X10-DpX_Center_Selection/average.dat' using 1:2 with lines linewidth 3 title 'cGA -DpX-CenterSelect', './10X10-SpX-ifNonWorse/average.dat' using 1:2 with lines linewidth 3 title 'cGA -SpX-ifNonWorse', './10X10-SpX-ifBetter/average.dat' using 1:2 with lines linewidth 3 title 'cGA -SpX-ifBetter'