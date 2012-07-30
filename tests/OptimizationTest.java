import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import junit.framework.TestCase;

import lu.uni.routegeneration.evaluation.RealEvaluation;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Trip;
import lu.uni.routegeneration.helpers.ArgumentsParser;
import lu.uni.routegeneration.helpers.XMLParser;
import lu.uni.routegeneration.jCell.RouteGenerationProblem;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


public class OptimizationTest extends TestCase {
	
	static Logger logger = Logger.getLogger(RouteGenerationTest.class);
	private ArrayList<String> controls = new ArrayList<String>();

	public OptimizationTest() {
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
	public void testOptimization() {

		ArgumentsParser arguments = new ArgumentsParser();
		arguments.parse(new String[] {
	    	"-baseFolder", "./test/Kirchberg/",
	    	"-baseName", "Kirchberg",
	    	"-referenceNodeId", "56640729#4",
	    	});
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


		RealEvaluation evaluator = new RealEvaluation();
		evaluator.setBaseFolder(arguments.getBaseFolder());
		evaluator.setBaseName(arguments.getBaseName());
		evaluator.setStopHour(arguments.getStopHour());
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
}
