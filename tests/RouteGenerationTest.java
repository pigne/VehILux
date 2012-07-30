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
	
//	@Test
//	public void testMain() {
//		RouteGeneration.main(null);
//	}
	
	@Test
	public void testKirchberg() {
	    RouteGeneration.main(new String[] {
		    	"-baseFolder", "./test/Kirchberg/",
		    	"-baseName", "Kirchberg",
		    	"-referenceNodeId", "56640729#4",
		    	});
	}	
	
//	public void testGetRoute() {
//		String baseFolder = "./test/Luxembourg/";
//		String baseName = "Luxembourg";
//		RouteGeneration rg = new RouteGeneration();
//		rg.setBaseFolder(baseFolder);
//		rg.setBaseName(baseName);
//		rg.readInput();
//		String fromId = "-95510758#0";
//		String toId = "9069577#1";
//		Path path = rg.getPath(fromId, toId);
//		Trip trip = new Trip(path);
//		double weight = trip.computeWeight();
//		System.out.println(trip.getRoute() + " " + weight);
//	}
	
//	@Test
//	public void testEvaluate() {
//		GawronEvaluation.main(new String[] {"-baseFolder", "./test/evalTest/"});
//	}

}
