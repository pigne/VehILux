/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.uni.routegeneration.generation;

import com.sun.org.apache.xalan.internal.xsltc.dom.AbsoluteIterator;
import java.util.ArrayList;
import java.math.*;

/**
 *
 * @author Masoud
 */
public class EvaluationPoint {
    int[] realFlows;
    int[] estimatedFlows;
    String id;
    int sumOfDifference = 0;
    int sumOfDifferenceF1 = 0;
    int meanOfDifferencePercent = 0;

    public String toString(){
        String str = new String();
        str = id + " : ";
        for(int i=1;i<=11;i++){
            str=str + estimatedFlows[i] +" ";
        }
        return(str);
    }

    public String RealtoString(){
        String str = new String();
        str = id + " : ";
        for(int i=1;i<=11;i++){
            str=str + realFlows[i] +" ";
        }
        return(str);
    }


    public EvaluationPoint(String NodeID){
        realFlows = new int[24];
        estimatedFlows = new int[24];
        for (int i=0;i<24;i++){
            realFlows[i]=0;
            estimatedFlows[i]=0;
        }
        id = NodeID;
    }

    public void updateEstimate(String path,int hour){
        if(path.contains(id)) estimatedFlows[hour]++;
    }

    public void setRealFlows(int[] rf,int startH,int stopH){
        for(int i=startH;i<=stopH;i++){
            realFlows[i] = rf[i];
        }
    }

    public void reset(){
        for (int i=0;i<24;i++){
            estimatedFlows[i]=0;
        }
        sumOfDifference = 0;
        sumOfDifferenceF1 = 0;
        meanOfDifferencePercent = 0;

    }

    public void updateFitness(int startH, int stopH){
        sumOfDifference = 0;
        sumOfDifferenceF1 = 0;
        meanOfDifferencePercent = 0;
        for(int i=startH; i<=stopH; i++){

            double d = Math.abs(realFlows[i]-estimatedFlows[i]);
            sumOfDifference +=d;

            double max = Math.max(realFlows[i],estimatedFlows[i]);
            double min = Math.min(realFlows[i],estimatedFlows[i]);

            if(realFlows[i]<1 || estimatedFlows[i]<1){
                sumOfDifferenceF1 += d;
            } else{
                sumOfDifferenceF1 += (d*Math.log10(max/min));
            }

            if(realFlows[i]<100)
                meanOfDifferencePercent +=d;
            else
                meanOfDifferencePercent +=(d*100/realFlows[i]);
        }

        meanOfDifferencePercent = (int)(meanOfDifferencePercent/(stopH-startH+1));

    }



}
