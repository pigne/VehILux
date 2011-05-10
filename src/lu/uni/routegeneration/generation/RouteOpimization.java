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

import jmetal.qualityIndicator.QualityIndicator;

public class RouteOpimization {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object

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

    problem = new RouteGeneration();
    algorithm = new ssGA(problem);
    algorithm.setInputParameter("populationSize",10);
    algorithm.setInputParameter("maxEvaluations",100);

    crossover = new routeGenCrossoverV2();
    mutation = new routeGenMutation();
    selection = SelectionFactory.getSelectionOperator("BinaryTournament2") ;

    // Add the operators to the algorithm
    algorithm.addOperator("crossover",crossover);
    algorithm.addOperator("mutation",mutation);
    algorithm.addOperator("selection",selection);

    // Execute the Algorithm
    long initTime = System.currentTimeMillis();
    SolutionSet population = algorithm.execute();

    Solution bestSolution = population.get(0);

    System.out.println("calculating best result again");
    problem.evaluate(bestSolution);
    ((RouteGeneration)problem).evalData.writeResultsToFile();

    long estimatedTime = System.currentTimeMillis() - initTime;
    // Result messages
    logger_.info("Total execution time: "+estimatedTime + "ms");
    logger_.info("Variables values have been writen to file VAR");
    population.printVariablesToFile("VAR");
    logger_.info("Objectives values have been writen to file FUN");
    population.printObjectivesToFile("FUN");

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

