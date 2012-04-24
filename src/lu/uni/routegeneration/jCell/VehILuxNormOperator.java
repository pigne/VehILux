package lu.uni.routegeneration.jCell;

import java.util.Vector;

import jcell.*; // uses jcell package

/**
 * @author Sune
 *
 * Wrapping operator to ensure that group sums are exactly 100 after execution according to the route generation problem. The operator can optionally discretise values to simulate integer alleles.
 */
public class VehILuxNormOperator implements Operator {

	private Operator operator;
	private Boolean discretise;
	
	/** Initializes a new instance
	 * @param operator the operator to wrap
	 * @param discretise or not
	 */
	public VehILuxNormOperator(Operator operator, Boolean discretise)
	{
		this.operator = operator;
		this.discretise = discretise;
	}
	
	@Override
	public Object execute(Object o)
	{
		//return operator.execute(o);
		RealIndividual result = (RealIndividual)operator.execute(o);
		
		RouteGenerationProblem.NormaliseIndividual(result);
		
		if (this.discretise)
		{
			RouteGenerationProblem.DiscretiseIndividual(result);
		}
		
		return result;
	}
}
