package lu.uni.routegeneration.jCell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.graphstream.graph.Node;

import jcell.Individual;
import jcell.Problem;
import jcell.RealIndividual;
import jcell.Target;
import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.evaluation.RealEvaluation;
import lu.uni.routegeneration.generation.Area;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Trip;
import lu.uni.routegeneration.generation.ZoneType;
import lu.uni.routegeneration.helpers.LoopHandler;
import lu.uni.routegeneration.helpers.XMLParser;

public class RouteGenerationProblem extends Problem {

	public static int[] GeneGroupLengths = {3,4,2,2,1,1}; 
	public static boolean discrete = false;
	public static String bestDetectors = "";
	public static Individual bestIndividual = null;
	private static double bestFitness = 1.7976931348623157E308; //new Double(0).MAX_VALUE;

	private RouteGeneration routeGen;
	private HashMap<String, Detector> currentSolution;
	private HashMap<String, String> detectorIds;
	private RealEvaluation evaluator;
	private int stopHour;
	
	public RouteGenerationProblem(RouteGeneration routeGen, RealEvaluation evaluator) {
		super();
	
		Target.maximize = false;
		variables = 13; 
		maxFitness = 0.0;
		
		//Set the maximum and minimum values for each of the solution variables 
		//Structure  Tr/Ti/Tc/Zc1/Zc2/Zc3/Zcd/Zi1/Zid/Zr1/Zrd/IR/SR
		Double minValues[] = {1.0, 	 1.0, 	1.0,   1.0,	  1.0,	 1.0, 	1.0,   1.0,   1.0,   1.0,   1.0,   30.0, 20.0};
		Double maxValues[] = {100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 70.0, 80.0};

		minAllowedValues = new Vector<Double>(Arrays.asList(minValues));
        maxAllowedValues = new Vector<Double>(Arrays.asList(maxValues));
	    
        this.routeGen = routeGen;
        this.evaluator = evaluator;
		this.stopHour = routeGen.getStopHour();
		
        currentSolution = new HashMap<String, Detector>();
		detectorIds = new HashMap<String, String>();
		for(Detector detector : evaluator.controls.values()){
			Detector det = new Detector(stopHour);
			det.id = detector.id;
			detectorIds.put(detector.edge, detector.id);
			currentSolution.put(det.id, det);
		}
	}
	
	@Override
	public Object eval(Individual ind) {
		
		RouteGenerationProblem.NormaliseIndividual(ind);
		if(RouteGenerationProblem.discrete)
		{
			RouteGenerationProblem.DiscretiseIndividual(ind);
		}
		
		setRouteGenerationParameters(ind);

		double shiftingRatio = (Double)ind.getAllele(12)/100;
		setDetectors(shiftingRatio);
		
		double value = evaluate();
		
		if (value < bestFitness)
		{
			bestFitness = value; 
			bestDetectors = getCurrentDectectors();
			bestIndividual = (Individual)ind.clone();
		}
				
		return value;
	}

	private void setRouteGenerationParameters(Individual ind) {
		// set ZoneType probabilities
		ZoneType.RESIDENTIAL.setProbability((Double)ind.getAllele(0)/100);
		ZoneType.COMMERCIAL.setProbability((Double)ind.getAllele(1)/100);
		ZoneType.INDUSTRIAL.setProbability((Double)ind.getAllele(2)/100);

		// set area probabilities
		
		//Fills in the different Commercial areas probabilities
		// Zc1/Zc2/Zc3/
		ArrayList<Area> areas = routeGen.getAreas();
		// XXX 3 first areas are commercial
		for (int i = 3; i < 6; i++) {
			routeGen.getAreas().get(i - 3).setProbability((Double)ind.getAllele(i)/100);
		}
		
		//Fills in the default Commercial area probability
		//Zcd/
		routeGen.getDefaultCommercialArea().setProbability((Double)ind.getAllele(6)/100);
		
		//Fills in the Industrial area probability
		//Zi1
		routeGen.getAreas().get(3).setProbability((Double)ind.getAllele(7)/100);
		
		//Fills in the default Industrial area probability
		//Zid
		routeGen.getDefaultIndustrialArea().setProbability((Double)ind.getAllele(8)/100);
		
		//Fills in the Residentia area probability
		//Zr1
		routeGen.getAreas().get(4).setProbability((Double)ind.getAllele(9)/100);
		
		//Fills in the default Residential area probability
		//Zrd
		routeGen.getDefaultResidentialArea().setProbability((Double)ind.getAllele(10)/100);
		
		//Fills in the insideFlowRatio and ShiftingRatio
		//IR/SR
		routeGen.setInsideFlowRatio((Double)ind.getAllele(11)/100);
		
		routeGen.computeZonesProbabilities();
	}

	private void setDetectors(double shiftingRatio) {
		for(Detector d : currentSolution.values()){
			d.reset();
			d.setShiftingRatio(shiftingRatio);
		}
	}
	
	private double evaluate() {
		long start = System.currentTimeMillis();
		double fitness = 0;		

		computeCurrentSolution();
		
		//Applies shiftingRatio for each control point
		for(Detector d : currentSolution.values()){
			d.shift();
		}
	
		fitness = evaluator.compareTo(currentSolution);
		
		System.out.printf("%.1f s%n",(System.currentTimeMillis()-start)/1000.0);
		
		return fitness;
	}
	
	private void computeCurrentSolution() {

		ArrayList<Trip> trips = routeGen.generateTrips();
		
		for (Trip trip : trips) {
			String[] route = trip.getRoute().split(" ");
			for (int i = 0; i < route.length; ++i) {
				String edgeId = route[i]; 
				String detectorId = detectorIds.get(edgeId);
				Detector detector = currentSolution.get(detectorId);
				if (detector == null) {
					detector = currentSolution.put(detectorId, new Detector(stopHour));
				}
				detector.vehicles[trip.getDepartTime()/3600 - 1] += 1;
			}
		}
	}
	
	private void printIndividual(Individual ind) {
		String individual = "Individual:";
		for (int i=0; i < ind.getLength(); i++) {
			 individual += " " + ind.getAllele(i);
		}
		System.out.println(individual);
	}
	
	public String getCurrentDectectors()
	{
		String result = "Detectors:\r\n";
		for(Detector d : currentSolution.values())
		{
			result += d + " ";
		}
		result += "\r\nControls:\r\n";				
		for(Detector d : evaluator.controls.values())
		{
			result += d + " ";
		}
		return result;
	}
		
	/**
	 * Discretises alleles of individual to integer values maintaining the group sums of 100
	 * @param individual
	 */
	public static void DiscretiseIndividual(Individual individual)
    {
    	int locus = 0;
    	
    	for(int alleleGroup = 0; alleleGroup < RouteGenerationProblem.GeneGroupLengths.length; alleleGroup++)
    	{
    		int groupLength = RouteGenerationProblem.GeneGroupLengths[alleleGroup];
    		
    		double remainder = 0, value = 0;
    		for (int i = locus; i < locus + groupLength; i++)
    		{	    			
    			if (i != locus + groupLength - 1)
    			{
    				value = Math.round((Double)individual.getAllele(i));
    				// accumulate decimals
    				remainder += (Double)individual.getAllele(i) - value;    				
    			}
    			else
    			{
    				// add accumulated remaining decimals to last value in group (to maintain group sum of 100), round to eliminate numeric precision errors
    				value =  Math.round((Double)individual.getAllele(i) + remainder);
    			}
    			
    			individual.setAllele(i, value);
    		}
    		
    		locus += groupLength;
    	}
    }
	
	/**
	 * Normalises the groups of the individual to sum up to 100
	 * @param individual
	 */
	public static void NormaliseIndividual(Individual individual)
    {
    	int locus = 0;
    	
    	for(int alleleGroup = 0; alleleGroup < RouteGenerationProblem.GeneGroupLengths.length; alleleGroup++)
    	{
    		int groupLength = RouteGenerationProblem.GeneGroupLengths[alleleGroup];
    	
    		// compute the actual sum of the group
    		double sum = 0;
    		for (int i = locus; i < locus + groupLength; i++)
    		{
    			sum += (Double)individual.getAllele(i);
    		}

    		// if the group sum is different from 100 and group is composed of 2 alleles or more
    		if ((sum > 100.001 || sum < 99.999) && groupLength >= 2)
    		{	
    			// normalise alleles
				for (int i = locus; i < locus + groupLength; i++)
	        	{
	    			double targetValue = 100 * (Double)individual.getAllele(i) / sum;

	    			individual.setAllele(i, targetValue);
        		}
    		}
    		
    		locus += groupLength;
    	}

    }

}
