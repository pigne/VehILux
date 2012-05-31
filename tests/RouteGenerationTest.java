import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import junit.framework.TestCase;

import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.evaluation.RealEvaluation;
import lu.uni.routegeneration.generation.Area;
import lu.uni.routegeneration.generation.Flow;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.VType;
import lu.uni.routegeneration.generation.Zone;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class RouteGenerationTest extends TestCase {
	
	static Logger logger = Logger.getLogger(RouteGenerationTest.class);
	private RouteGeneration rg = null;
	
	
	public RouteGenerationTest() {
		BasicConfigurator.configure();
		logger.info("constructor");
	}
	
	@Before
	public void setUp() {
		logger.info("setUp");
		//rg = new RouteGeneration();
	}
	
	@Test
	public void test() {
		logger.info("test");
		fail("Not yet implemented");
	}
	
	@Test
	public void test_2() {
		logger.info("Test 2...") ;
		assertTrue(true) ;
   }
	
	public void checkRoutes() {
		logger.info("check routes");
		//String from = "138950605-AddedOffRampEdge";
		//String to = "138950601#1-AddedOnRampEdge";
		String from = "-35894685#0";
		String to = "-24247122";
		String route = null;
		Iterator<Path> routes;
		Dijkstra djk = new Dijkstra(Dijkstra.Element.NODE, from, "weight");
		Graph graph = rg.getGraph();
		djk.init(rg.getGraph());
		Node nodeFrom = graph.getNode(from);
		Node nodeTo = graph.getNode(to);
		djk.setSource(nodeFrom);	
		try {
			routes = djk.getAllPaths(nodeTo).iterator();
		}
		catch (Exception ex) {
			djk.compute();
			routes = djk.getAllPaths(nodeTo).iterator();
		}
		int routesNumber = 0;
		while (routes.hasNext()) {
			Path path = routes.next();
			routesNumber++;
		}
		route = rg.pathToString(djk.getPath(nodeTo));
	}

}
