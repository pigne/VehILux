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
        int s=0;
        for(int i=0;i<typeNum;i++){
            if(i!=place)
                s+=((ArrayInt)var[1]).getValue(i);
        }
        int s2 = 0;
        for(int i=0;i<typeNum;i++){
            double tmp1 = (double)((ArrayInt)var[1]).getValue(i)*(100-rnd)/s;
            if(i!=place)
                ((ArrayInt)var[1]).setValue(i, (int)tmp1);
            s2+=((ArrayInt)var[1]).getValue(i);
        }
        if (s2<100)
            ((ArrayInt)var[1]).setValue(typeNum-1,((ArrayInt)var[1]).getValue(typeNum-1)+100-s2);

        int c=0;
        for(int i=0;i<typeNum;i++){
            int size = ((ArrayInt)var[2]).getValue(i);
            int[] tmp = new int[size];
            int tc = c;
            for(int j=0;j<size;j++){
                tmp[j]=((ArrayInt)var[0]).getValue(c+j);
            }
            rnd = (int)(Math.random()*100);
            if (rnd==0) rnd=1;
            place = (int)(Math.random()*size);
            ((ArrayInt)var[0]).setValue(c+place, rnd);
            s=0;
            for(int m=0;m<size;m++){
                if(m!=place)
                    s+=((ArrayInt)var[0]).getValue(c+m);
            }
            s2 = 0;
            for(int m=0;m<size;m++){
                if(m!=place)
                    ((ArrayInt)var[0]).setValue(c+m, ((ArrayInt)var[0]).getValue(m+c)*(100-rnd)/s);
                s2+=((ArrayInt)var[0]).getValue(c+m);
            }
            if (s2<100)
                ((ArrayInt)var[0]).setValue(c+size-1,((ArrayInt)var[0]).getValue(c+size-1)+100-s2);
            c+=size;
            int r= (int)(Math.random()*100);
            var[3].setValue((double)r);
        }
        solution.setDecisionVariables(var);
        return solution;
      } // execute

}
