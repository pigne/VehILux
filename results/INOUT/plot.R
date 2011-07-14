e1<-read.table("measure.log",header=TRUE)
control<-read.table("control.log",header=TRUE)
colors<- c('Gray60','Steelblue',"Darkgoldenrod","Olivedrab")
n = (floor(length(t(e1['time'])) / 4) +2)
cat (n,'\n');





pdf(paste("measures.pdf"))


par(mfrow=c(n,4), cex=0.45)

for(i in colnames(control)){
   if(i != "time"){ 
      ylim <- range(c(t(e1[i]),t(control[i])))
      plot(t(control["time"]),t(control[i]),type='o',col=colors[1], main=i, ylim=ylim, xlab='Time of the day (h)',  ylab='Vehicle flow per hour' ); 
      points( t(e1["time"]),t(e1[i]),type='o',col=colors[2]);  
      legend("topleft", legend = c("Real Data", "Simulation"),
               text.width = strwidth("Simulation"),
               col=colors,lty = 1)
      cat(i,'\n'); 
   } 
}
dev.off()
