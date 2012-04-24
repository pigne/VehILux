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
		variables = 13; 
		maxFitness = 0.0;
		
		
		//Set the maximum and minimum values for each of the solution variables 
		//Structure  Tr/Ti/Tc/Zc1/Zc2/Zc3/Zcd/Zi1/Zid/Zr1/Zrd/IR/SR
		
		double minValues[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 30, 20};
		double maxValues[] = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 70, 80};

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

	public static Vector<Double> array2vector(double source[]){
	    Vector<Double> dest = new Vector<Double>();

	    for (int i=0; i<source.length; ++i){
	        dest.add(source[i]);
	    }
	    return dest;
	 }
}
