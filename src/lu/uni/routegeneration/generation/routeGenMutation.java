/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.uni.routegeneration.generation;

import jmetal.base.operator.mutation.Mutation;
import jmetal.util.*;
import jmetal.base.*;
import jmetal.base.variable.*;

/**
 *
 * @author Masoud
 */
public class routeGenMutation extends Mutation{

      public Object execute(Object object) throws JMException {

        Solution solution = (Solution )object;
        if(Math.random()<0.9) return(solution);
        Variable[] var = solution.getDecisionVariables();
        int typeNum = ((ArrayInt)var[1]).getLength();
        int rnd = (int)(Math.random()*100);
        int place = (int)(Math.random()*typeNum);
        ((ArrayInt)var[1]).setValue(place, rnd);
        int sum=0;
        for(int i=0;i<typeNum;i++){
            if(i!=place)
                sum+=((ArrayInt)var[1]).getValue(i);
        }
        int postSum = 0;
        int remaining = 100 - rnd;
        for(int i=0;i<typeNum;i++){
            double tmp1 = (double)((ArrayInt)var[1]).getValue(i)*remaining/sum;
            if(i!=place)
                ((ArrayInt)var[1]).setValue(i, (int)tmp1);
            postSum+=((ArrayInt)var[1]).getValue(i);
        }
        while (postSum<100){
            ((ArrayInt)var[1]).setValue(place,((ArrayInt)var[1]).getValue(place)+1);
            postSum++;
        }

        int startIndex=0;
        for(int i=0;i<typeNum;i++){

            int size = ((ArrayInt)var[2]).getValue(i);
            int[] tmp = new int[size];
            for(int j=0;j<size;j++){
                tmp[j]=((ArrayInt)var[0]).getValue(startIndex+j);
            }
            rnd = (int)(Math.random()*100);
            if (rnd==0) rnd=1;
            place = (int)(Math.random()*size);
            ((ArrayInt)var[0]).setValue(startIndex+place, rnd);
            
            if(size>2){
                sum=0;
                for(int m=0;m<size;m++){
                    if(m!=place)
                        sum+=((ArrayInt)var[0]).getValue(startIndex+m);
                }
                postSum = 0;
                remaining = 100 - rnd;
                for(int m=0;m<size;m++){
                    if(m!=place)
                        ((ArrayInt)var[0]).setValue(startIndex+m, ((ArrayInt)var[0]).getValue(startIndex+m)*remaining/sum);
                    postSum+=((ArrayInt)var[0]).getValue(startIndex+m);
                }
                while(postSum<100){
                    ((ArrayInt)var[0]).setValue(startIndex+place,((ArrayInt)var[0]).getValue(startIndex+place)+1);
                    postSum++;
                }
            }else{
                for(int m=0;m<size;m++){
                    if(m!=place)
                        ((ArrayInt)var[0]).setValue(startIndex+m,100-rnd);
                    else
                        ((ArrayInt)var[0]).setValue(startIndex+m,rnd);
                }
            }

            startIndex+=size;
        }

        int r= (int)(Math.random()*100);
        var[3].setValue((double)r);

        solution.setDecisionVariables(var);
        return solution;
      } // execute

}
