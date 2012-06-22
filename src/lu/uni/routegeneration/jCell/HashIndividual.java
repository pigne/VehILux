package lu.uni.routegeneration.jCell;

import java.util.Random;
import jcell.Individual;

/**
 * @author Sune
 * This class wraps an Individual to implement functions needed to store Individuals in a HashMap.
 * The code here should be within the Individual in jCell directly.
 */
public class HashIndividual
{
	 Individual individual;
	 
	 public HashIndividual(Individual individual)
	 {
		 this.individual = (Individual)individual.clone();
	 }

	 @Override
	 public int hashCode()
	 {
		double multiplier = 1.0;
		double value = 0.0;
		for(int locus = 0; locus < this.individual.getLength(); locus++)
		{
			Double allele = (Double)this.individual.getAllele(locus);
			value += allele * multiplier;
			multiplier *= 100.0; // specific to RouteGeneration, should use min/max-values
		}
		
		Integer code = new Integer(0);
		code = (int)(code.MAX_VALUE * value / multiplier);
		
		return code;
	 }

	@Override
	public boolean equals(Object o)
	{
		HashIndividual otherIndividual = (HashIndividual)o;
		
		for(int locus = 0; locus < this.individual.getLength(); locus++)
		{
			Object allele = this.individual.getAllele(locus);
			Object otherAllele = otherIndividual.individual.getAllele(locus);
			
			if (!allele.equals(otherAllele))
				return false;		
		}
		
		return true;
	}
}
