/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.uni.routegeneration.generation;
import jmetal.base.*;
import jmetal.util.*;
/**
 *
 * @author Masoud
 */
public class testProblem extends Problem{

    public testProblem(){
                numberOfVariables_  = 3;
                numberOfObjectives_ = 1;
                //numberOfConstraints_= 0;
                problemName_        = "testProblem";

                try{
                    solutionType_ = new RouteGenSolutionType(this) ;
                    variableType_ = new Class[numberOfVariables_] ;
                    length_       = new int[numberOfVariables_];
                    variableType_[0] = Class.forName("jmetal.base.variable.Int") ;
                    variableType_[1] = Class.forName("jmetal.base.variable.Int") ;
                    variableType_[2] = Class.forName("jmetal.base.variable.Int") ;

                } catch(ClassNotFoundException e) {
                    System.out.println("class not found exceptiion");
                }

    }

    public void evaluate(Solution solution) {
        Variable[] vars = solution.getDecisionVariables();
        int residential_proba = 0;
        int commercial_proba = 0;
        int industrial_proba = 0;
        try{
            residential_proba = (int)(vars[0].getValue());//5;
            commercial_proba = (int)(vars[1].getValue());//80;
            industrial_proba = (int)(vars[2].getValue());//15;
        }catch(JMException e){
            System.out.println("JME exception");
        }

        double mean = (residential_proba+commercial_proba+industrial_proba)/3;
        solution.setFitness(mean);
        solution.setObjective(0, mean);

    }
}
