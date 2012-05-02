package lu.uni.routegeneration.jCell;

import jcell.*;
import jcell.neighborhoods.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Random;
import java.util.Date;
import java.util.Vector;

import lu.uni.routegeneration.generation.RouteGeneration;


import operators.mutation.*;
import operators.recombination.*;
import operators.selection.*;
import operators.replacement.*;
import ssEA.*;
import genEA.*;


public class RouteGeneLaunch  implements GenerationListener
{
    
    static int longitCrom      ;
    static int numberOfFuncts  ;
    
    // Default maximum number of function evaluations
    //static int evaluationsLimit = 10000;
    static int evaluationsLimit = 5000;
    
    //private static boolean showDisplay = false;
   
    private boolean verbose = true;
    
    private Vector<Double> BestIndPerGen = null;
    private Vector[] bestIslandIndPerGen = null;
    private Vector<String> bestDetectorPerGen = null;

    private static String selection1;

    private static String selection2;

    private static String replacement;
    private static String crossover;
    
    private static String mutation;
    
    private static EvolutionaryAlg ea = null;
    
    private static boolean coevolutionary = false;        
    private static boolean synchronised = true;
    private static boolean coevElitism = true;    
    private static int islandcount = 4;
    private static boolean parallelProblemInit = false;
    
    public RouteGeneLaunch(){
    	BestIndPerGen = new Vector<Double>();
    	bestIslandIndPerGen = new Vector[islandcount];
        for(int island = 0; island< islandcount; island++)
    	{
        	bestIslandIndPerGen[island] = new Vector<Double>();        	
    	}
        bestDetectorPerGen = new Vector<String>();
    }
    
    public Vector<Double> getBestIndPerGen(){
    	return BestIndPerGen;
    }
    
    public Vector<Double>[] getBestIslandIndPerGen(){
    	return bestIslandIndPerGen;
    }

    private static Population CreatePopulation(String algorithm, Problem prob, Random r, int x, int y)
    {
    	Population pop;
    	if(algorithm.equalsIgnoreCase("cGA"))
		{
    		// Create the population
    		pop = new PopGrid(x,y);	
		}
		else
		{
			pop = new Population(x * y);
		}
    	
    	//Individual ind = new VehILuxIndividual();
    	//VehILuxRealIndividual ind = new VehILuxRealIndividual();
    	// need to initialize each individual to normalise
    	for (int i=0; i<pop.getPopSize(); i++)
        {	
			RealIndividual ind = new RealIndividual();
			
			ind.setMinMaxAlleleValue(true, prob.getMinAllowedValues());
			ind.setMinMaxAlleleValue(false, prob.getMaxAllowedValues());
			ind.setLength(prob.numberOfVariables());
			ind.setNumberOfFuncts(prob.numberOfObjectives());
			ind.setRandomValues(r);
			
			RouteGenerationProblem.NormaliseIndividual(ind);
			
			if (RouteGenerationProblem.discrete)
			{
				RouteGenerationProblem.DiscretiseIndividual(ind);
			}
			
			pop.setIndividual(i, ind);
        }
        //pop.setRandomPop(r, ind);
        
        return pop;
    }
    
    private static Problem CreateProblem()
    {
    	Problem problem = new RouteGenerationProblem();    	
    	//Problem problem = new RouteGenerationProblemTest();
    	
    	return problem;
    }
    static int index;
    
    public static void main (String args[]) throws Exception
    {
    	String algorithm = args[0];
    	
    	int numberofruns = 1;
    	Vector<Vector<Double>> results = new Vector<Vector<Double>>();
    	
    	long start, end;
    	start = (new Date()).getTime();
    	
    	Individual bestIndiv = null;
    	
    	RouteGeneLaunch rgl = null;
				
		double[] averages = new double[numberofruns];    	
		
		//Population size
		int x = 10;
		int y = 10;		
    	
    	for(int i = 0; i<numberofruns; i++){
    		
        	System.out.println("Running "+i+"...");
        	
        	Random r = new Random(); // seed for the random number generator
        	
    		rgl = new RouteGeneLaunch();
            if (coevolutionary)
            {
            	//int[] islandMask = {0,0,0,1,1,2,2,2,3,3,1,3};
            	int[] islandMask = {0, 0, 0, 1, 1, 1, 1, 2, 2, 3, 3, 2, 3}; 
            	//int[] islandMask = {0, 1, 2, 2, 2, 2, 2, 1, 1, 0, 0, 3, 3};
            	
            	ea = new CoevEA(r, islandcount, islandMask, synchronised, coevElitism);
            	for(int island = 0; island< islandcount; island++)
            	{
	            	if(algorithm.equalsIgnoreCase("cGA")){
	            		((CoevEA)ea).setParam(island, CoevEA.PARAM_ALG, new CellularGA(r));
	            	} else if (algorithm.equalsIgnoreCase("genGA")){           
	            		((CoevEA)ea).setParam(island, CoevEA.PARAM_ALG, new GenGA(r));		            	
		            } else if(algorithm.equalsIgnoreCase("ssGA")){  
		            	((CoevEA)ea).setParam(island, CoevEA.PARAM_ALG, new SSGA(r));
		            }
            	}
            }
            else
            {
	            if(algorithm.equalsIgnoreCase("cGA")){
	            	ea = new CellularGA(r);
	            	System.out.println("cGA");
	            } else if (algorithm.equalsIgnoreCase("genGA")){           
	            	ea = new GenGA(r);
	            	System.out.println("genGA");
	            } else if(algorithm.equalsIgnoreCase("ssGA")){  
	            	ea = new SSGA(r);
	            	System.out.println("ssGA");
	            }
            }        
  
    		Double cross = 1.0; // crossover probability
			Double mutac = 1.0; // probability of individual mutation
			Double alleleMutationProb; // = 1.0 /prob.numberOfVariables(); // allele mutation probability;

    		//Double cross = Double.parseDouble(args[2]);
    		//Double mutac = Double.parseDouble(args[3]);
            if (coevolutionary)
            {
            	//alleleMutationProb = alleleMutationProb * islandcount;
            	((CoevEA)ea).setParam(EvolutionaryAlg.PARAM_LISTENER,rgl);
            	
            	if (parallelProblemInit)
            	{
	            	Thread[] t = new Thread[islandcount];            	
	            	for (int island = 0; island < islandcount; island++)
	            	{
	            		index = island;
	            		t[island] = new Thread( new Runnable()
	    					{
		    	                int island=new Integer(index);
		    	
		    	                public void run ()
		    	                {                    
		    	                	Problem prob = CreateProblem();
		    	            		((CoevEA)ea).setParam(island, EvolutionaryAlg.PARAM_PROBLEM, prob);
		    	                } 
	    	            	}
	            		);
	            	}
	            	
	            	// start all island threads initialising problems in parallel
	            	for (int island = 0; island < islandcount; island++)
	        		{            		
	        			t[island].start();
	        			Thread.sleep(2000);
	        		}
	        		
	        		// wait for all to finish
	            	for (int island = 0; island < islandcount; island++)
	        		{
	        			try {
	        				t[island].join();
	        			
	        			} catch (InterruptedException e) {
	        				// TODO Auto-generated catch block
	        				e.printStackTrace();
	        			}
	        		}
            	}
            	
        		for (int island = 0; island < islandcount; island++)
            	{	
        			Problem prob = null;
        			
        			if (parallelProblemInit)
                	{
        				prob = (Problem)((CoevEA)ea).getParam(island, EvolutionaryAlg.PARAM_PROBLEM);
                	}
        			else
        			{
        				System.out.println("Started problem " + island);
        				prob = CreateProblem();
	            		((CoevEA)ea).setParam(island, EvolutionaryAlg.PARAM_PROBLEM, prob);
        			}
        			alleleMutationProb = 1.0 / prob.numberOfVariables(); // allele mutation probability;
        			
	            	Population pop = CreatePopulation(algorithm, prob, r, x / (islandcount / 2), y / (islandcount / 2));
	            	// common parameter objects
	            	((CoevEA)ea).setParam(island, EvolutionaryAlg.PARAM_POPULATION, pop);
	            	((CoevEA)ea).setParam(island, EvolutionaryAlg.PARAM_STATISTIC, new ComplexStats());
	            	ea.setParam(EvolutionaryAlg.PARAM_TARGET_FITNESS, (Double) new Double(prob.getMaxFitness()));
	            	ea.setParam(EvolutionaryAlg.PARAM_TARGET_FITNESS, -1.0); // to avoid islands quitting
	        	    
	        	    // specific parameter objects
		            if(algorithm.equalsIgnoreCase("cGA")){
		            	// Set parameters of CGA	        	    
		            	((CoevEA)ea).setParam(island, CellularGA.PARAM_NEIGHBOURHOOD, new Compact9()); 	//Neighborhood
		            	((CoevEA)ea).setParam(island, CellularGA.PARAM_CELL_UPDATE, new LineSweep((PopGrid)pop));
		            	((CoevEA)ea).setParam(island, "replacement", new ReplaceIfNonWorse()); //Replacement Strategy
		        	    replacement = "If Non Worse";
		        	    
		            } else if (algorithm.equalsIgnoreCase("genGA")){
		            	// no special parameters needed	        	    
		            } else if(algorithm.equalsIgnoreCase("ssGA")){
		            	((CoevEA)ea).setParam(island, "replacement", new ReplaceIfBetter()); //Replacement Strategy
		        	    replacement = "If better";
		            }
		            ((CoevEA)ea).setParam(island, EvolutionaryAlg.PARAM_ALLELE_MUTATION_PROB, alleleMutationProb);
            	}
            	ea.setParam(EvolutionaryAlg.PARAM_EVALUATION_LIMIT, new Integer(evaluationsLimit / islandcount));            	
            }
            else
            {
            	Problem prob = CreateProblem();
            	alleleMutationProb = 1.0 / prob.numberOfVariables(); // allele mutation probability;
                ea.setParam(EvolutionaryAlg.PARAM_PROBLEM, prob);
                
                Population pop = CreatePopulation(algorithm, prob, r, x, y);
            	
            	// common parameter objects
            	ea.setParam(EvolutionaryAlg.PARAM_POPULATION, pop);
        	    ea.setParam(EvolutionaryAlg.PARAM_STATISTIC, new ComplexStats());
        	    ea.setParam(EvolutionaryAlg.PARAM_LISTENER,rgl);
        	    ea.setParam(EvolutionaryAlg.PARAM_TARGET_FITNESS, (Double) new Double(prob.getMaxFitness()));
        	    
        	    // specific parameter objects
	            if(algorithm.equalsIgnoreCase("cGA")){
	            	// Set parameters of CGA	        	    
	        	    ea.setParam(CellularGA.PARAM_NEIGHBOURHOOD, new Compact9()); 	//Neighborhood
	        	    ea.setParam(CellularGA.PARAM_CELL_UPDATE, new LineSweep((PopGrid)pop));
	        	    ea.setParam("replacement", new ReplaceIfNonWorse()); //Replacement Strategy
	        	    replacement = "If Non Worse";
	        	    
	            } else if (algorithm.equalsIgnoreCase("genGA")){
	            	// no special parameters needed	        	    
	            } else if(algorithm.equalsIgnoreCase("ssGA")){
	        	    ea.setParam("replacement", new ReplaceIfBetter()); //Replacement Strategy
	        	    replacement = "If better";
	            }
	            ea.setParam(EvolutionaryAlg.PARAM_EVALUATION_LIMIT, new Integer(evaluationsLimit));
	            ea.setParam(EvolutionaryAlg.PARAM_ALLELE_MUTATION_PROB, alleleMutationProb);
            }
            
            // common parameter values
            ea.setParam(EvolutionaryAlg.PARAM_MUTATION_PROB, mutac);    	    
    	    ea.setParam(EvolutionaryAlg.PARAM_CROSSOVER_PROB, cross);
            
            ea.setParam("selection1", new TournamentSelection(r)); // selection of first parent
            //ea.setParam("selection1", new CenterSelection(r)); // selection of first parent
            selection1="Center Selection";
    	    ea.setParam("selection2", new TournamentSelection(r)); // selection of second parent
    	    selection2="Tournament Selection";
    	    
//    	    //ea.setParam("crossover", new VehILuxNormOperator(new PBX(r))); //Crossover Operator
//    	    //ea.setParam("crossover", new VehILuxNormOperator(new Spx(r), discrete)); //Crossover Operator
//    	    ea.setParam("crossover", new VehILuxNormOperator(new Dpx(r), discrete)); //Crossover Operator
//	    	//ea.setParam("mutation", new VehILuxNormOperator(new GaussianMutation(r, ea), discrete)); //Mutation Operator
//    	    ea.setParam("mutation", new VehILuxNormOperator(new FloatUniformMutation(r, ea), discrete)); //Mutation Operator
    	    
    	    ea.setParam("crossover", new Spx(r)); //Crossover Operator	    	//
    	    ea.setParam("mutation", new FloatUniformMutation(r, ea)); //Mutation Operator
    	    //ea.setParam("mutation", new GaussianMutation(r, ea)); //Mutation Operator
    	    
            // generation cycles
            ea.experiment();
    		
    		// Get the best Individual
    		int pos = ((Integer)((Statistic)ea.getParam(EvolutionaryAlg.PARAM_STATISTIC)).getStat(SimpleStats.MIN_FIT_POS)).intValue();
    		Individual bestInd = ((Population) ea.getParam(EvolutionaryAlg.PARAM_POPULATION)).getIndividual(pos);
    		
    		if (RouteGenerationProblem.bestIndividual != null)
    		{
    			bestIndiv = RouteGenerationProblem.bestIndividual;
    		}    	
    		
    		if (bestIndiv == null){
    			bestIndiv = bestInd;
    		}else{
    			if ((Double) bestIndiv.getFitness() > (Double) bestInd.getFitness()){
    				bestIndiv = (Individual) bestInd.clone();
    			}
    		}
    		    		    		
    		double avg = ((Double)((Statistic)ea.getParam(EvolutionaryAlg.PARAM_STATISTIC)).getStat(ComplexStats.AVG_FIT)).doubleValue();
    		    		
    		//Save average of this run
    		averages[i] = avg;
    		     		
            //Save the best individuals per generation of this run
            if (coevolutionary)
            {
            	if (synchronised)
            	{
            		results.add(rgl.getBestIndPerGen());
            	}
            	else
            	{
	            	Vector<Double>[] best = rgl.getBestIslandIndPerGen();
	            	
	            	for (int island = 0; island < islandcount; island++)
	            	{
	            		results.add(best[i]);
	            	}
            	}
            }
            else
            {
            	results.add(rgl.getBestIndPerGen());
            }
    	}
    	
    	end = (new Date()).getTime();
    	
    	crossover = ea.getParam("crossover").toString();
	    mutation = ea.getParam("mutation").toString();
    	
    	Double best = (Double) bestIndiv.getFitness();
    	
    	//Saving to file
		String filename = "RouteGenProblem_" + x + "x" +y + "Neigh" + CellularGA.PARAM_NEIGHBOURHOOD + "Mp" + ea.getParam(CellularGA.PARAM_MUTATION_PROB) + "Cp" + ea.getParam(CellularGA.PARAM_CROSSOVER_PROB) +"_" + evaluationsLimit + ".dat" ;
		
		File save_file = new File(filename);
		
		int ID_File=1;
		while (save_file.exists()){
			filename = "RouteGenProblem_" + x + "x" +y + "Neigh" + CellularGA.PARAM_NEIGHBOURHOOD + "Mp" + ea.getParam(CellularGA.PARAM_MUTATION_PROB) + "Cp" + ea.getParam(CellularGA.PARAM_CROSSOVER_PROB) +"_" + evaluationsLimit + "-"+(++ID_File)+".dat" ;
			save_file = new File(filename);
		}
    	
		
    	BufferedWriter out = new BufferedWriter(new FileWriter(save_file));
    	
    	out.write("# Mobility Model optimization using jCell\n");
    	out.write("#\n");
    	Calendar cal = Calendar.getInstance();
    	SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
    	out.write("# Cellular Genetic Algorithm  Time&Date: "+ date_format.format(cal.getTime()) + "\n");
    	out.write("# Parameters: \n");
    	out.write("# \tPopulation: "+x+"x"+y+"\n");
    	out.write("# \tEvaluationsLimit: " + evaluationsLimit +"\n");    	
    	out.write("# \tPARAM_POP_ADAPTATION: " +ea.getParam(CellularGA.PARAM_POP_ADAPTATION)+"\n");
    	out.write("# \tPARAM_NEIGHBOURHOOD: " +ea.getParam(CellularGA.PARAM_NEIGHBOURHOOD)+"\n");
    	
    	if (coevolutionary)
    	{
	    	out.write("# \tCo-evolution: " + (synchronised?"synchronised":"asynchronised") + ", " + (coevElitism?"elitism":"no elitism") + ", " + islandcount + " islands\n");	    	
    	}
    	else
    	{
    		out.write("# \tCo-evolution: none \n");
    	}
    	out.write("# \tDiscretised values: " + RouteGenerationProblem.discrete +"\n");   
    	        
    	out.write("# Crossover Operator:" + crossover + "\n");
    	out.write("# \tCROSSOVER_PROB: "+ea.getParam(CellularGA.PARAM_CROSSOVER_PROB)+"\n");
    	out.write("# Mutation Operator:" + mutation + "\n");
    	out.write("# \tPARAM_MUTATION_PROB: "+ea.getParam(CellularGA.PARAM_MUTATION_PROB)+"\n");	
    	out.write("# \tPARAM_SYNCHR_UPDATE: "+ea.getParam(CellularGA.PARAM_SYNCHR_UPDATE)+"\n");
    	out.write("# \tPARAM_CELL_UPDATE: "+ea.getParam(CellularGA.PARAM_CELL_UPDATE)+"\n");
    	//out.write("# \tCrossover: WHX C13 Mutation:Uniform Sel1:TS Sel2:TS\n");
    	
    	double mean = rgl.getMean(averages);
    	out.write("#\n#\n# Average: "+ mean +"\n");
    	out.write("#\n# Standard deviation: "+rgl.getStandardDeviation(mean, averages) +" #\n");
    	
    	// Writes: best found solution, number of generations, elapsed time (mseconds)
    	out.write("#\n#\n# Solution: Best Time (ms)\n#\n");
    	out.write("# " + best + " " +(end-start) + "\n#\n");
    	out.write("# Alleles of best individual:\n");
    	System.out.println("# Alleles of best individual:\n");
		
		for (int i = 0; i< bestIndiv.getLength();i++){
			out.write("# "+ bestIndiv.getAllele(i) +"\n");
        	System.out.println("# "+ bestIndiv.getAllele(i));
        }
		
		
		out.write("#\n#\n# Best Indiviudal average per generation: Generation BestIndividual\n");
      	System.out.println("#\n#\n# Best Indiviudal average per generation: Generation BestIndividual\n");
		
		
		// Calculation of average of best individual per generation
		double[] average = null;
		int numberEval = 0;
		Enumeration<Vector<Double>> e = results.elements();
		while (e.hasMoreElements()){
			Vector<Double> n = e.nextElement();
			
			numberEval = n.size();
			
			if (average == null){
				average = new double[numberEval];
			}
			
			int i = 0;
			Enumeration<Double> e2 = n.elements();
			while (e2.hasMoreElements()){
				average[i++] += e2.nextElement();
			}
		}
		
		for (int i = 0; i < numberEval; i++){
			average[i] /= results.size();
			out.write(i + "\t" + average[i] +"\n");
                        System.out.println(i + "\t" + average[i]);
		}
		
		out.write("#\n#\n# Best Indiviudals detectors\n");
      	System.out.println("#\n#\n# Best Indiviudals detectors\n");
      	
      	out.write(RouteGenerationProblem.bestDetectors +"\n");
      	System.out.println(RouteGenerationProblem.bestDetectors);
      	
		out.close();
    }
    
    public double getMean(double[] elements) {
    	double sum = 0.0;
    	for (int i=0; i< elements.length;i++)
    		sum += elements[i];
    	return sum / elements.length;  
    }

    public double getStandardDeviation(double mean, double[] elements) {  
    	double squareSum = 0.0;
    	for (int i=0; i< elements.length;i++)
    		squareSum += elements[i]*elements[i];
    	return Math.sqrt( squareSum/elements.length - mean*mean );
    }
    
    
    public void generation(EvolutionaryAlg alg)
    {
    	//CellularGA cea = (CellularGA) ea;
    	verbose = ((Boolean) ea.getParam(CellularGA.PARAM_VERBOSE)).booleanValue();

    	if (coevolutionary)
    	{	
    		if (synchronised)
    		{
	    		if (alg instanceof CoevEA)
	    		{
	    			Problem p = (Problem)alg.getParam(CoevEA.PARAM_PROBLEM);
	    			Individual individual = (Individual)alg.getParam(CoevEA.PARAM_BEST_IDV);
	    			individual.setFitness(p.eval(individual)); // use eval not to count evaluations!
	    			
	    			BestIndPerGen.add(((Double)individual.getFitness()).doubleValue());
	    			
	    			if (p instanceof RouteGenerationProblem)
	    			{
	    				RouteGenerationProblem rgp = (RouteGenerationProblem)p;
		    			
		    			bestDetectorPerGen.add(rgp.getCurrentDectectors());		    			
	    			}
	    		}
    		}
    		else
    		{
        		int island = ((CoevEA)ea).IslandIndex(alg);
        		
        		int pos = ((Integer)((Statistic)alg.getParam(EvolutionaryAlg.PARAM_STATISTIC)).getStat(SimpleStats.MIN_FIT_POS)).intValue();
    			Individual bestInd = ((Population)alg.getParam(EvolutionaryAlg.PARAM_POPULATION)).getIndividual(pos);
        		
        		bestIslandIndPerGen[island].add(((Double)bestInd.getFitness()).doubleValue());
    		}
    	}
    	else if ((!ea.getParam(EvolutionaryAlg.PARAM_POPULATION).getClass().getName().equalsIgnoreCase("distributedGA")) &&
    			(((Population)ea.getParam(EvolutionaryAlg.PARAM_POPULATION)).getPopSize() != 1))
    	{
			// Get the best Individual
			int pos = ((Integer)((Statistic)ea.getParam(EvolutionaryAlg.PARAM_STATISTIC)).getStat(SimpleStats.MIN_FIT_POS)).intValue();
			Individual bestInd = ((Population) ea.getParam(EvolutionaryAlg.PARAM_POPULATION)).getIndividual(pos);

			BestIndPerGen.add(((Double)bestInd.getFitness()).doubleValue());
			//System.out.println("\t Best individual of generation " + (++NumGen) +" : " + bestInd.getFitness());
    	}
    }

}
