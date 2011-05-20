/**
 * ssGA.java
 * @author Antonio J. Nebro
 * @version 1.0
 */
package lu.uni.routegeneration.generation;

import jmetal.base.*;
import jmetal.base.operator.comparator.* ;
import jmetal.base.operator.selection.BestSolutionSelection;
import jmetal.base.operator.selection.WorstSolutionSelection;
import jmetal.base.variable.Permutation;
import jmetal.base.Algorithm;
import java.util.Comparator;
import jmetal.util.*;

/** 
 * Class implementing a steady state genetic algorithm
 */
public class ssGA extends Algorithm {
  private Problem           problem_;        
  
 /**
  *
  * Constructor
  * Create a new SSGA instance.
  * @param problem Problem to solve
  *
  */
  public ssGA(Problem problem){
    this.problem_ = problem;                        
  } // SSGA

  public double[] FitnessHistory;
 /**
  * Execute the SSGA algorithm
 * @throws JMException 
  */
  public double[] get_fitness_history(){
        int populationSize = ((Integer)this.getInputParameter("populationSize")).intValue();
        int maxEvaluations = ((Integer)this.getInputParameter("maxEvaluations")).intValue();
        double[] r = new double[maxEvaluations - populationSize];
        for(int i=0;i<(maxEvaluations - populationSize);i++){
            r[i] = FitnessHistory[i];
        }
        return(r);
  }


  public SolutionSet execute() throws JMException, ClassNotFoundException {
    int populationSize ;
    int maxEvaluations ;
    int evaluations    ;

    SolutionSet population        ;
    Operator    mutationOperator  ;
    Operator    crossoverOperator ;
    Operator    selectionOperator ;
    
    Comparator  comparator        ;
    
    comparator = new ObjectiveComparator(0) ; // Single objective comparator
    
    Operator findWorstSolution ;
    findWorstSolution = new WorstSolutionSelection(comparator) ;


    // Read the parameters
    populationSize = ((Integer)this.getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer)this.getInputParameter("maxEvaluations")).intValue();                

    FitnessHistory = new double[maxEvaluations - populationSize];

    // Initialize the variables
    population   = new SolutionSet(populationSize);        
    evaluations  = 0;                

    // Read the operators
    mutationOperator  = this.operators_.get("mutation");
    crossoverOperator = this.operators_.get("crossover");
    selectionOperator = this.operators_.get("selection");  

    // Create the initial population
    Solution newIndividual;
    int bestindex = 0;
    double bestFitness = 100000;
    for (int i = 0; i < populationSize; i++) {
      newIndividual = new Solution(problem_);                    
      problem_.evaluate(newIndividual);            
      evaluations++;
      population.add(newIndividual);
      if (newIndividual.getObjective(0)<bestFitness){
          bestFitness = newIndividual.getObjective(0);
          RouteOpimization.logger_.info("evaluation:"+evaluations+"  best fitness:" + bestFitness);
      }
    } //for       

    FitnessHistory[evaluations - populationSize] = bestFitness;

    // main loop
    while (evaluations < maxEvaluations) {

      Solution [] parents = new Solution[2];

        // Selection
      parents[0] = (Solution)selectionOperator.execute(population);
      parents[1] = (Solution)selectionOperator.execute(population);
 
      // Crossover
      Solution [] offspring = (Solution []) crossoverOperator.execute(parents);  

      // Mutation
      mutationOperator.execute(offspring[0]);

      // Evaluation of the new individual
      problem_.evaluate(offspring[0]);            
          
      evaluations ++;
      System.out.println("evaluation: "+evaluations);
      
      if (offspring[0].getObjective(0)<bestFitness){
          bestFitness = offspring[0].getObjective(0);
          RouteOpimization.logger_.info("evaluation:"+evaluations+"  best fitness:" + bestFitness);
      }


      // Replacement: replace the last individual is the new one is better
      int worstIndividual = (Integer)findWorstSolution.execute(population) ;

      if (comparator.compare(population.get(worstIndividual), offspring[0]) > 0) {
        population.remove(worstIndividual) ;
        population.add(offspring[0]);
      } // if
    } // while
    
    // Return a population with the best individual
    population.sort(comparator) ;

    SolutionSet resultPopulation = new SolutionSet(1) ;
    resultPopulation.add(population.get(0)) ;

    return resultPopulation ;
  } // execute


} // SSGA
