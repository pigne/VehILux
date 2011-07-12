model<-read.table("model_measures.log",header=TRUE)
control<-read.table("control.log",header=TRUE)
sim<-read.table("sim_measures.log",header=TRUE)
colors<- c('Gray60','Steelblue',"Darkgoldenrod","Olivedrab")
n = (floor(length(t(model['time'])) / 4) +2)
cat (n,'\n');





pdf(paste("measures.pdf"))


par(mfrow=c(n,4), cex=0.45)

for(i in colnames(control)){
   if(i != "time"){ 
      ylim <- range(c(t(model[i]),t(sim[i]),t(control[i])))
      plot(t(control["time"]),t(control[i]),type='o',col=colors[1], main=unlist(strsplit(i,"X"))[2], ylim=ylim, xlab='Time of the day (h)',  ylab='Vehicle flow per hour' ); 
      points( t(model["time"]),t(model[i]),type='o',col=colors[2]);  
      points( t(sim["time"]),t(sim[i]),type='o',col=colors[3]);  
      legend("topleft", legend = c("Real Data", "Model", "Simulation"),
               text.width = strwidth("Simulation"),
               col=colors,lty = 1)
      cat(i,'\n'); 
   } 
}
dev.off()
