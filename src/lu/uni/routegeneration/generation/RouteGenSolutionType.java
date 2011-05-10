/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.uni.routegeneration.generation;
import java.util.Random;
import jmetal.base.*;
import jmetal.base.variable.*;
import jmetal.util.JMException;
/**
 *
 * @author Masoud
 */

public class RouteGenSolutionType extends SolutionType {


	public RouteGenSolutionType(Problem problem) throws ClassNotFoundException {
		super(problem) ;
		problem.variableType_ = new Class[problem.getNumberOfVariables()];
		problem.setSolutionType(this) ;

		// Initializing the types of the variables
                problem.variableType_[0] = Class.forName("jmetal.base.variable.ArrayInt");   //areas probs
                problem.variableType_[1] = Class.forName("jmetal.base.variable.ArrayInt");   //area type probs
                problem.variableType_[2] = Class.forName("jmetal.base.variable.ArrayInt");   //area type parts
                problem.variableType_[3] = Class.forName("jmetal.base.variable.Int");        //insideFlowRate
	} // Constructor

	/**
	 * Creates the variables of the solution
	 * @param decisionVariables
	 */
	public Variable[] createVariables() {
		Variable[] variables = new Variable[problem_.getNumberOfVariables()];
                RouteGeneration pr = (RouteGeneration)problem_;
                variables[1] = new ArrayInt(pr.getAreaTypeNum());
                variables[2] = new ArrayInt(pr.getAreaTypeNum());
                variables[3] = new Int(0,100);

                int sumnum = 0;
                for(int i=0;i<pr.getAreaTypeNum();i++){
                    sumnum+=pr.getAreaParts(i);
                    try{
                    ((ArrayInt)variables[2]).setValue(i, pr.getAreaParts(i));
                    }catch(JMException e){
                        System.out.println("jmetal exception");
                    }
                }
                variables[0] = new ArrayInt(sumnum);
                int[] vt = split(pr.getAreaTypeNum());

                for(int i=0;i<pr.getAreaTypeNum();i++){
                    try{
                    ((ArrayInt)variables[1]).setValue(i, vt[i]);
                    }catch(JMException e){

                        System.out.println("jmetal exception");
                    }
                }

                int index = 0;
                for(int i=0;i<pr.getAreaTypeNum();i++){
                    int[] ap = split(pr.getAreaParts(i));
                    for(int j=0;j<pr.getAreaParts(i);j++){
                        try{
                            ((ArrayInt)variables[0]).setValue(index, ap[j]);
                        }catch(JMException e){
                            System.out.println("jmetal exception");
                        }
                        index++;
                    }
                }

                return variables ;
	} // createVariables


        //this function splits 100 to n random length part and puts lengths to array n.
        public int[] split(int n){
            int[] s = new int[n];
            int[] temp = new int[n-1];
            for(int i=0;i<n-1;i++){
                int r = (int)(Math.random()*100);
                temp[i] = r;
            }
            for(int i=0;i<n-1;i++){
                for(int j=i+1;j<n-1;j++){
                    if (temp[j]<temp[i]){
                        int t = temp[j];
                        temp[j] = temp[i];
                        temp[i] = t;
                    }
                }
            }
            int last = 0;
            for(int i=0;i<n-1;i++){
                s[i] = temp[i] - last;
                last = temp[i];
            }
            s[n-1] = 100 - last;
            return(s);
        }
  
}
