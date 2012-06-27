import static org.junit.Assert.*;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import jcell.EvolutionaryAlg;
import jcell.Individual;
import jcell.Problem;

import junit.framework.Assert;
import junit.framework.TestCase;

import lu.uni.routegeneration.helpers.ArgumentsParser;
import lu.uni.routegeneration.helpers.DumpHandler;
import lu.uni.routegeneration.helpers.LoopHandler;
import lu.uni.routegeneration.helpers.NetHandler;
import lu.uni.routegeneration.helpers.RouteHandler;
import lu.uni.routegeneration.helpers.TextFileParser;
import lu.uni.routegeneration.helpers.XMLParser;
import lu.uni.routegeneration.jCell.CoevEA;
import lu.uni.routegeneration.jCell.RouteGenerationProblem;
import lu.uni.routegeneration.ui.EditorPanel;
import lu.uni.routegeneration.ui.Lane;
import lu.uni.routegeneration.ui.ShapeType;
import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.evaluation.GawronEvaluation;
import lu.uni.routegeneration.evaluation.RealEvaluation;
import lu.uni.routegeneration.generation.Flow;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Trip;
import lu.uni.routegeneration.generation.ZoneType;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.graphstream.graph.Path;
import org.junit.Before;
import org.junit.Test;

public class RouteGenerationTest extends TestCase {
	
	static Logger logger = Logger.getLogger(RouteGenerationTest.class);
	private String baseFolder = "./test/Luxembourg/";
	private String baseName = "Luxembourg";
	private int stopHour = 11;
	private ArrayList<String> controls = new ArrayList<String>();

	public RouteGenerationTest() {
		BasicConfigurator.configure();
		logger.info("constructor");
	}
	
	@Before
	public void setUp() {
		logger.info("setUp");
		controls.add("1431");
		controls.add("1429");
		controls.add("445");
		controls.add("433");
		controls.add("432");
		controls.add("420");
		controls.add("415");
		controls.add("412");
		controls.add("407");
		controls.add("404");
		controls.add("403");
		controls.add("401");
		controls.add("400");
	}
	
	
	@Test
	public void testGenerateTrips() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		File file = new File(baseFolder + outputFolder);
		if (!file.exists()) {
			File dir = new File(baseFolder + outputFolder);  
			dir.mkdir();
		}
		
		String baseFolder = ArgumentsParser.getBaseFolder();
	    String baseName = ArgumentsParser.getBaseName();
	    double defaultResidentialAreaProbability = ArgumentsParser.getDefaultResidentialAreaProbability();
	    double defaultCommercialAreaProbability = ArgumentsParser.getDefaultCommercialAreaProbability();
	    double defaultIndustrialAreaProbability = ArgumentsParser.getDefaultIndustrialAreaProbability();
	    double insideFlowRatio = ArgumentsParser.getInsideFlowRatio();
	    double shiftingRatio = ArgumentsParser.getShiftingRatio();
	    String referenceNodeId = ArgumentsParser.getReferenceNodeId();
	    int stopHour = ArgumentsParser.getStopHour();
		
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.setStopHour(stopHour);
		rg.setReferenceNodeId(referenceNodeId);
		rg.readInput();
		rg.setInsideFlowRatio(insideFlowRatio);
		rg.setDefaultResidentialAreaProbability(defaultResidentialAreaProbability);
		rg.setDefaultCommercialAreaProbability(defaultCommercialAreaProbability);
		rg.setDefaultIndustrialAreaProbability(defaultIndustrialAreaProbability);
		
		rg.computeDijkstra();
		ArrayList<Trip> trips = rg.generateSortedTrips();
		int res=0;
		int com=0;
		int ind=0;
		for (Trip trip : trips) {
			//logger.info("trip dest: " + trip.getDestinationZoneType().name());
			if (trip.getDestinationZoneType().equals(ZoneType.RESIDENTIAL)) {
				res++;
			}
			else if (trip.getDestinationZoneType().equals(ZoneType.COMMERCIAL)) {
				com++;
			}
			else if (trip.getDestinationZoneType().equals(ZoneType.INDUSTRIAL)) {
				ind++;
			}
		}
		logger.info("res" + res);
		logger.info("com" + com);
		logger.info("ind" + ind);
		
		XMLParser.writeFlows(rg.getBaseFolder(), rg.getBaseName(), outputFolder, rg.getTrips(), rg.getVTypes(), rg.getStopTime());
		XMLParser.writeRoutes(rg.getBaseFolder(), rg.getBaseName(), outputFolder, rg.getTrips(), rg.getVTypes());
	}
	
	@Test
	public void testOptimization() {
		String baseFolder = ArgumentsParser.getBaseFolder();
	    
		String baseName = ArgumentsParser.getBaseName();
	    double defaultResidentialAreaProbability = ArgumentsParser.getDefaultResidentialAreaProbability();
	    double defaultCommercialAreaProbability = ArgumentsParser.getDefaultCommercialAreaProbability();
	    double defaultIndustrialAreaProbability = ArgumentsParser.getDefaultIndustrialAreaProbability();
	    double insideFlowRatio = ArgumentsParser.getInsideFlowRatio();
	    double shiftingRatio = ArgumentsParser.getShiftingRatio();
	    String referenceNodeId = ArgumentsParser.getReferenceNodeId();
	    int stopHour = 11;
		
		baseFolder = "./test/Kirchberg/";
		baseName = "Kirchberg";
		referenceNodeId = "56640729#4";
	    
	    RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.setStopHour(stopHour);
		rg.setReferenceNodeId(referenceNodeId);
		rg.readInput();
		rg.setInsideFlowRatio(insideFlowRatio);
		rg.setDefaultResidentialAreaProbability(defaultResidentialAreaProbability);
		rg.setDefaultCommercialAreaProbability(defaultCommercialAreaProbability);
		rg.setDefaultIndustrialAreaProbability(defaultIndustrialAreaProbability);
		
		rg.computeDijkstra();
		
		RealEvaluation evaluator = new RealEvaluation();
		evaluator.setBaseFolder(baseFolder);
		evaluator.setBaseName(baseName);
		evaluator.setStopHour(stopHour);
		
		evaluator.readInput();

		RouteGenerationProblem problem = new RouteGenerationProblem(rg, evaluator);   
    	double[] ind = new double[] { 
    			9.019859885898525,
    			16.406314982888496,
    			74.57382513121298,
    			29.588267662323528,
    			17.95847782067055,
    			51.273326645246534,
    			1.1799278717593802,
    			4.970798361740271,
    			95.02920163825972,
    			28.684498573393284,
    			71.31550142660672,
    			69.5180108613385, // inner traffic
    			39.292397212308};
    	double fitness = problem.evalTest(ind);
    	logger.info("fitness: " + fitness);
	}
	
//	@Test
//	public void testKirchbergGeneration() {
//		baseFolder = "./test/Kirchberg/";
//		baseName = "Kirchberg";
//		rg.setReferenceNodeId("56640729#4");
	
	@Test 
	public void testReadTrips() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		
	    String baseFolder = ArgumentsParser.getBaseFolder();
	    String baseName = ArgumentsParser.getBaseName();
	    double defaultResidentialAreaProbability = ArgumentsParser.getDefaultResidentialAreaProbability();
	    double defaultCommercialAreaProbability = ArgumentsParser.getDefaultCommercialAreaProbability();
	    double defaultIndustrialAreaProbability = ArgumentsParser.getDefaultIndustrialAreaProbability();
	    double insideFlowRatio = ArgumentsParser.getInsideFlowRatio();
	    double shiftingRatio = ArgumentsParser.getShiftingRatio();
	    String referenceNodeId = ArgumentsParser.getReferenceNodeId();
	    int stopHour = ArgumentsParser.getStopHour();
	    
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.setStopHour(stopHour);
		rg.setReferenceNodeId(referenceNodeId);
		rg.readInput();
		rg.setInsideFlowRatio(insideFlowRatio);
		rg.setDefaultResidentialAreaProbability(defaultResidentialAreaProbability);
		rg.setDefaultCommercialAreaProbability(defaultCommercialAreaProbability);
		rg.setDefaultIndustrialAreaProbability(defaultIndustrialAreaProbability);
		
		EditorPanel editor = new EditorPanel(rg.getZones(), rg.getAreas());
		
		// show loops
		ArrayList<String> edgeIds = new ArrayList<String>();
		for (Loop loop : rg.getLoops()) {
			edgeIds.add(loop.getEdge());
		}
		editor.setNodes("loops", rg.getNodes(edgeIds), Color.cyan, 10, ShapeType.RECT );
		
		// show controls
		LoopHandler h = new LoopHandler(stopHour);
		XMLParser.readFile(baseFolder + baseName + ".control.xml", h);
		edgeIds = new ArrayList<String>();
		for (Loop loop : h.getLoops()) {
			edgeIds.add(loop.getEdge());
		}
		editor.setNodes("controls", rg.getNodes(edgeIds), Color.yellow, 10, ShapeType.OVAL);
		
		// show sources
		RouteHandler routeHandler = new RouteHandler();
		XMLParser.readFile(baseFolder + baseName + ".rou.xml", routeHandler);
		ArrayList<Trip> trips = routeHandler.getTrips();
		int currentHour = 0;
		ArrayList<String>[] sources = new ArrayList[stopHour];
		ArrayList<String>[] destinations = new ArrayList[stopHour];
		for (int i = 0; i < stopHour; ++i) {
			sources[i] = new ArrayList<String>();
			destinations[i] = new ArrayList<String>();
		}
		logger.info("trips: " +trips.size());
		for (Trip trip : trips) {
			int hour = (int) trip.getDepartTime()/3600;
			sources[hour].add(trip.getSourceId());
			destinations[hour].add(trip.getDestinationId());
		}
		//editor.setDisplayAreas(true);
		editor.setDisplayPoints(new String[]{"sources", "destinations"});
		editor.setDisplayEdges(true);
		editor.setDisplayZones(true);
		editor.run();
		
		for (int i = 0; i < stopHour; ++i) {
			// show sources
			//editor.setNodes("sources", rg.getNodes(sources[i]), Color.green, 5, ShapeType.OVAL);
			
			// show destinations
			editor.setNodes("destinations", rg.getNodes(destinations[i]), Color.orange, 5, ShapeType.OVAL);
			
			//editor.generateScreenShot(baseFolder + outputFolder, baseFolder + outputFolder + "screenshot"+i+".pdf");
			editor.writeImage(baseFolder + outputFolder, baseFolder + outputFolder + "screenshot"+i+".png");
			
		}
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Test
//	public void testShowMap() {
////		baseFolder = "./test/Kirchberg/";
////		baseName = "Kirchberg";
//		
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
//		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
//		
//		RouteGeneration rg = new RouteGeneration();
//		rg.setBaseFolder(baseFolder);
//		rg.setBaseName(baseName);
////		rg.setReferenceNodeId("56640729#4");
//		rg.readInput();
//		
//		EditorPanel editor = new EditorPanel(rg.getZones(), rg.getAreas());
//		editor.run();
//		
//		// show loops
//		ArrayList<String> edgeIds = new ArrayList<String>();
//		for (Loop loop : rg.getLoops()) {
//			edgeIds.add(loop.getEdge());
//		}
//		editor.setNodes("loops", rg.getNodes(edgeIds), Color.black, 12, ShapeType.RECT);
//		
//		// show controls
//		LoopHandler h = new LoopHandler(stopHour);
//		XMLParser.readFile(baseFolder + baseName + ".control.xml", h);
//		edgeIds = new ArrayList<String>();
//		for (Loop loop : h.getLoops()) {
//			edgeIds.add(loop.getEdge());
//		}
//		editor.setNodes("controls", rg.getNodes(edgeIds), Color.black, 12, ShapeType.OVAL);
//		
//		// show points of teleporting
////		edgeIds = TextFileParser.readStringList(baseFolder + "uniquelines.txt");
////		editor.setNodes("teleporting", rg.getNodes(edgeIds), Color.red, 5);
////		logger.info("unique lines:" + edgeIds.size());
////		File file = new File(baseFolder + outputFolder);
////		if (!file.exists()) {
////			File dir = new File(baseFolder + outputFolder);  
////			dir.mkdir();
////		}
//		
//		NetHandler netHandler = new NetHandler(true);
//		XMLParser.readFile(baseFolder + baseName + ".net.xml", netHandler);
//		ArrayList<Lane> edges = netHandler.getEdges();
//		editor.setEdges(edges);
//		
//		editor.setDisplayAreas(true);
//		editor.setDisplayPoints(new String[]{"loops", "controls"});
//		editor.setDisplayEdges(true);
//		editor.setDisplayZones(true);
//		
//		
//		editor.generateScreenShot(baseFolder + outputFolder, baseFolder + outputFolder + "screenshot.pdf");
//		editor.writeImage(baseFolder + outputFolder, baseFolder + outputFolder + "screenshot.png");
//		
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
//	@Test
//	public void testShowPoints() {
//		
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
//		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
//		
//		RouteGeneration rg = new RouteGeneration();
//		rg.readInput();
//		
//		EditorPanel editor = new EditorPanel(rg.getZones(), rg.getAreas());
//		
//		// show loops
//		ArrayList<String> edgeIds = new ArrayList<String>();
//		for (Loop loop : rg.getLoops()) {
//			edgeIds.add(loop.getEdge());
//		}
//		editor.setNodes("loops", rg.getNodes(edgeIds), Color.blue, 10, ShapeType.RECT);
//		
//		// show controls
//		LoopHandler h = new LoopHandler(stopHour);
//		XMLParser.readFile(baseFolder + baseName + ".control.xml", h);
//		edgeIds = new ArrayList<String>();
//		for (Loop loop : h.getLoops()) {
//			edgeIds.add(loop.getEdge());
//		}
//		editor.setNodes("controls", rg.getNodes(edgeIds), Color.red, 10, ShapeType.OVAL);
//	
//		editor.run();
//		
//		editor.generateScreenShot(baseFolder + outputFolder, baseFolder + outputFolder + "screenshot.pdf");
//		
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public void testGetRoute() {
//		
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
//		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
//		
//		RouteGeneration rg = new RouteGeneration();
//		rg.setBaseFolder(baseFolder);
//		rg.setBaseName(baseName);
//		rg.readInput();
//		
//		String fromId = "-31670823#0";
//		String toId = "149399817#1";
//		Path path = rg.getPath(fromId, toId);
//		Trip trip = new Trip(path);
//		double weight = trip.computeWeight();
//		System.out.println(trip.getRoute() + " " + weight);
//		
//	}
//	
	
	@Test
	public void testEvaluate() {
		//ArgumentsParser.parse(args);
	    String baseFolder = ArgumentsParser.getBaseFolder();
		baseFolder = "./test/evalTest/";
	    String baseName = ArgumentsParser.getBaseName();
	    double defaultResidentialAreaProbability = ArgumentsParser.getDefaultResidentialAreaProbability();
	    double defaultCommercialAreaProbability = ArgumentsParser.getDefaultCommercialAreaProbability();
	    double defaultIndustrialAreaProbability = ArgumentsParser.getDefaultIndustrialAreaProbability();
	    double insideFlowRatio = ArgumentsParser.getInsideFlowRatio();
	    double shiftingRatio = ArgumentsParser.getShiftingRatio();
	    String referenceNodeId = ArgumentsParser.getReferenceNodeId();
	    int stopHour = ArgumentsParser.getStopHour();
		GawronEvaluation evaluation = new GawronEvaluation();
		evaluation.evaluate(baseFolder, baseName, stopHour, new double[] {defaultResidentialAreaProbability, defaultCommercialAreaProbability, defaultIndustrialAreaProbability}, insideFlowRatio, shiftingRatio, 1, 3600);
	}
//	
//	@Test
//	public void testEvaluateBig() {
//		//ArgumentsParser.parse(args);
//	    String baseFolder = ArgumentsParser.getBaseFolder();
//		baseFolder = "./test/evalBig/";
//	    String baseName = ArgumentsParser.getBaseName();
//	    double defaultResidentialAreaProbability = ArgumentsParser.getDefaultResidentialAreaProbability();
//	    double defaultCommercialAreaProbability = ArgumentsParser.getDefaultCommercialAreaProbability();
//	    double defaultIndustrialAreaProbability = ArgumentsParser.getDefaultIndustrialAreaProbability();
//	    double insideFlowRatio = ArgumentsParser.getInsideFlowRatio();
//	    double shiftingRatio = ArgumentsParser.getShiftingRatio();
//	    String referenceNodeId = ArgumentsParser.getReferenceNodeId();
//	    int stopHour = ArgumentsParser.getStopHour();
//		GawronEvaluation evaluation = new GawronEvaluation();
//		evaluation.evaluate(baseFolder, baseName, stopHour, new double[] {defaultResidentialAreaProbability, defaultCommercialAreaProbability, defaultIndustrialAreaProbability}, insideFlowRatio, shiftingRatio, 1, 3600);
//	}
	
}
