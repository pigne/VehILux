n=0;

for file in `ls`;
do
tail +28 >> temp_$n.txt;
n=n+1;
done