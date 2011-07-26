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


import operators.mutation.*;
import operators.recombination.*;
import operators.selection.*;
import operators.replacement.*;


public class RouteGeneLaunch  implements GenerationListener
{
    
    static int longitCrom      ;
    static int numberOfFuncts  ;
    
    // Default maximum number of function evaluations
    static int evaluationsLimit = 10000;
    
    //private static boolean showDisplay = false;
   
    private boolean verbose = true;
    
    private Vector<Double> BestIndPerGen = null;
     
    private static String crossover;
    
    private static String mutation;
    
 
    public RouteGeneLaunch(){
    	BestIndPerGen = new Vector<Double>();
    }
    
    public Vector<Double> getBestIndPerGen(){
    	return BestIndPerGen;
    }
    
    
    public static void main (String args[]) throws Exception
    {
    	//int numberofruns = Integer.parseInt(args[0]);
    	int numberofruns = 1;
    	Vector<Vector<Double>> results = new Vector<Vector<Double>>();
    	
    	long start, end;
    	start = (new Date()).getTime();
    	
    	Individual bestIndiv = null;
    	
    	RouteGeneLaunch rgl = null;
		
		EvolutionaryAlg ea = null;
		
		double[] averages = new double[numberofruns];    	
		
    	//Population size
    	int x = 10;
		int y = 10;
		
    	
    	for(int i = 0; i<numberofruns; i++){
    		
        	System.out.println("Running "+i+"...");
        	
        	Random r = new Random(); // seed for the random number generator
        	
    		rgl = new RouteGeneLaunch();
    		
    		ea = new CellularGA(r);
        	
    		Problem prob = new RouteGenerationProblem();
    		ea.setParam(CellularGA.PARAM_PROBLEM, prob);
    		longitCrom = prob.numberOfVariables();
    		numberOfFuncts = prob.numberOfObjectives();    		
    		//evaluationsLimit = Integer.parseInt(args[1]);
    		
    		// Create the population
    		Population pop = new PopGrid(x,y);
    		Individual ind = new RealIndividual(longitCrom);
    		
    		ind.setMinMaxAlleleValue(true, prob.getMinAllowedValues());
    		ind.setMinMaxAlleleValue(false, prob.getMaxAllowedValues());
    		ind.setLength(prob.numberOfVariables());
    		ind.setNumberOfFuncts(prob.numberOfObjectives());
    		ind.setRandomValues(r);
    		
    		pop.setRandomPop(r, ind);
    		
  
    		Double cross = 1.0; // crossover probability
			Double mutac = 1.0; // probability of individual mutation
			Double alleleMutationProb = 1.0 /prob.numberOfVariables(); // allele mutation probability;

    		//Double cross = Double.parseDouble(args[2]);
    		//Double mutac = Double.parseDouble(args[3]);
    		
    		
    		// Set parameters of CGA
    	    ea.setParam(CellularGA.PARAM_POPULATION, pop);
    	    ea.setParam(CellularGA.PARAM_STATISTIC, new ComplexStats());
    	    ea.setParam(CellularGA.PARAM_LISTENER,rgl);
    	    ea.setParam(CellularGA.PARAM_MUTATION_PROB, mutac);
    	    ea.setParam(CellularGA.PARAM_ALLELE_MUTATION_PROB, alleleMutationProb);
    	    ea.setParam(CellularGA.PARAM_CROSSOVER_PROB, cross);
    	    ea.setParam(CellularGA.PARAM_EVALUATION_LIMIT, new Integer(evaluationsLimit));
    	    ea.setParam(CellularGA.PARAM_TARGET_FITNESS, (Double) new Double(prob.getMaxFitness())); 
    	    ea.setParam(CellularGA.PARAM_NEIGHBOURHOOD, new Linear5()); 
    	    
    		ea.setParam("selection1", new TournamentSelection(r)); // selection of first parent
    	    ea.setParam("selection2", new TournamentSelection(r)); // selection of second parent
    	    ea.setParam("crossover", new Spx(r));
    	    crossover = "Single Point Crossover";
    	    ea.setParam("mutation", new FloatUniformMutation(r,ea)); 
    	    mutation = "Float Uniform Mutation";
    	    ea.setParam("replacement", new ReplaceIfBetter()); 
    	   
    	    
    		// generation cycles 
    		ea.experiment();
    		

    		
    		// Get the best Individual
    		int pos = ((Integer)((Statistic)ea.getParam(EvolutionaryAlg.PARAM_STATISTIC)).getStat(SimpleStats.MIN_FIT_POS)).intValue();
    		Individual bestInd = ((Population) ea.getParam(EvolutionaryAlg.PARAM_POPULATION)).getIndividual(pos);
    		
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
    		results.add(rgl.getBestIndPerGen());
    		
    		
			// writes: best found solution, number of evaluations, elapsed time (mseconds) in the standard output

			//System.out.print(bestInd.getFitness() + " " );
			
			//for (int m = 0; m< bestInd.getLength();m++){
			//	System.out.print(bestInd.getAllele(m) + " ");
			//}
			//System.out.print(prob.getNEvals() + " " + "\n");
			
    	}
    	
    	end = (new Date()).getTime();
    	
    	
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
    	out.write("# \tPARAM_POP_ADAPTATION: " +ea.getParam(CellularGA.PARAM_POP_ADAPTATION)+"\n");
    	out.write("# \tPARAM_NEIGHBOURHOOD: " +ea.getParam(CellularGA.PARAM_NEIGHBOURHOOD)+"\n");
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
		
		
		for (int i = 0; i< bestIndiv.getLength();i++){
			out.write("# "+ bestIndiv.getAllele(i) +"\n");
		}
		
		
		out.write("#\n#\n# Best Indiviudal average per generation: Generation BestIndividual\n");
		
		
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
		}
		
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
    
    
    public void generation(EvolutionaryAlg ea)
    {   
    	//CellularGA cea = (CellularGA) ea;
    	verbose = ((Boolean) ea.getParam(CellularGA.PARAM_VERBOSE)).booleanValue();

    	if ((!ea.getParam(EvolutionaryAlg.PARAM_POPULATION).getClass().getName().equalsIgnoreCase("distributedGA")) &&
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
