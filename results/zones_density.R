 ind<-read.table("ind.log",header=FALSE)
 com<-read.table("com.log",header=FALSE)
 res<-read.table("res.log",header=FALSE)

colnames(ind)= c('Industrial');
colnames(com)= c('Commercial');
colnames(res)= c('Residential');


from=-10000;
to=300000
d_ind = density(t(ind),from=from,to=to)
d_res = density(t(res),from=from,to=to)
d_com = density(t(com),from=from,to=to)


colors<- c('Gray60','Steelblue',"Darkgoldenrod","Olivedrab")


ylim=c(0,0.00003)
par(mfrow=c(3,1), cex=0.7)
par(mai=c(0.6,0.7,.6,.5))

plot(d_res,main="Residential",ylim=ylim)
plot(d_ind,main="Industrial",ylim=ylim)
plot(d_com,main="Commercial",ylim=ylim,)



############################################################


#
# bar chart
#

s_com=sum(com)
s_ind=sum(ind)
s_res=sum(res)


S = s_com+s_ind+s_res
s_res=s_res/S
s_ind=s_ind/S
s_com=s_com/S


sum_normal=c(s_com,s_ind,s_res)

 pind<-read.table("proba_ind.log",header=FALSE)
 pcom<-read.table("proba_com.log",header=FALSE)
 pres<-read.table("proba_res.log",header=FALSE)
s_pcom=sum(pcom)
s_pind=sum(pind)
s_pres=sum(pres)


sum_proba=c(s_pcom,s_pind,s_pres)

mat = matrix(c(sum_normal,sum_proba),ncol=2)
rownames(mat)<-c('Commercial','Industrial','Residential')
colnames(mat)<-c('Original Distribution','Biased Distribution')

par(pin=c(4,5))
 
par(mai=c(0.6,0.7,.6,.5))





barplot(mat,beside=FALSE,names.arg=colnames(mat),legend = rownames(mat))




dotchart(sort,cex=.4,groups= data$type,gcolor="black",bg="gray", panel = function (x, y) { panel.xyplot(x, y, pch = 16, col = "black"); panel.segments()})
 
#####################################################################

 ind<-read.table("ind.log",header=FALSE)
 com<-read.table("com.log",header=FALSE)
 res<-read.table("res.log",header=FALSE)


pdf("zones_density.pdf")
colnames(ind)= c('Ind.');
colnames(com)= c('Commercial');
colnames(res)= c('Residential');

mean_surf=c(mean(com),mean(ind),mean(res))
card= c(length(t(com)),length(t(ind)),length(t(res)))


par(mai=c(3.5,1,.6,.5))
barplot(height=mean_surf,width=card,space=0,col=c("gray81","gray50","gray68"),xlab="Number of zones",ylab="Average surface per zone type (m^2)")
dev.off()

