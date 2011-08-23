package lu.uni.routegeneration.jCell;

import java.util.Vector;

import jcell.Individual;
import jcell.Problem;
import jcell.Target;
import lu.uni.routegeneration.generation.RouteGeneration;

public class RouteGenerationProblem extends Problem{

	RouteGeneration routeGen;
	
	public RouteGenerationProblem(){
		super();
	
		Target.maximize = false;
		variables = 9; 
		maxFitness = 0.0;
		
		
		//Set the maximum and minimum values for each of the solution variables 
		//Structure  Tr/Tc/Ti/IR/Zr1/Zc1/Zc2/Zc3/ZI1
		
		Integer minValues[] = {1, 1, 1, 30, 1, 1, 1, 1, 1};
		Integer maxValues[] = {100, 100, 100, 70, 100, 100, 100, 100, 100};

		minAllowedValues = array2vector(minValues);
		maxAllowedValues = array2vector(maxValues);
	    
		routeGen = new RouteGeneration();
	}
	
	
	@Override
	public Object eval(Individual ind) {
		
		//for(int i = 0; i < ind.getLength(); i++)
			//System.out.printf("Ind("+i +"):" + ind.getAllele(i));
		
		return routeGen.evaluate(ind);
	}

	//This function is to copy an array into a vector

	public static Vector<Integer> array2vector(Integer source[]){
	    Vector<Integer> dest = new Vector<Integer>();

	    for (int i=0; i<source.length; ++i){
	        dest.add(source[i]);
	    }
	    return dest;
	 }
	
}
