package lu.uni.routegeneration.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import lu.uni.routegeneration.generation.Flow;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Trip;
import lu.uni.routegeneration.helpers.ArgumentsParser;
import lu.uni.routegeneration.helpers.DumpHandler;
import lu.uni.routegeneration.helpers.RouteHandler;
import lu.uni.routegeneration.helpers.SummaryHandler;
import lu.uni.routegeneration.helpers.TripHandler;
import lu.uni.routegeneration.helpers.XMLParser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class GawronEvaluation {

	static Logger logger = Logger.getLogger(RouteGeneration.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		//String[] args2 = new String[] {"-baseFolder", "./test/testEval/", "-steps", "5"};
		//ArgumentsParser.parse(args2);
		ArgumentsParser.parse(args);
	    String baseFolder = ArgumentsParser.getBaseFolder();
	    String baseName = ArgumentsParser.getBaseName();
	    double defaultResidentialAreaProbability = ArgumentsParser.getDefaultResidentialAreaProbability();
	    double defaultCommercialAreaProbability = ArgumentsParser.getDefaultCommercialAreaProbability();
	    double defaultIndustrialAreaProbability = ArgumentsParser.getDefaultIndustrialAreaProbability();
	    double insideFlowRatio = ArgumentsParser.getInsideFlowRatio();
	    double shiftingRatio = ArgumentsParser.getShiftingRatio();
	    int steps = ArgumentsParser.getSteps();
	    String referenceNodeId = ArgumentsParser.getReferenceNodeId();
	    int stopHour = ArgumentsParser.getStopHour();
	    int dumpInterval = ArgumentsParser.getDumpInterval();
	    
	    GawronEvaluation evaluation = new GawronEvaluation();
	    evaluation.evaluate(baseFolder, baseName, stopHour, new double[] {defaultResidentialAreaProbability, defaultCommercialAreaProbability, defaultIndustrialAreaProbability}, insideFlowRatio, shiftingRatio, steps, dumpInterval);
	}

	private String baseName; 
	private String baseFolder; 
	private int stopHour; 
	private double shiftingRatio;
	private int steps;
	private int dumpInterval;
	
	public GawronEvaluation() {
		
	}
	
	public void evaluate(String baseFolder, String baseName, int stopHour, double[] defaultProbabilities, double insideFlowRatio, double shiftingRatio, int steps, int dumpInterval) {
		this.baseFolder = baseFolder;
		this.baseName = baseName;
		this.stopHour = stopHour;
		this.shiftingRatio = shiftingRatio;
		this.steps = steps;
		this.dumpInterval = dumpInterval;
		long start = System.currentTimeMillis();
		System.out.printf("%.1f s%n",(start)/1000.0);
	
		try {
			
			writeTripInfo();
			writeSummary();
			writeFitness();
			
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("%.1f s%n",(System.currentTimeMillis()-start)/1000.0);
	}
	
	private void writeFitness() throws IOException {
		RealEvaluation evaluator = new RealEvaluation();
		evaluator.setBaseFolder(baseFolder);
		evaluator.setBaseName(baseName);
		evaluator.setStopHour(stopHour);
		evaluator.readInput();
		HashMap<String, String> detectorIds = evaluator.getDetectorIds();
		//System.out.println("real");
		//printSolution(evaluator.controls);
		
		String outputFileName = "eval_fitness.txt";
		System.out.println("writing " + baseFolder + outputFileName);
		PrintStream stream = new PrintStream(new FileOutputStream(baseFolder + outputFileName));
		double[] dijkstra1 = new double[steps];
		double[] dijkstra2 = new double[steps];
		double[] sumo = new double[steps];
		for (int i = 0; i < steps; ++i) {
			String step = String.format("%03d", i);
			stream.println("Iteration " + i);
			// rou file
			String rouFileName = baseName + "_" + step + ".rou.xml";
			System.out.println("Reading " + baseFolder + rouFileName);
			HashMap<String, Detector> rouSolution = computeRouSolution(baseFolder + rouFileName, detectorIds);
			stream.print("rou before shift:\n");
			printSolution(stream, rouSolution);
			dijkstra1[i] = evaluator.compareTo(rouSolution);
			stream.print("rou fitness:\t" + dijkstra1[i] + "\n");
			
			shiftDetectors(rouSolution, shiftingRatio);
			stream.print("\nrou after shift:\n");
			printSolution(stream, rouSolution);
			dijkstra2[i] = evaluator.compareTo(rouSolution);
			stream.print("rou fitness:\t" + dijkstra2[i] + "\n");
			
			// dump file
			String dumpFileName = "dump_" + step + "_" + dumpInterval + ".xml";
			System.out.println("Reading " + baseFolder + dumpFileName);
			HashMap<String, Detector> dumpSolution = computeDumpSolution(baseFolder + dumpFileName, detectorIds);
			sumo[i] = evaluator.compareTo(dumpSolution);
			stream.print("\ndump:\n");
			printSolution(stream, dumpSolution);
			stream.print("dump fitness:\t" + sumo[i] + "\n");
		}
		
		stream.println("\niter\t dijkstra1 \t dijsktra2 \t SUMO");
		for (int i = 0; i < steps; ++i) {
			stream.println(i+"\t" + dijkstra1[i] + "\t" + dijkstra2[i] + "\t" + sumo[i]);
		}
	}
	
	private HashMap<String, Detector> computeRouSolution(String path, HashMap<String, String> detectorIds) {
		HashMap<String, Detector> currentSolution = new HashMap<String, Detector>();
		RouteHandler routeHandler = new RouteHandler();
		XMLParser.readFile(path, routeHandler);
		ArrayList<Trip> trips = routeHandler.getTrips();
		for (Trip trip : trips) {
			String[] route = trip.getRoute().split(" ");
			for (int i = 0; i < route.length; ++i) {
				String edgeId = route[i]; 
				String detectorId = detectorIds.get(edgeId);
				if (detectorId != null) {
					Detector detector = currentSolution.get(detectorId);
					if (detector == null) {
						currentSolution.put(detectorId, new Detector(stopHour, detectorId));
						detector = currentSolution.get(detectorId);
					}
					detector.vehicles[(int)trip.getDepartTime()/3600] += 1;
				}
			}
		}
		return currentSolution;
	}
	
	private HashMap<String, Detector> computeDumpSolution(String path, HashMap<String, String> detectorIds) {
		HashMap<String, Detector> currentSolution = new HashMap<String, Detector>();
		ArrayList<String> edgeIds = new ArrayList<String>();
		DumpHandler h = new DumpHandler(detectorIds, stopHour);
		XMLParser.readFile(path, h);
		HashMap<String,Loop> loops = h.getLoops();
		for (Loop loop  : loops.values()) {
			for (int i = 0; i < stopHour; i++) {
				Flow flow = loop.getFlow(i);
				int entered = flow.getEntered();
				int left = flow.getLeft();
				String detectorId = loop.getId();
					Detector detector = currentSolution.get(detectorId);
					if (detector == null) {
						currentSolution.put(detectorId, new Detector(stopHour, detectorId));
						detector = currentSolution.get(detectorId);
					}
					detector.vehicles[flow.getHour()] += entered;
			}
		}
		return currentSolution;
	}
	
	private double stepTotalTime;
	private double stepTotalLength;
	private double stepAvgTime;
	private double stepAvgLength;
	
	private void writeTripInfo() throws FileNotFoundException {
		
		String outputTripInfoFileName = "evalTripInfo.txt";
		PrintStream out = new PrintStream(new FileOutputStream(baseFolder + outputTripInfoFileName));
		ArrayList<long[]> stepTripNumbers = new ArrayList<long[]>();
		ArrayList<double[]> stepDurations = new ArrayList<double[]>();
		ArrayList<double[]> stepRouteLengths = new ArrayList<double[]>();
		
		for (int i = 0; i < steps; ++i) {
			String step = String.format("%03d", i);
			
			String tripFileName = "tripinfo_" + step + ".xml";
			TripHandler h = new TripHandler(stopHour);
			XMLParser.readFile(baseFolder + tripFileName, h);
			stepTripNumbers.add(h.getTripNumbers());
			stepDurations.add(h.getDurations());
			stepRouteLengths.add(h.getRouteLenghts());
		}
		
		printTable(out, "tripNumbers", stepTripNumbers);
		printTableDouble(out, "duration", stepDurations);
		printTableDouble(out, "routeLength", stepRouteLengths);
		
	}
	
	private void printTable(PrintStream stream, String title, ArrayList<long[]> steps) {
		stream.println(title);
		stream.print("iter\t");
		for (int i = 0; i < stopHour; ++i) {
			stream.print(i+"\t");
		}
		stream.print("total\n");
		int i = 0;
		for (long[] data : steps) {
			stream.print(i+"\t");
			i++;
			double total = 0;
			for (int j = 0; j < stopHour; ++j) {
				total += data[j];
				stream.print(data[j] + "\t");
			}
			stream.print(total);
			stream.println();
		}
		stream.println();
	}
	
	private void printTableDouble(PrintStream stream, String title, ArrayList<double[]> steps) {
		stream.println(title);
		stream.print("iter\t");
		for (int i = 0; i < stopHour; ++i) {
			stream.print(i+"\t");
		}
		stream.print("total\n");
		int i = 0;
		for (double[] data : steps) {
			stream.print(i+"\t");
			i++;
			double total = 0;
			long count = 0;
			for (int j = 0; j < stopHour; ++j) {
				total += data[j];
				count++;
				stream.print(data[j] + "\t");
			}
			stream.print(total);
			stream.println();
		}
		stream.println();
	}

	private void writeSummary() throws FileNotFoundException {
		String outputSummaryFileName = "evalSummary.txt";
		PrintStream out= new PrintStream(new FileOutputStream(baseFolder + outputSummaryFileName));
		ArrayList<long[]> stepLoaded = new ArrayList<long[]>();
		ArrayList<long[]> stepEmitted = new ArrayList<long[]>();
		ArrayList<long[]> stepRunning = new ArrayList<long[]>();
		ArrayList<long[]> stepWaiting = new ArrayList<long[]>();
		ArrayList<long[]> stepEnded = new ArrayList<long[]>();
		ArrayList<long[]> stepLoaded2 = new ArrayList<long[]>();
		ArrayList<long[]> stepEmitted2 = new ArrayList<long[]>();
		ArrayList<long[]> stepRunning2 = new ArrayList<long[]>();
		ArrayList<long[]> stepWaiting2 = new ArrayList<long[]>();
		ArrayList<long[]> stepEnded2 = new ArrayList<long[]>();
		
		for (int i = 0; i < steps; ++i) {
			String step = String.format("%03d", i);
			String fileName = "summary_" + step + ".xml";
			SummaryHandler h = new SummaryHandler(stopHour);
			XMLParser.readFile(baseFolder + fileName, h);
			stepLoaded.add(h.getLoadeds());
			stepEmitted.add(h.getEmitteds());
			stepRunning.add(h.getRunnings());
			stepWaiting.add(h.getWaitings());
			stepEnded.add(h.getEndeds());
			stepLoaded2.add(h.getLoadeds2());
			stepEmitted2.add(h.getEmitteds2());
			stepRunning2.add(h.getRunnings2());
			stepWaiting2.add(h.getWaitings2());
			stepEnded2.add(h.getEndeds2());
		}
		
		printTable(out, "loaded", stepLoaded);
		printTable(out, "loaded2", stepLoaded2);
		printTable(out, "emitted", stepEmitted);
		printTable(out, "emitted2", stepEmitted2);
		printTable(out, "running", stepRunning);
		printTable(out, "running2", stepRunning2);
		printTable(out, "waiting", stepWaiting);
		printTable(out, "waiting2", stepWaiting2);
		printTable(out, "ended", stepEnded);
		printTable(out, "ended2", stepEnded2);
		
	}
	
	public void printSolution(PrintStream out, HashMap<String, Detector> solution) throws IOException {
		for (Detector detector : solution.values()) {
			out.print(detector.id + "\t");
			for (int i = 0; i < detector.vehicles.length; ++i) {
				out.print(detector.vehicles[i] + "\t");
			}
			out.print("\n");
		}
	}
	
	private void shiftDetectors(HashMap<String, Detector> solution, double shiftingRatio) {
		for(Detector d : solution.values()){
			//d.reset();
			d.setShiftingRatio(shiftingRatio);
			// Applies shiftingRatio for each control point
			d.shift();
		}
	}
}
