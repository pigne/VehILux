model<-read.table("model_measures.log",header=TRUE)
control<-read.table("control.log",header=TRUE)
colors<- c('Gray60','Steelblue',"Darkgoldenrod","Olivedrab")
n = (floor(length(t(model['time'])) / 3) +2)
cat (n,'\n');
linetype <- c(1,6);
plotchar <- seq(1,1+2,1)



pdf(paste("measures.pdf"))


par(mfrow=c(n,3), cex=0.6)
#par(pin=c(4/3,5/n)) ### default c(4.14,3.57)
par(mai=c(1.2/n,.769/3,.8/n,.394/3))  ### margins default c(.956,.769,.769,.394)


for(i in colnames(control)){
   if(i != "time"){ 
      ylim <- range(c(t(model[i]),t(control[i])))
      plot(t(control["time"]),t(control[i]),type='o',lty=linetype[1],pch=plotchar[1],col=colors[1], main=unlist(strsplit(i,"X"))[2], ylim=ylim, xlab='Time of the day (h)',  ylab='Vehicle flow per hour' ); 
      points( t(model["time"]),t(model[i]),lty=linetype[2],type="o",pch=plotchar[2],col=colors[2]);  
      legend("topleft", legend = c("Real Data", "Model"),
               text.width = strwidth("Real Data"),pch=plotchar,
               col=colors,lty=linetype)
      cat(i,'\n'); 
   } 
}
dev.off()


for( i in colnames(C) ){
   l = t(C[i]);
   k = t(M[i]);
   for (j in 1:length(l)) 
   { 
       l[j] <-  k[j]/ l[j];
   }
   C[i] = t(l)    
} 
