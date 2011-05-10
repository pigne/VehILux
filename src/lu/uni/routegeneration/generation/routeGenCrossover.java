/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.uni.routegeneration.generation;

import jmetal.base.operator.crossover.Crossover;
import jmetal.base.*;
import jmetal.util.*;
import jmetal.base.variable.*;

/**
 *
 * @author Masoud
 */
public class routeGenCrossover extends Crossover {

      public Object execute(Object object) throws JMException {
        Solution [] parents = (Solution [])object;
        Solution [] offSpring = new Solution[2];
        offSpring[0] = new Solution(parents[0]);
        offSpring[1] = new Solution(parents[1]);
        Variable[] var0 = offSpring[0].getDecisionVariables();
        Variable[] var1 = offSpring[1].getDecisionVariables();
        int typeNum = ((ArrayInt)var0[1]).getLength();

//        if(Math.random()>0.5){
            System.out.println("here");
            int[] t0 = new int[typeNum];
            int[] t1 = new int[typeNum];
            for(int j=0;j<typeNum;j++){
                t0[j]=((ArrayInt)var0[1]).getValue(j);
                t1[j]=((ArrayInt)var1[1]).getValue(j);
            }
            crossProbs(t0, t1, typeNum);
            for(int j=0;j<typeNum;j++){
                ((ArrayInt)var0[1]).setValue(j, t0[j]);
                ((ArrayInt)var1[1]).setValue(j, t1[j]);
            }

  /*          if(Math.random()>0.5){
                int s1 = ((ArrayInt)var0[0]).getLength();
                for(int j=0;j<s1;j++){
                    int tmp = ((ArrayInt)var0[0]).getValue(j);
                    ((ArrayInt)var0[0]).setValue(j,((ArrayInt)var1[0]).getValue(j) );
                    ((ArrayInt)var0[0]).setValue(j,tmp);
                }
            }*/

//        }else{

            int c = 0;
            for(int i=0;i<typeNum;i++){
                int size = ((ArrayInt)var0[2]).getValue(i);
                int[] tmp0 = new int[size];
                int[] tmp1 = new int[size];
                int tc = c;
                for(int j=0;j<size;j++){
                    tmp0[j]=((ArrayInt)var0[0]).getValue(c);
                    tmp1[j]=((ArrayInt)var1[0]).getValue(c);
                    c++;
                }
                crossProbs(tmp0, tmp1, size);
                for(int j=0;j<size;j++){
                    ((ArrayInt)var0[0]).setValue(tc, tmp0[j]);
                    ((ArrayInt)var1[0]).setValue(tc, tmp1[j]);
                    tc++;
                }
            }

    /*        if(Math.random()>0.5){
                int s1 = ((ArrayInt)var0[1]).getLength();
                for(int j=0;j<s1;j++){
                    int tmp = ((ArrayInt)var0[1]).getValue(j);
                    ((ArrayInt)var0[1]).setValue(j,((ArrayInt)var1[1]).getValue(j) );
                    ((ArrayInt)var0[1]).setValue(j,tmp);
                }
            }*/

  //      }

        double r0 = (double)var0[3].getValue();
        double r1 = (double)var1[3].getValue();
        var0[3].setValue((r0+r1)/2);
        var1[3].setValue((r0+r1)/2);

        offSpring[0].setDecisionVariables(var0);
        offSpring[1].setDecisionVariables(var1);
        return offSpring;//*/
      } // execute

      public void crossProbs(int[] p1, int[] p2, int size){
          int i1 = (int)(Math.random() * size);
          int i2 = (int)(Math.random() * size);
          int tmp;
          tmp = p2[i2];
          p2[i2] = p1[i1];
          p1[i1] = tmp;


          int sum = 0;
          for(int i=0;i<size;i++){
              sum+=p1[i];
          }
          sum-=p1[i1];
          int s = 0;
          for(int i=0;i<size;i++){
//              System.out.println(i+"-"+i1+"-"+p1[i]+"-"+sum+"-"+s);
              if(i!=i1)
                p1[i] = (int)(double)p1[i]*(100-p1[i1])/sum;
              s+=p1[i];
          }
          if(s<100) p1[size-1]+=(100-s);

          sum = 0;
          for(int i=0;i<size;i++){
              sum+=p2[i];
          }
          sum-=p2[i2];
          s = 0;
          for(int i=0;i<size;i++){
              if(i!=i2)
                p2[i] = (int)(double)p2[i]*(100-p2[i2])/sum;
              s+=p2[i];
          }
          if(s<100) p2[size-1]+=(100-s);
      }

}
