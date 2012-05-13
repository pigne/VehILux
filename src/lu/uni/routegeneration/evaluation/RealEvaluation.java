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

import lu.uni.routegeneration.generation.RouteGeneration;

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

	RouteGeneration rg;

	Detector currentDetector = null;
	File currentFile = null;
	String currentDetectorName;
	public HashMap<String, Detector> controls;

	

	class CLoopHandler extends DefaultHandler {

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);

			if (qName.equals("loop")) {
				currentDetectorName = attributes.getValue("id");
				currentDetector = new Detector(rg.getStopHour());
				currentDetector.id = currentDetectorName;
				currentDetector.edge = attributes.getValue("edge");
				controls.put(currentDetectorName, currentDetector);
			} else if (qName.equals("flow")) {
				int h = (int) (Double.parseDouble(attributes.getValue("hour")));
				if (h <= rg.getStopHour()) {

					currentDetector.vehicles[h - 1] = (int) Double
							.parseDouble(attributes.getValue("cars"))
							+ (int) Double.parseDouble(attributes
									.getValue("trucks"));

				}
			}
		}

	};

	public RealEvaluation(RouteGeneration rg) {
		//org.util.Environment.getGlobalEnvironment().readCommandLine(args);
		//org.util.Environment.getGlobalEnvironment().initializeFieldsOf(this);
		this.rg =rg;
		
		DefaultHandler h;

		controls = new HashMap<String, Detector>();

		PrintStream out = null;

		File f = new File(rg.getBaseFolder() + rg.getBaseName() + ".control.xml");
		h = new CLoopHandler();
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
			parser.parse(new InputSource(new FileInputStream(f)));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		f = new File(rg.getBaseFolder() + rg.getBaseName() + ".real_eval.log");

		out = null;
		try {
			out = new PrintStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.printf("time ");
		for (Detector d : controls.values()) {
			out.printf("%s ", d.id);
		}
		out.println();
		for (int i = rg.getStartHour()-1; i < rg.getStopHour(); i++) {
			out.printf("%d ", i + 1);
			for (Detector d : controls.values()) {
				out.printf("%d ", d.vehicles[i]);
			}
			out.printf("%n");
		}
		out.printf("%n");
		out.close();

	}
	
	
	public double compareTo(HashMap<String, Detector> solution){
		
		double[] sum =new double[rg.getStopHour()];
		
		for(String id : solution.keySet()){
			Detector sd = solution.get(id);
 			Detector cd = controls.get(id);
 			if(cd == null){
 				System.err.println("Detector Error. Does not exist.");
 			}
 			if(sd.vehicles.length != cd.vehicles.length){
 				System.err.println("Detector Error. Solution and control length differ");
 			}
 			for(int i = rg.getStartHour()-1 ; i<sd.vehicles.length; i++){
 				sum[i]+=Math.abs((sd.vehicles[i]-cd.vehicles[i]));
 			}
		}
		double ssum=0.0;
		for(double v : sum){
	 		ssum+=v;
		}
	 	return ssum;
	}

	
	public double[] eachDetectorCompareTo(HashMap<String, Detector> solution){
		
		double[] detectors = new double[solution.size()];
		int di=0;
		for(String id : solution.keySet()){
			double sum =0;
			Detector sd = solution.get(id);
 			Detector cd = controls.get(id);
 			if(cd == null){
 				System.err.println("Detector Error. Does not exist.");
 			}
 			if(sd.vehicles.length != cd.vehicles.length){
 				System.err.println("Detector Error. Solution and control length differ");
 			}
 			for(int i = rg.getStartHour()-1 ; i<sd.vehicles.length; i++){
 				sum+=Math.abs((sd.vehicles[i]-cd.vehicles[i]));
 			}
 			detectors[di++]=sum;
		}
		
	 	return detectors;
	}
}
