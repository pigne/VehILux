/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.uni.routegeneration.generation;
import jmetal.base.*;

/**
 *
 * @author Masoud
 */
//public class RouteOpimization {
//
//}

import jmetal.base.*;
import jmetal.base.operator.selection.*   ;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import java.io.IOException;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import jmetal.base.variable.*;
import java.util.*;

import jmetal.qualityIndicator.QualityIndicator;

public class RouteOpimization {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object
  public static final int population_number = 10;//should be an even number
  public static final int generation_number = 14;
  public static final int running_number = 10;

  //evaluation number for gGA
  public static final int evaluation_number = population_number + generation_number*(population_number - 2);

  public static void main(String [] args) throws
                                  JMException,
                                  SecurityException,
                                  IOException,
                                  ClassNotFoundException {
    Problem   problem   ;         // The problem to solve
    Algorithm algorithm ;         // The algorithm to use
    Operator  crossover ;         // Crossover operator
    Operator  mutation  ;         // Mutation operator
    Operator  selection ;         // Selection operator

    QualityIndicator indicators ; // Object to get quality indicators

      // Logger object and file to store log messages
    logger_      = Configuration.logger_ ;
    fileHandler_ = new FileHandler("NSGAII_main.log");
    logger_.addHandler(fileHandler_) ;

    ArrayList<double[]> fitnessSets = new ArrayList<double[]>();

    problem = new RouteGeneration();
    //algorithm = new ssGA(problem);
    algorithm = new gGA(problem);
    ((gGA)algorithm).generationNumber = generation_number;

    algorithm.setInputParameter("populationSize",population_number);
    algorithm.setInputParameter("maxEvaluations",evaluation_number);

    crossover = new routeGenCrossoverV2();
    mutation = new routeGenMutation();
//    selection = new RandomSelection() ;
    selection = new BinaryTournament();

    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    // Execute the Algorithm and gathering the results
    long initTime = System.currentTimeMillis();
    Solution bestSolution = null;
    double bestfitness = 100000;
    for (int i=0; i<running_number; i++) {
       System.out.println("***** ***** ***** ***** running number: "+i);
        SolutionSet population = algorithm.execute();
        if(i==0) bestfitness = population.get(0).getObjective(0);
        if (population.get(0).getObjective(0)<=bestfitness){
            bestSolution = new Solution(population.get(0));
            bestfitness = bestSolution.getObjective(0); 
        }
        double[] newfitnessHistory = ((gGA)algorithm).get_fitness_history();
        fitnessSets.add(newfitnessHistory);
    }

    double[] FitnessProgress = new double[generation_number];
    for(int i = 0;i< (generation_number);i++){
        double sum = 0;
        for(int j=0;j<running_number; j++){
            sum+=(fitnessSets.get(j))[i];
        }
        FitnessProgress[i] = sum/running_number;
    }

    System.out.println("calculating best result again");
    problem.evaluate(bestSolution);
    ((RouteGeneration)problem).evalData.writeResultsToFile();

    System.out.println("FitnessProgress");
    for(int i = 0;i< (generation_number);i++){
        System.out.println( i + " \t "+ FitnessProgress[i]);
    }


    long estimatedTime = System.currentTimeMillis() - initTime;
    // Result messages
/*    logger_.info("Total execution time: "+estimatedTime + "ms");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");*/

  } //main

  //this function was used just for testing outputs of crossover and mutation functions
  public void testSolutionType() throws JMException {
        Problem   problem   ;         // The problem to solve
        problem = new RouteGeneration();
        Solution[] s = new Solution[2];
        try {
            s[0] = new Solution(problem);
            s[1] = new Solution(problem);
        }catch(ClassNotFoundException e){
        }

        Variable[] var = s[0].getDecisionVariables();
        Variable[] var2 = s[1].getDecisionVariables();

        for(int i =0;i<((ArrayInt)var[0]).getLength();i++){
            System.out.print(((ArrayInt)var[0]).getValue(i)+" ");
        }
        System.out.println(" *");

        for(int i =0;i<((ArrayInt)var[1]).getLength();i++){
            System.out.print(((ArrayInt)var[1]).getValue(i)+" ");
        }
        System.out.println(" *");

        for(int i =0;i<((ArrayInt)var2[0]).getLength();i++){
            System.out.print(((ArrayInt)var2[0]).getValue(i)+" ");
        }
        System.out.println(" *");

        for(int i =0;i<((ArrayInt)var2[1]).getLength();i++){
            System.out.print(((ArrayInt)var2[1]).getValue(i)+" ");
        }
        System.out.println(" *");

        routeGenCrossoverV2 co = new routeGenCrossoverV2();
        //routeGenMutation mu = new routeGenMutation();
        Object oo = co.execute(s);
        Solution[] ch = (Solution[])oo;
        Variable[] var3 = ch[0].getDecisionVariables();

        for(int i =0;i<((ArrayInt)var3[0]).getLength();i++){
            System.out.print(((ArrayInt)var3[0]).getValue(i)+" ");
        }
        System.out.println(" *");

        for(int i =0;i<((ArrayInt)var3[1]).getLength();i++){
            System.out.print(((ArrayInt)var3[1]).getValue(i)+" ");
        }
        System.out.println(" *");
    }

} 

