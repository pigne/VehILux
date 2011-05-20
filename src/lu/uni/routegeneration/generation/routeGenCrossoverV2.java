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
public class routeGenCrossoverV2 extends Crossover {

      public Object execute(Object object) throws JMException {
        Solution [] parents = (Solution [])object;
        Solution [] offSpring = new Solution[2];
        offSpring[0] = new Solution(parents[0]);
        offSpring[1] = new Solution(parents[1]);
        Variable[] var0 = offSpring[0].getDecisionVariables();
        Variable[] var1 = offSpring[1].getDecisionVariables();
        int typeNum = ((ArrayInt)var0[1]).getLength();

/*        int chrom_length = 1 + 1 + typeNum; //  area type probs, plus shifting ratio, plus area probs
        int place = (int)(Math.random()*chrom_length)+1;
        if (place>chrom_length) place = chrom_length;

//        if (place>=1){ //exchanging area type probs
        if (Math.random()>0.5){ //exchanging area type probs
            for(int i=0;i<typeNum;i++){
                int tmp = ((ArrayInt)var0[1]).getValue(i);
                ((ArrayInt)var0[1]).setValue(i, ((ArrayInt)var1[1]).getValue(i));
                ((ArrayInt)var1[1]).setValue(i, tmp);
            }
        }

//        if (place>=2){ //exchanging shifting ratio
        if (Math.random()>0.5){ //exchanging shifting ratio
            int tmp = (int)((Int)var0[3]).getValue();
            ((Int)var0[3]).setValue(((Int)var1[3]).getValue());
            ((Int)var1[3]).setValue(tmp);
        }

        int count = 0;
        for(int j=0; j<(place-2); j++){
            count+=(int)((ArrayInt)var0[2]).getValue(j);
        }
        for(int j=0;j<count;j++){
            int tmp = ((ArrayInt)var0[0]).getValue(j);
            ((ArrayInt)var0[0]).setValue(j, ((ArrayInt)var1[0]).getValue(j));
            ((ArrayInt)var1[0]).setValue(j, tmp);
        }
*/

        if (Math.random()>0.5){ //exchanging area type probs
            for(int i=0;i<typeNum;i++){
                int tmp = ((ArrayInt)var0[1]).getValue(i);
                ((ArrayInt)var0[1]).setValue(i, ((ArrayInt)var1[1]).getValue(i));
                ((ArrayInt)var1[1]).setValue(i, tmp);
            }
        }

        if (Math.random()>0.5){ //exchanging shifting ratio
            int tmp = (int)((Int)var0[3]).getValue();
            ((Int)var0[3]).setValue(((Int)var1[3]).getValue());
            ((Int)var1[3]).setValue(tmp);
        }

        int startIndex = 0;
        for(int i=0; i<typeNum; i++){
            int size = (int)((ArrayInt)var0[2]).getValue(i);
            if (Math.random()>0.5){ //exchanging shifting ratio
                for(int j=0;j<size;j++){
                    int tmp = ((ArrayInt)var0[0]).getValue(startIndex+j);
                    ((ArrayInt)var0[0]).setValue(startIndex+j, ((ArrayInt)var1[0]).getValue(startIndex+j));
                    ((ArrayInt)var1[0]).setValue(startIndex+j, tmp);
                }
            }
            startIndex+=size;
        }

        offSpring[0].setDecisionVariables(var0);
        offSpring[1].setDecisionVariables(var1);
        return offSpring;//*/
      }

}
