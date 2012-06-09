/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file SumoEvaluation.java
 * @date Nov 9, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.TreeSet;

import lu.uni.routegeneration.generation.Flow;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.helpers.LoopHandler;
import lu.uni.routegeneration.helpers.XMLParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 */
public class RealEvaluation {

	// ----------- PARAMETERS ----------

	String baseName = "Luxembourg";
	String baseFolder = "./test/Luxembourg/";

	int stopHour = 11;

	Detector currentDetector = null;
	File currentFile = null;
	String currentDetectorName;
	public HashMap<String, Detector> controls;
	
	/**
	 * @return the baseName
	 */
	public String getBaseName() {
		return baseName;
	}

	/**
	 * @param baseName the baseName to set
	 */
	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	/**
	 * @return the baseFolder
	 */
	public String getBaseFolder() {
		return baseFolder;
	}

	/**
	 * @param baseFolder the baseFolder to set
	 */
	public void setBaseFolder(String folderName) {
		this.baseFolder = folderName;
	}

	/**
	 * @return the stopHour
	 */
	public int getStopHour() {
		return stopHour;
	}

	/**
	 * @param stopHour
	 *            the stopHour to set
	 */
	public void setStopHour(int stopHour) {
		this.stopHour = stopHour;
	}

	public RealEvaluation() {
		
	}
	
	public void readInput() {
		LoopHandler h = new LoopHandler(stopHour);
		XMLParser.readFile(baseFolder + baseName + ".control.xml", h);
		TreeSet<Loop> loops = h.getLoops();
		controls = new HashMap<String, Detector>();
		for (Loop loop : loops) {
			Detector detector = new Detector(stopHour);
			detector.id = loop.getId();
			detector.edge = loop.getEdge();
			for (Flow flow : loop.getFlows()) {
				if (flow.getHour() <= detector.vehicles.length) {
					detector.vehicles[flow.getHour()-1] = flow.getVehicles();
				}
			}
			controls.put(detector.id, detector);
		}
	}
	
	public double compareTo(HashMap<String, Detector> solutions){
		double[] sum = new double[stopHour];
		for (String id : solutions.keySet()){
			Detector solution = solutions.get(id);
 			Detector control = controls.get(id);
 			if (control == null) {
 				System.err.println("Detector Error. Control Detector " + id + " does not exist.");
 			}
 			if (solution.vehicles.length != control.vehicles.length) {
 				System.err.println("Detector Error. Solution and control lengths differ");
 			}
 			for(int i = 0 ; i< solution.vehicles.length; i++){
 				sum[i] += Math.abs((solution.vehicles[i]-control.vehicles[i]));
 			}
		}
		double ssum = 0.0;
		for (double v : sum){
	 		ssum += v;
		}
	 	return ssum;
	}
	
	public double[] eachDetectorCompareTo(HashMap<String, Detector> solutions){
		double[] detectors = new double[solutions.size()];
		int di = 0;
		for(String id : solutions.keySet()){
			double sum = 0;
			Detector solution = solutions.get(id);
 			Detector control = controls.get(id);
 			if(control == null){
 				System.err.println("Detector Error. Does not exist.");
 			}
 			if(solution.vehicles.length != control.vehicles.length){
 				System.err.println("Detector Error. Solution and control length differ");
 			}
 			for(int i = 0 ; i< solution.vehicles.length; i++){
 				sum += Math.abs(control.vehicles[i]-control.vehicles[i]);
 			}
 			detectors[di++]=sum;
		}
		
	 	return detectors;
	}
	
	public void write() {
		String path = baseFolder + baseName + ".real_eval.log";	
		File f = new File(path);
		PrintStream out = null;
		try {
			out = new PrintStream(f);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.printf("\t");
		for (Detector d : controls.values()) {
			out.printf("%s\t", d.id);
		}
		out.println();
		for (int i = 0; i < stopHour; i++) {
			out.printf("%d\t", i + 1);
			for (Detector d : controls.values()) {
				out.printf("%d\t", d.vehicles[i]);
			}
			out.printf("%n");
		}
		out.printf("%n");
		out.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RealEvaluation real = new RealEvaluation();
		real.readInput();
		real.write();
		System.out.println("Done.");
	}

}
