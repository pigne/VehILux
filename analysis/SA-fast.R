require(rJava)
library(foreign)

setwd("lib")
a<-list.files()
setwd("..")
.jinit(classpath=paste(getwd(),"/bin/",sep=""),parameters="-Xmx2000m")

jars<-grep(".*jar", a, value=TRUE)
for(jar in jars){
.jaddClassPath(paste(getwd(),"/lib/",jar,sep=""))
}




#rg <- new(J("lu.uni.routegeneration.r.RTest")) 
rg<-.jnew("lu/uni/routegeneration/generation/RouteGeneration")
params = rg$getParametersNames()
params
boundaries = rg$getParametersBoundaries()
bmin = boundaries[1,]
bmax = boundaries[2,]




library("sensitivity")

bmin=c(1,1,1,0.5,1,1,1,1,1)
bmax=c(100,100,100,0.8,10,10,10,10,10)
params = c("Residential Type","Industrial Type","Commercial Type","Inner Traffic Ratio","COM(1)","COM(2)","COM(3)","IND(4)","RES(5)")


q_arg=list()
for(i in 1:length(params)){
  q_arg[[i]]=list(min=bmin[[i]], max=bmax[[i]])
}



fast <- fast99 (model=NULL,
		factors=params,
		n=70,
		q = "qunif",
		q.arg = q_arg
		)
length(fast$X[,1])

y=c()
for(i in 1:length(fast$X[,1])){
  raw = c()
  for(i in fast$X[i,]){raw=c(raw,i)}
  res=rg$fitness(raw)
  cat(res,"\n")
  y=c(y,res)
}

tell(fast, y)
params = c("Residential Type","Industrial Type","Commercial Type","Inner Traffic Ratio","COM(1)","COM(2)","COM(3)","IND(4)","RES(5)")

############ plot FAST
pdf("sa-FAST-0.4-0.7.pdf",width=6,height=6)
par(las=2, mar=c(8,3,1,1))
ylim = c(0, 1)
S <- rbind(fast$D1 / fast$V, 1- fast$Dt/fast$V - fast$D1 / fast$V)
colnames(S) <- colnames(params)
bar.col <- c("white","lightgray")
barplot(S, ylim = ylim, col = bar.col,names.arg=params)
legend("topright", c("Main Effect", "Interactions"), fill = bar.col, bty='n')
dev.off()



#####   Inner traffic over Y
pdf("inner_over_fitness-0.4-0.7.pdf",width=6,height=6)
layout(matrix(1))
par(mar=c(3,2,1,1),mgp=c(2,0.8,0))
inner_over_y <-data.frame(fast$X[[4]],fast$y)
colnames(inner_over_y)<-c("inner","y")
inner_over_y <- inner_over_y[with(inner_over_y, order(inner)), ]
par(mar=c(5,5,1,1),mgp=c(4,1,0))
plot(inner_over_y, xlab="Inner traffic ratio", ylab="Model Fitness",col="#999999",bg="#F0F0F0",pch=21); grid()
dev.off()


###### all params  over fitness
pdf("params_over_fitness-0.4-0.7.pdf",width=6,height=6)
par(las=0,mar=c(3,2,1,1),mgp=c(2,0.8,0))

layout(matrix(1:9,ncol=3,byrow=TRUE))

for(i in 1:length(params)){
param_over_y <-data.frame(fast$X[[i]],fast$y)
colnames(param_over_y)<-c("param","y")
param_over_y <- param_over_y[with(param_over_y, order(param)), ]
plot(param_over_y, xlab=params[i], ylab="",col="#444444",pch=19, cex=.1); grid()
}
dev.off()



par(las=0,mar=c(3,2,1,1),mgp=c(2,0.8,0))

layout(matrix(1:9,ncol=3,byrow=TRUE))

for(i in 1:length(params)){
param_over_y <-data.frame(fast$X[[i]],fast$X[[4]])
colnames(param_over_y)<-c("param","inner")
param_over_y <- param_over_y[with(param_over_y, order(param)), ]
plot(param_over_y, xlab=params[i], ylab="",col="#444444",pch=19, cex=.1); grid()
}







