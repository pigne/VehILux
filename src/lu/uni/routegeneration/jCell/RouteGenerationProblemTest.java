package lu.uni.routegeneration.jCell;

import java.util.*;

import jcell.Individual;
import jcell.Problem;
import jcell.RealIndividual;
import jcell.Target;
import java.util.Set;

/**
 * @author Sune
 * 
 * Test problem wit faster execution time mainly to test co-evolution and masks
 */
public class RouteGenerationProblemTest extends Problem {

	private int delay = 1;
	private Random r = new Random();
	
	public static Individual bestIndividual = new RealIndividual();
	private static double bestFitness = 1.7976931348623157E308; //new Double(0).MAX_VALUE;
		
	public RouteGenerationProblemTest()
	{
        super();

        Double minValues[] = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 30.0, 20.0};
		Double maxValues[] = {100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 70.0, 80.0};
        
        Target.maximize = false;
        this.variables = minValues.length;
        this.maxFitness = 0.0;

        minAllowedValues = new Vector<Double>(Arrays.asList(minValues));
        maxAllowedValues = new Vector<Double>(Arrays.asList(maxValues));
        
        delay = 1;
	}
	
	public String getCurrentDectectors()
	{
		return "1 2 3 4 5";
	}
	
	@Override
	public Object eval(Individual ind) {
		// the fitness is 0.0 when all values are either 25, 33, 34 or 50 
		
		RouteGenerationProblem.NormaliseIndividual(ind);
		
		if(RouteGenerationProblem.discrete)
		{
			RouteGenerationProblem.DiscretiseIndividual(ind);
		}
				
		double fitness = 0;
		Boolean skipEvaluation = false;
		
		synchronized (bestIndividual)
		{
			if (RouteGenerationProblem.evaluatedIndividuals.containsKey(new HashIndividual(ind)))
			{
				fitness = RouteGenerationProblem.evaluatedIndividuals.get(new HashIndividual(ind));
				skipEvaluation = true;
				RouteGenerationProblem.skipCount++;
			}
		}
		
		if (skipEvaluation)
		{
			System.out.println("skip: " + ind.toString());			
		}
		else
		{	
			for(int i = 0; i < minAllowedValues.size(); i++)
			{
				double delta = (25.0d - (double)ind.getAllele(i)) * (33.0d - (double)ind.getAllele(i)) * (34.0d - (double)ind.getAllele(i)) * (50.0d - (double)ind.getAllele(i));
				fitness += Math.abs(delta); 
			}
			
			try {
				// delay = 1 + r.nextInt(5);
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			synchronized (bestIndividual)
			{
				if (!RouteGenerationProblem.evaluatedIndividuals.containsKey(new HashIndividual(ind)))
				{
					RouteGenerationProblem.evaluatedIndividuals.put(new HashIndividual(ind), fitness);
				}
			}
		}
		
		synchronized (bestIndividual)
		{
			if (fitness < bestFitness)
			{
				bestFitness = fitness;				
				bestIndividual = (Individual)ind.clone();
			}
		}
		
		return fitness;
	}

}
