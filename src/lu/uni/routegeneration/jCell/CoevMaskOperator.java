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
	private int length;
	
	/** Initializes a new instance
	 * @param operator the operator to wrap
	 * @param mask the mask to apply after each operator execution
	 */
	public CoevMaskOperator(Operator operator, Boolean[] mask)
	{
		this.operator = operator;
		this.mask = mask;
		this.length = 0;
		
		for(int locus = 0; locus< this.mask.length; locus++)
		{
			if (mask[locus])length++;
		}
	}
	
	@Override
	public String toString()
	{
		return "Masked: " + this.operator.toString();
	}

	public Boolean[] getMask()
	{
		return this.mask;
	}
	
	/**
	 * Creates a shorter Individual corresponding only to the alleles of a single island (determined by the mask)
	 * @param individual the individual to copy
	 * @return shorter individual of unmasked values
	 */
	private Individual buildIslandIndividual(Individual individual)
	{
		Individual result = (Individual)individual.clone();
		result.setLength(this.length);
		
		int islandLocus = 0;		
		for(int locus = 0; locus< this.mask.length; locus++)
		{
			if (mask[locus])
			{
				result.setAllele(islandLocus++, individual.getAllele(locus)) ;
			}
		}
		
		return result;
	}
	
	@Override
	public Object execute(Object o) {
		// the original unchanged individual
		Individual reference = null;

		Object islandIndividual = null;
		if (o instanceof Individual[])
		{
			Individual iv[] = (Individual[])o;
			reference = (Individual)iv[0].clone();
			islandIndividual = new Individual[]{buildIslandIndividual((Individual)iv[0]), buildIslandIndividual((Individual)iv[1])};
		}
		else if (o instanceof Individual)
		{
			reference = (Individual)((Individual)o).clone();
			islandIndividual = buildIslandIndividual(reference);
		}
		 
		// call the actual operator on the smaller temp Individual corresponding to the alleles not masked out
		Individual tempResult = (Individual)this.operator.execute(islandIndividual);
		Individual result = (Individual)reference.clone();
		
		int tempLocus = 0;		
		if (reference != null)
		{
			for(int locus = 0; locus< this.mask.length; locus++)
			{
				if (mask[locus])
				{
					// if mask at locus is true, set it to value of the result
					result.setAllele(locus, tempResult.getAllele(tempLocus++));
				}
				else
				{
					// else, set to reference state (the values before this operator)
					result.setAllele(locus, reference.getAllele(locus));
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
