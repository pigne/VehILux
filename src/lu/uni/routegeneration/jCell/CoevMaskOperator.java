package lu.uni.routegeneration.jCell;

import jcell.*;

/**
 * Operator used to wrap another operator preventing it from touching the alleles masked out by the mask. The purpose is to allow an island to work on a defined subset of alleles.
 * @author Sune
 *
 */
public class CoevMaskOperator implements Operator {

	private Operator operator;
	private Boolean[] mask;
	
	/** Initializes a new instance
	 * @param operator the operator to wrap
	 * @param mask the mask to apply after each operator execution
	 */
	public CoevMaskOperator(Operator operator, Boolean[] mask)
	{
		this.operator = operator;
		this.mask = mask;
	}

	public Boolean[] getMask()
	{
		return this.mask;
	}
	
	@Override
	public Object execute(Object o) {
		// the original unchanged individual
		Individual reference = null;
		
		if (o instanceof Individual[])
		{
			Individual iv[] = (Individual[])o;
			reference = (Individual)iv[0].clone();
		}
		else if (o instanceof Individual)
		{
			reference = (Individual)((Individual)o).clone();
		}
		 
		// call the actual operator
		Individual result = (Individual)this.operator.execute(o);
		
		if (reference != null)
		{
			for(int locus = 0; locus< this.mask.length; locus++)
			{
				// if mask at locus is not true, revert back to reference state
				if (!mask[locus])
				{
					result.setAllele(locus, reference.getAllele(locus)) ;
				}
			}
		}
		else
		{
			// TODO produce error
		}
		
		return result;
	}
}
