import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import junit.framework.TestCase;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Trip;
import lu.uni.routegeneration.helpers.ArgumentsParser;
import lu.uni.routegeneration.helpers.LoopHandler;
import lu.uni.routegeneration.helpers.NetHandler;
import lu.uni.routegeneration.helpers.RouteHandler;
import lu.uni.routegeneration.helpers.VehlLuxLog;
import lu.uni.routegeneration.helpers.XMLParser;
import lu.uni.routegeneration.ui.EditorPanel;
import lu.uni.routegeneration.ui.Lane;
import lu.uni.routegeneration.ui.ShapeType;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


public class EditorTest extends TestCase {
	
	static Logger logger = Logger.getLogger(RouteGenerationTest.class);
	
	private ArrayList<String> controls = new ArrayList<String>();

	public EditorTest() {
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
	public void testShowODTrips() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		
		ArgumentsParser arguments = new ArgumentsParser();
//		arguments.parse(new String[] {
//	    	"-baseFolder", "./test/Kirchberg/",
//	    	"-baseName", "Kirchberg",
//	    	"-referenceNodeId", "56640729#4",
//	    	});
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(arguments.getBaseFolder());
		rg.setBaseName(arguments.getBaseName());
		rg.setStopHour(arguments.getStopHour());
		rg.setReferenceNodeId(arguments.getReferenceNodeId());
		rg.readInput();
		rg.setInsideFlowRatio(arguments.getInsideFlowRatio());
		rg.setDefaultResidentialAreaProbability(arguments.getDefaultResidentialAreaProbability());
		rg.setDefaultCommercialAreaProbability(arguments.getDefaultCommercialAreaProbability());
		rg.setDefaultIndustrialAreaProbability(arguments.getDefaultIndustrialAreaProbability());
		
		EditorPanel editor = new EditorPanel(rg.getZones(), rg.getAreas());
		
		// set loops
		ArrayList<String> edgeIds = new ArrayList<String>();
		for (Loop loop : rg.getLoops()) {
			edgeIds.add(loop.getEdge());
		}
		editor.setNodes("loops", rg.getNodes(edgeIds), Color.BLACK, 15, ShapeType.RECT );
		
		// set controls
		LoopHandler h = new LoopHandler(rg.getStopHour());
		XMLParser.readFile(arguments.getBaseFolder() + arguments.getBaseName() + ".control.xml", h);
		edgeIds = new ArrayList<String>();
		for (Loop loop : h.getLoops()) {
			edgeIds.add(loop.getEdge());
		}
		editor.setNodes("controls", rg.getNodes(edgeIds), Color.BLACK, 15, ShapeType.OVAL);
		
		// set sources
		RouteHandler routeHandler = new RouteHandler();
		XMLParser.readFile(arguments.getBaseFolder() + arguments.getBaseName() + ".rou.xml", routeHandler);
		ArrayList<Trip> trips = routeHandler.getTrips();
		int currentHour = 0;
		ArrayList<String>[] sources = new ArrayList[rg.getStopHour()];
		ArrayList<String>[] destinations = new ArrayList[rg.getStopHour()];
		for (int i = 0; i < rg.getStopHour(); ++i) {
			sources[i] = new ArrayList<String>();
			destinations[i] = new ArrayList<String>();
		}
		logger.info("trips: " +trips.size());
		for (Trip trip : trips) {
			int hour = (int) trip.getDepartTime()/3600;
			if (hour < rg.getStopHour()) {
				sources[hour].add(trip.getSourceId());
				destinations[hour].add(trip.getDestinationId());
			}
		}
		
		editor.setDisplayAreas(true);
		editor.setDisplayEdges(true);
		editor.setDisplayZones(true);
		editor.setDisplayPoints(new String[]{"sources", "destinations"});
		editor.run();
		
		editor.writeImage(arguments.getBaseFolder() + outputFolder, arguments.getBaseFolder() + outputFolder + "background.png");
		
		NetHandler netHandler = new NetHandler(true);
		XMLParser.readFile(arguments.getBaseFolder() + arguments.getBaseName() + ".net.xml", netHandler);
		ArrayList<Lane> edges = netHandler.getEdges();
		editor.setEdges(edges);
		editor.setDisplayAreas(false);
		editor.setDisplayEdges(true);
		editor.setDisplayZones(true);
//		editor.setDisplayPoints(new String[]{"sources", "destinations"});
		editor.setDisplayPoints(new String[]{"loops", "controls"});
		editor.writeImage(arguments.getBaseFolder() + outputFolder, arguments.getBaseFolder() + outputFolder + "background2.png");

		editor.setTransparent(true);
		editor.setDisplayEdges(false);
		editor.setDisplayZones(false);
		
		for (int i = 0; i < rg.getStopHour(); ++i) {
			// show sources
			editor.setNodes("sources", rg.getNodes(sources[i]), Color.green, 5, ShapeType.OVAL);
			
			// show destinations
			editor.setNodes("destinations", rg.getNodes(destinations[i]), Color.orange, 5, ShapeType.OVAL);
			
			//editor.generateScreenShot(baseFolder + outputFolder, baseFolder + outputFolder + "screenshot"+i+".pdf");
			editor.writeImage(arguments.getBaseFolder() + outputFolder, arguments.getBaseFolder() + outputFolder + "screenshot"+i+".png");
			
		}
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
	
}
