import static org.junit.Assert.*;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import junit.framework.Assert;
import junit.framework.TestCase;

import lu.uni.routegeneration.helpers.DumpHandler;
import lu.uni.routegeneration.helpers.LoopHandler;
import lu.uni.routegeneration.helpers.TextFileParser;
import lu.uni.routegeneration.helpers.XMLParser;
import lu.uni.routegeneration.jCell.RouteGenerationProblem;
import lu.uni.routegeneration.ui.EditorPanel;
import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.evaluation.RealEvaluation;
import lu.uni.routegeneration.generation.Flow;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Trip;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.junit.Before;
import org.junit.Test;

public class RouteGenerationTest extends TestCase {
	
	static Logger logger = Logger.getLogger(RouteGenerationTest.class);
	private String baseFolder = "./test/Luxembourg/";
	private String baseName = "Luxembourg";
	private int stopHour = 11;
	
	public RouteGenerationTest() {
		BasicConfigurator.configure();
		logger.info("constructor");
	}
	
	@Before
	public void setUp() {
		logger.info("setUp");
	}
	
	@Test
	public void testLuxGeneration() {
		baseFolder = "./test/Luxembourg/";
		baseName = "Luxembourg";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		File file = new File(baseFolder + outputFolder);
		if (!file.exists()) {
			File dir = new File(baseFolder + outputFolder);  
			dir.mkdir();
		}
		
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.readInput();
		rg.computeDijkstra();
		
		// 0.2
		rg.setInsideFlowRatio(0.2);
		rg.generateTrips();
		XMLParser.writeFlows(baseFolder, baseName, outputFolder + "_02", rg.getTrips(), rg.getVTypes(), rg.getStopTime());
		XMLParser.writeRoutes(baseFolder, baseName, outputFolder+ "_02", rg.getTrips(), rg.getVTypes());
		
		// 0.3
		rg.setInsideFlowRatio(0.3);
		rg.generateTrips();
		XMLParser.writeFlows(baseFolder, baseName, outputFolder + "_03", rg.getTrips(), rg.getVTypes(), rg.getStopTime());
		XMLParser.writeRoutes(baseFolder, baseName, outputFolder+ "_03", rg.getTrips(), rg.getVTypes());
		
		// 0.4
		rg.setInsideFlowRatio(0.4);
		rg.generateTrips();
		XMLParser.writeFlows(baseFolder, baseName, outputFolder + "_04", rg.getTrips(), rg.getVTypes(), rg.getStopTime());
		XMLParser.writeRoutes(baseFolder, baseName, outputFolder+ "_04", rg.getTrips(), rg.getVTypes());
		
	}
	
	@Test
	public void testOptimization() {
		baseFolder = "./test/Kirchberg/";
		baseName = "Kirchberg";
		
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.setStopHour(11);
		rg.setReferenceNodeId("56640729#4");
		rg.readInput();
		rg.computeDijkstra();
		
		RealEvaluation evaluator = new RealEvaluation();
		evaluator.setBaseFolder(baseFolder);
		evaluator.setBaseName(baseName);
		evaluator.setStopHour(11);
		evaluator.readInput();
		RouteGenerationProblem rgProblem = new RouteGenerationProblem(rg, evaluator);
		
		
	}
	@Test
	public void testKirchbergGeneration() {
		baseFolder = "./test/Kirchberg/";
		baseName = "Kirchberg";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		File file = new File(baseFolder + outputFolder);
		if (!file.exists()) {
			File dir = new File(baseFolder + outputFolder);  
			dir.mkdir();
		}
		
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.setReferenceNodeId("56640729#4");
		rg.readInput();
		rg.computeDijkstra();
		
		rg.generateTrips();
		
		XMLParser.writeFlows(baseFolder, baseName, outputFolder, rg.getTrips(), rg.getVTypes(), rg.getStopTime());
		XMLParser.writeRoutes(baseFolder, baseName, outputFolder, rg.getTrips(), rg.getVTypes());
	}
	
	@Test
	public void testShowPoints() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		
		RouteGeneration rg = new RouteGeneration();
		rg.readInput();
		
		EditorPanel editor = new EditorPanel(rg.getZones(), rg.getAreas());
		
		// show loops
		ArrayList<String> edgeIds = new ArrayList<String>();
		for (Loop loop : rg.getLoops()) {
			edgeIds.add(loop.getEdge());
		}
		editor.setNodes("loops", rg.getNodes(edgeIds), Color.magenta);
		
		// show controls
		LoopHandler h = new LoopHandler(stopHour);
		XMLParser.readFile(baseFolder + baseName + ".control.xml", h);
		edgeIds = new ArrayList<String>();
		for (Loop loop : h.getLoops()) {
			edgeIds.add(loop.getEdge());
		}
		editor.setNodes("controls", rg.getNodes(edgeIds), Color.cyan);
		
		// show points of teleporting
		edgeIds = TextFileParser.readStringList(baseFolder + "uniquelines.txt");
		editor.setNodes("teleporting", rg.getNodes(edgeIds), Color.red);
		logger.info("unique lines:" + edgeIds.size());
		File file = new File(baseFolder + outputFolder);
		if (!file.exists()) {
			File dir = new File(baseFolder + outputFolder);  
			dir.mkdir();
		}
		
		editor.run();
		
		editor.generateScreenShot(baseFolder + outputFolder, baseFolder + outputFolder + "screenshot.pdf");
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testDump() {
		ArrayList<String> controls = new ArrayList<String>();
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
		LoopHandler loopHandler = new LoopHandler(stopHour);
		XMLParser.readFile(baseFolder + baseName + ".control.xml", loopHandler);
		ArrayList<String> edgeIds = new ArrayList<String>();
		for (String control : controls) {
			for (Loop loop : loopHandler.getLoops()) {
				if (loop.getId().equals(control)) {
					edgeIds.add(loop.getEdge());
				}
			}
		}
		DumpHandler h = new DumpHandler(edgeIds, stopHour);
		XMLParser.readFile(baseFolder + "dump.xml", h);
		System.out.println("edge\tsec\th.sumEntered\th.sumLeft");
		HashMap<String,Loop> loops = h.getLoops();
		for (String edgeId : edgeIds) {
			Loop loop  = loops.get(edgeId);
			System.out.println(loop.getEdge() + "\t" + loop.getSumSec() + "\t" + loop.getSumEntered() + "\t" + loop.getSumLeft());
		}
		System.out.println("Entered:");
		for (int i = 0; i < stopHour; i++) {
			System.out.printf("\t%d", i + 1);
		}
		System.out.printf("%n");
		for (String edgeId : edgeIds) {
			Loop loop  = loops.get(edgeId);
			System.out.printf("%s\t", loop.getId());
			for (int i = 0; i < stopHour; i++) {
				Flow flow = loop.getFlow(i);
				System.out.printf("%d\t", flow.getEntered());
			}
			System.out.printf("%n");
		}
		System.out.printf("%n");
		
		System.out.println("Left:");
		for (int i = 0; i < stopHour; i++) {
			System.out.printf("\t%d", i + 1);
		}
		System.out.printf("%n");
		for (String edgeId : edgeIds) {
			Loop loop  = loops.get(edgeId);
			System.out.printf("%s\t", loop.getId());
			for (int i = 0; i < stopHour; i++) {
				Flow flow = loop.getFlow(i);
				System.out.printf("%d\t", flow.getLeft());
			}
			System.out.printf("%n");
		}
		System.out.printf("%n");	
	}
	
	public void testGetRoute() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.readInput();
		
		String fromId = "-9068621";
		String toId = "-9070454";
		Path path = rg.getPath(fromId, toId);
		Trip trip = new Trip(path);
		double weight = trip.getWeight();
		System.out.println(trip.getRoute() + " " + weight);
		
	}
	
	
}
