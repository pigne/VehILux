data<-read.table("all_types.log",header=FALSE)

colnames(data)= c('type','surface')



sm.density.compare(data$surface, data$type, xlab="Surface" )





