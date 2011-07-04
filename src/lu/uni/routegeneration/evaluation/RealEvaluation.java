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
	// --- base name
	String baseName = "LuxembourgVille";

	/**
	 * @return the baseName
	 */
	public String getBaseName() {
		return baseName;
	}

	/**
	 * @param baseName
	 *            the baseName to set
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
	 * @param baseFolder
	 *            the baseFolder to set
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

	// --- folder name
	String baseFolder = "./test/";

	int stopHour = 11;

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
				currentDetector = new Detector(stopHour);
				currentDetector.id = currentDetectorName;
				currentDetector.edge = attributes.getValue("edge");
				controls.put(currentDetectorName, currentDetector);
			} else if (qName.equals("flow")) {
				int h = (int) (Double.parseDouble(attributes.getValue("hour")));
				if (h <= stopHour) {

					currentDetector.vehicles[h - 1] = (int) Double
							.parseDouble(attributes.getValue("cars"))
							+ (int) Double.parseDouble(attributes
									.getValue("trucks"));

				}
			}
		}

	};

	public RealEvaluation() {
		//org.util.Environment.getGlobalEnvironment().readCommandLine(args);
		//org.util.Environment.getGlobalEnvironment().initializeFieldsOf(this);
		DefaultHandler h;

		controls = new HashMap<String, Detector>();

		PrintStream out = null;

		File f = new File(baseFolder + baseName + ".control.xml");
		h = new CLoopHandler();
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
			parser.parse(new InputSource(new FileInputStream(f)));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		f = new File(baseFolder + baseName + ".real_eval.log");

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
		for (int i = 0; i < stopHour; i++) {
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
		
		double[] sum =new double[stopHour];
		
		for(String id : solution.keySet()){
			Detector sd = solution.get(id);
 			Detector cd = controls.get(id);
 			if(cd == null){
 				System.err.println("Detector Error. Does not exist.");
 			}
 			if(sd.vehicles.length != cd.vehicles.length){
 				System.err.println("Detector Error. Solution and control length differ");
 			}
 			for(int i = 0 ; i<sd.vehicles.length; i++){
 				sum[i]+=Math.abs((sd.vehicles[i]-cd.vehicles[i]));
 			}
		}
		double ssum=0.0;
		for(double v : sum){
	 		ssum+=v;
		}
	 	return ssum;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RealEvaluation();
		System.out.println("Done.");

	}

}
