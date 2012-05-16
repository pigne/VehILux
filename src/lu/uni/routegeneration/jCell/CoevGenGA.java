/**
 * @author Bernabe Dorronsoro 
 * @author Sune Nielsen - re-evaluate elite individual for coevolution  
 * Description
 * Generational GA with elitism
 * 
 */

package lu.uni.routegeneration.jCell;
import java.util.Random;

import jcell.*;

public class CoevGenGA extends EvolutionaryAlg
{
   public CoevGenGA(Random r)
   {
      super(r);
   }
   
   public void experiment()
   {
	   
	   // Could be used as a multiobjective algorithm, but it is not tested.
	   boolean multiobjective = problem.numberOfObjectives() > 1;
	   
      double optimum; // Best fitness value in the population

      Operator oper;
      Individual iv[] = new Individual[2]; // Used for crossover
	Integer ind[] = new Integer[2]; // Used for avoiding that the same individual is selected twice in selection
      
	Population auxPop = new Population(population.getPopSize());
		
    problem.reset(); // Set the number of evaluations to 0
    problem.evaluatePopulation(population); 
    
    int worst = 0, best = 0;
    if (multiobjective) // Only if we are solving a multiobjective problem
    {
  	  paretoFront.initialize(population);
	      // Get the position of the best and worst individual in the population
	      for (int i=1; i<population.getPopSize(); i++)
	      {
	    	if (Target.isWorse(population.getIndividual(i), population.getIndividual(worst)))
	   			  worst = i;
	    	if (Target.isBetter(population.getIndividual(i), population.getIndividual(best)))
	   			  best = i;
	      }
    }
	else // if the problem is not multiobjective
	{
	  // Compute some statistic measures from the population
      statistic.calculate(population);
    
	  if (Target.maximize) // if it is a maximization problem
	  	optimum = ((Double)statistic.getStat(SimpleStats.MAX_FIT_VALUE)).doubleValue();
	  else  // if it is a minimization problem
		  optimum = ((Double)statistic.getStat(SimpleStats.MIN_FIT_VALUE)).doubleValue();
	  
	  if (Target.isBetterOrEqual(optimum, targetFitness))
		  return; // stop if the solution is found
	}

    generationNumber = 0;
    listener.generation(this);
             
      // For the termination condition we can set either a max number of generations or a max number of evaluations
      while ((problem.getNEvals() < evaluationLimit) && (generationNumber < generationLimit))
      {
    	  Individual bestIndividual;    	  
    	  
    	  // Insert the best individual in the population into the new population (elitism)    	  
		 if (multiobjective)
			bestIndividual = population.getIndividual(best);
		 else if(Target.maximize)
			bestIndividual = population.getIndividual(((Integer) statistic.getStat(SimpleStats.MAX_FIT_POS)).intValue());
		 else
			bestIndividual = population.getIndividual(((Integer) statistic.getStat(SimpleStats.MIN_FIT_POS)).intValue());
 
      	 // evaluate the best individual again because the other parts of the solution have changed in coevolution
		problem.evaluate(bestIndividual);
		auxPop.setIndividual(0, bestIndividual);
      			
         for (int k=1; k<population.getPopSize(); k++)
         {
        	 // BREEDING LOOP:
        	 
            // First parent selection
			oper = (Operator)operators.get("selection1");
			ind[0] = (Integer)oper.execute(population.getIndividuals());
			
			iv[0] = (Individual)population.getIndividual(ind[0].intValue()).clone();

			// Second parent selection
			oper = (Operator)operators.get("selection2");
			if (oper != null)
		    {
				ind[1] = (Integer)oper.execute(population.getIndividuals());
				while (ind[0].intValue() == ind[1].intValue())
				{
				    // It is not allowed the same parent to be selected twice
				    ind[1] = (Integer)oper.execute(population.getIndividuals());
				}
				iv[1] = (Individual)population.getIndividual(ind[1].intValue()).clone();
		    }
            
            // Recombination
            oper = (Operator)operators.get("crossover");
            if (oper != null)
               if (r.nextDouble() < crossoverProb)
                  iv[0] = (Individual)oper.execute(iv);
            
            // Mutation
            oper = (Operator)operators.get("mutation");
            if (oper != null)
               // the mutation operator is executed on all sons
               if (r.nextDouble() < mutationProb)
                  iv[0] = (Individual)oper.execute(iv[0]);

            // Local Search and evaluation of the new solution
            oper = (Operator)operators.get("local");
            if (oper != null)
               if (r.nextDouble() < localSearchProb)
                  iv[0] = (Individual)oper.execute(iv[0]);
               else problem.evaluate(iv[0]);
            else problem.evaluate(iv[0]);
            

            // Replacement: (mu, mu)-GA with elitism
            auxPop.setIndividual(k,iv[0]);

            // if we are in the multiobjective case, insert the new solution into the archive
            if (multiobjective) paretoFront.Insert((Individual)iv[0].clone());
            
         }
         
         // Replace the current population by the new one
         population.copyPop(auxPop);
         
         if (!multiobjective)
         {
         	statistic.calculate(population);
         	if (Target.maximize)
 	         	optimum = ((Double)statistic.getStat(SimpleStats.MAX_FIT_VALUE)).doubleValue();
 	         else optimum = ((Double)statistic.getStat(SimpleStats.MIN_FIT_VALUE)).doubleValue();
 	         if (Target.isBetterOrEqual(optimum, targetFitness))
 	            return; // stop if the best solution is found
         }
         else
         {
     	      // Get the position of the best and worst individuals in the population
     	      for (int i=1; i<population.getPopSize(); i++)
     	      {
     	    	if (Target.isWorse(population.getIndividual(i), population.getIndividual(worst)))
     	   			  worst = i;
     	    	if (Target.isBetter(population.getIndividual(i), population.getIndividual(best)))
     	   			  best = i;
     	      }
         }
         generationNumber++;
         // listener is a class for monitoring the search, if needed
         listener.generation(this);
      }
   }
}