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
        ArrayInt array_type_probs0 = (ArrayInt)var0[1];
        ArrayInt array_type_probs1 = (ArrayInt)var1[1];
        ArrayInt area_probs0 = (ArrayInt)var0[0];
        ArrayInt area_probs1 = (ArrayInt)var1[0];
        ArrayInt area_nums = (ArrayInt)var0[2];
        int typeNum = ((ArrayInt)var0[1]).getLength();

        int chrom_length = 1 + 1 + typeNum; //  number of genes: area type probs, plus shifting ratio, plus area probs
        int place = (int)(Math.random()*(chrom_length-1))+1; // random place is >=1 and <chrom_length
        //if (place>chrom_length) place = chrom_length;

        //System.out.println("crossover point:"+place);

        if (place>=1){ //exchanging area type probs
//      if (Math.random()>0.5){ //exchanging area type probs
            for(int i=0;i<typeNum;i++){
                int tmp = array_type_probs0.getValue(i);
                array_type_probs0.setValue(i, array_type_probs1.getValue(i));
                array_type_probs1.setValue(i, tmp);
            }
        }

        if (place>=2){ //exchanging shifting ratio
//        if (Math.random()>0.5){ //exchanging shifting ratio
            int tmp = (int)((Int)var0[3]).getValue();
            ((Int)var0[3]).setValue(((Int)var1[3]).getValue());
            ((Int)var1[3]).setValue(tmp);
        }

        int area_count = place - 2; //number of area types whose area probibilities are to be exchanged
        int index=0;
        for(int i=0;i<area_count;i++){
            int n = (int)(area_nums.getValue(i)); //number of areas in this area type
            for(int j=0;j<n;j++){
                int tmp = area_probs0.getValue(index+j);
                area_probs0.setValue(index+j, area_probs1.getValue(index+j));
                area_probs1.setValue(index+j, tmp);
            }
            index +=n;
        }
        offSpring[0].setDecisionVariables(var0);
        offSpring[1].setDecisionVariables(var1);
        return offSpring;//*/
      }

}
