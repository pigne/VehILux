package lu.uni.routegeneration.jCell;

import java.util.*;

import jcell.Individual;
import jcell.Problem;
import jcell.Target;


/**
 * @author Sune
 * 
 * Test problem wit faster execution time mainly to test co-evolution and masks
 */
public class RouteGenerationProblemTest extends Problem {

	private int delay = 1;
	private Random r = new Random();
	
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
	
	@Override
	public Object eval(Individual ind) {
		// the fitness is 0.0 when all values are either 25, 33, 34 or 50 
		
		double fitness = 0;
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
		return fitness;
	}

}
