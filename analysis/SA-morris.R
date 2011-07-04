require(rJava)
library(foreign)

setwd("lib")
a<-list.files()
setwd("..")
.jinit(classpath=paste(getwd(),"/bin/",sep=""),parameters="-Xmx2g")

jars<-grep(".*jar", a, value=TRUE)
for(jar in jars){
.jaddClassPath(paste(getwd(),"/lib/",jar,sep=""))
}


rg<-.jnew("lu/uni/routegeneration/generation/RouteGeneration")
params = rg$getParametersNames()
params
boundaries = rg$getParametersBoundaries()
bmin = boundaries[1,]
bmax = boundaries[2,]


bmin=c(1,1,1,0,1,1,1,1,1)
bmax=c(100,100,100,1,10,10,10,10,10)
params = c("Residential Type","Industrial Type","Commercial Type","Inner Traffic Ratio","COM(1)","COM(2)","COM(3)","IND(4)","RES(5)")


library("sensitivity")
sa <- morris (model=NULL, 
		factors=params,
		r=10,
		design=list( type="oat", levels=4, grid.jump=2),
		binf=bmin,
		bsup=bmax
		)
#write.table (sa$X, row.names=FALSE, col.names=FALSE)
#y= matrix(nrow=length(sa$X[,1]),ncol=1)
y=c()
for(i in 1:length(sa$X[,1])){
  res=rg$fitness(sa$X[i,])
  res
  y=c(y,res)
}

tell(sa, y)


plot(sa, xlim=c(0,1), main="r=30")



##### plot Morris
identify=TRUE
par(mar=c(3,3,1,1),mgp=c(2,0.8,0))


  mu.star <- apply(sa$ee, 2, function(x) mean(abs(x)))
  sigma <- apply(sa$ee, 2, sd)
  
  plot(mu.star, sigma, xlab = expression(mu^"*"),
       ylab = expression(sigma), col="darkgray",bg="#EEEEEE",pch=21,cex=1.3)
  grid()
  if (identify) {
    identify(mu.star, sigma, labels = params)
  } else {
    text(mu.star, sigma, labels = colnames(sa$ee), pos = 4)
  }
  
  dev.copy(device=pdf, width=6,height=6, file="sa-Morris-2.pdf")
  
  
  
  
  

  par(mar=c(4,4,1,1),mgp=c(3,1,0))

  layout(matrix(1:9,ncol=3,byrow=TRUE))

  for(i in 1:length(params)){
  param_over_y <-data.frame(sa$X[,i],sa$X[,4])
  colnames(param_over_y)<-c("param","inner")
  param_over_y <- param_over_y[with(param_over_y, order(param)), ]
  plot(param_over_y, xlab=params[i], ylab="",col="gray",bg="#F5F5F5",pch=21, cex=1.1); grid()
  }
