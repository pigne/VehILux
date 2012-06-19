/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file RouteGeneration.java
 * @date Nov 2, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;

import lu.uni.routegeneration.helpers.AreasHandler;
import lu.uni.routegeneration.helpers.ArgumentsParser;
import lu.uni.routegeneration.helpers.LoopHandler;
import lu.uni.routegeneration.helpers.MathHelper;
import lu.uni.routegeneration.helpers.NetHandler;
import lu.uni.routegeneration.helpers.OSMHandler;
import lu.uni.routegeneration.helpers.VehicleTypesHandler;
import lu.uni.routegeneration.helpers.XMLParser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphParseException;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.jhlabs.map.proj.Projection;


/**
 * Main class that handles all the process of creating mobility traces
 */
public class RouteGeneration {

	// Define a static logger variable so that it references the Logger instance named "RouteGeneration".
	static Logger logger = Logger.getLogger(RouteGeneration.class);

	private void runGenerationForInsideFlowRatio(String baseFolder, String baseName, int firstInnerRatio, int stepSize, int stepsNumber, String outputFolder) {
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.readInput();
		rg.computeDijkstra();
		double insideFlowRatio = firstInnerRatio;
		for (int i = 0; i < stepsNumber; ++i) {
			insideFlowRatio += i*stepSize;
			rg.setInsideFlowRatio(insideFlowRatio);
			logger.info("generating trips for InsideFlowRatio 0." + (insideFlowRatio));
			rg.generateSortedTrips();
			String outputFolderRatio = outputFolder + "_0" + insideFlowRatio + "/";
			XMLParser.writeFlows(rg.getBaseFolder(), rg.getBaseName(), outputFolderRatio, rg.getTrips(), rg.getVTypes(), rg.getStopTime());
			XMLParser.writeRoutes(rg.getBaseFolder(), rg.getBaseName(), outputFolderRatio, rg.getTrips(), rg.getVTypes());
			logger.info("generated " + rg.getTrips().size() + " trips");
		}
	}
	
	private static void runGeneration(String baseFolder, String baseName, int stopHour, double[] defaultProbabilities, double insideFlowRatio, double shiftingRatio, String referenceNodeId) {		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
		
		RouteGeneration rg = new RouteGeneration();
		rg.setBaseFolder(baseFolder);
		rg.setBaseName(baseName);
		rg.setStopHour(stopHour);
		rg.setReferenceNodeId(referenceNodeId);
		rg.readInput();
		rg.setInsideFlowRatio(insideFlowRatio);
		rg.setDefaultResidentialAreaProbability(defaultProbabilities[0]);
		rg.setDefaultCommercialAreaProbability(defaultProbabilities[1]);
		rg.setDefaultIndustrialAreaProbability(defaultProbabilities[2]);
		
		rg.computeDijkstra();
		
		rg.generateSortedTrips();
		XMLParser.writeFlows(rg.getBaseFolder(), rg.getBaseName(), outputFolder, rg.getTrips(), rg.getVTypes(), rg.getStopTime());
		XMLParser.writeRoutes(rg.getBaseFolder(), rg.getBaseName(), outputFolder, rg.getTrips(), rg.getVTypes());
		
		int res=0;
		int com=0;
		int ind=0;
		for (Trip trip : rg.getTrips()) {
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
		logger.info("number of residential destinations: " + res);
		logger.info("number of commercial destinations: " + com);
		logger.info("number of industrial destination: " + ind);
		
	}
			
	public static void main(String[] args) {
		
		// Set up a simple configuration that logs on the console.
	    BasicConfigurator.configure();
	    //logger.setLevel(Level.WARN);
		
	    // student:
//	    String baseFolder = "./test/Luxembourg_newParameters/";
//		String baseName = "Luxembourg";
//		double defaultResidentialAreaProbability = 0.07;
//		double defaultCommercialAreaProbability = 0.62;
//		double defaultIndustrialAreaProbability = 0.16;
//		double insideFlowRatio = 0.75;
//		double shiftingRatio = 0.36;
		
		ArgumentsParser.parse(args);
	    String baseFolder = ArgumentsParser.getBaseFolder();
	    String baseName = ArgumentsParser.getBaseName();
	    double defaultResidentialAreaProbability = ArgumentsParser.getDefaultResidentialAreaProbability();
	    double defaultCommercialAreaProbability = ArgumentsParser.getDefaultCommercialAreaProbability();
	    double defaultIndustrialAreaProbability = ArgumentsParser.getDefaultIndustrialAreaProbability();
	    double insideFlowRatio = ArgumentsParser.getInsideFlowRatio();
	    double shiftingRatio = ArgumentsParser.getShiftingRatio();
	    String referenceNodeId = ArgumentsParser.getReferenceNodeId();
	    int stopHour = ArgumentsParser.getStopHour();
	    
	    runGeneration(baseFolder, baseName,	stopHour, new double[] {defaultResidentialAreaProbability, defaultCommercialAreaProbability, defaultIndustrialAreaProbability}, insideFlowRatio, shiftingRatio, referenceNodeId);
	}
	
	// ----------- PARAMETERS ----------
	
	private String baseName; // Project name. Is assumed to be the base name of all configuration files (ex. MyProject.rou.xml, MyProject.net.xml)
	private String baseFolder; // Path that to the folder containing configuration files.
	private int stopHour; // Time of the running simulation (hours)
	private String referenceNodeId;
	private double insideFlowRatio;
	private Point2D.Double netOffset;	
	private Projection proj;
	private HashMap<String, Zone> zones;
	private ArrayList<Area> areas;
	private ArrayList<VType> vtypes;
	private TreeSet<Loop> loops;
	private ArrayList<Trip> trips;
	private Graph graph;
	private Dijkstra referenceDjk;	
	private int stopTime = stopHour;
	private double sumResidentialSurface;
	private double sumCommercialSurface;
	private double sumIndustrialSurface;
	private Area defaultIndustrialArea;
	private Area defaultCommercialArea;
	private Area defaultResidentialArea;
	private Random random;
	
	// ----------- Getters & Setters ----------
	
	public Area getDefaultIndustrialArea() {
		return defaultIndustrialArea;
	}

	public Area getDefaultCommercialArea() {
		return defaultCommercialArea;
	}

	public Area getDefaultResidentialArea() {
		return defaultResidentialArea;
	}
	
	public int getStopHour() {
		return stopHour;
	}

	public void setStopHour(int stopHour) {
		this.stopHour = stopHour;
		this.stopTime = stopHour * 3600;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getBaseFolder() {
		return baseFolder;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public double getInsideFlowRatio() {
		return insideFlowRatio;
	}

	public void setInsideFlowRatio(double insideFlowRatio) {
		this.insideFlowRatio = insideFlowRatio;
	}

	public String getReferenceNodeId() {
		return referenceNodeId;
	}

	public void setReferenceNodeId(String referenceNodeId) {
		this.referenceNodeId = referenceNodeId;
	}

	/**
	 * @return the list of vehicle types
	 */
	public ArrayList<VType> getVTypes() { 
		return vtypes; 
	}


	public ArrayList<Trip> getTrips() {
		return trips;
	}

	
	/**
	 * @return the graph
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * @return time of the simulation in seconds
	 */
	public int getStopTime() {
		return stopTime;
	}
	
	public HashMap<String, Zone> getZones() {
		return zones;
	}

	public ArrayList<Area> getAreas() {
		return areas;
	}
	
	public TreeSet<Loop> getLoops() {
		return loops;
	}
	
	// ----------- Constructor  ----------
	
	public RouteGeneration() {
		random = new Random(System.currentTimeMillis());
		stopTime = stopHour * 3600;
		sumResidentialSurface = 0.0;
		sumCommercialSurface = 0.0;
		sumIndustrialSurface = 0.0;
		baseName = "Luxembourg"; // Project name. Is assumed to be the base name of all configuration files (ex. MyProject.rou.xml, MyProject.net.xml)
		baseFolder = "./test/Luxembourg/"; // Path that to the folder containing configuration files.
		stopHour = 11; // Time of the running simulation (hours)
		referenceNodeId= "77813703#1";
		insideFlowRatio = 0.4;
	}
	
	public void readInput() {
		if (baseFolder == null || baseFolder.isEmpty()) {
			System.err.println("Please set the name of a base folder.");
		}
		if (baseName == null || baseName.isEmpty()) {
			System.err.println("Please set the name of a base name for input files.");
		}
		
		logger.info("reading .net.xml file...");
		readNet(baseFolder + baseName + ".net.xml"); 
		
		logger.info("reading .osm.xml files...");
		readZones(baseFolder, baseName, ".osm.xml");
		logger.info("read " + zones.size() + " zones");
		
		logger.info("surface read from osm: ");
		logger.info("residential surface " + sumResidentialSurface);
		logger.info("commercial surface " + sumCommercialSurface);
		logger.info("industrial surface " + sumIndustrialSurface);

		logger.info("reading .area.xml file...");
		readAreas(baseFolder + baseName + ".areas.xml");
		logger.info("added " + areas.size() + " areas.");
		
		logger.info("reading vehicle types from .veh.xml file...");
		readVehicleTypes(baseFolder + baseName + ".veh.xml");	
		logger.info("read " + vtypes.size() + " vehicle types");
		
		logger.info("reading .loop.xml file...");
		readLoops(baseFolder + baseName + ".loop.xml");
		logger.info("read " + loops.size() + " induction loops");	
		
		readGraph(baseFolder + baseName + ".dgs", baseFolder + baseName + ".net.xml");

		logger.info("assigning zones to areas...");
		assignZonesToAreas();
		
		printAreasInfo();
		
		computeZonesProbabilities();
	}
	
	private void printAreasInfo() {
		for (Area area : areas) {
			if (area.getZones() != null) {
				logger.info("zones in area " + area.getZoneType().name() + ": " + area.getZones().size() + " surface: " + area.getSurface());
			}	
		}
		logger.info("zones in def area " + defaultResidentialArea.getZoneType().name()  + ": " + defaultResidentialArea.getZones().size() + ", surface: " + defaultResidentialArea.getSurface());
		logger.info("zones in def area " + defaultCommercialArea.getZoneType().name()  + ": " + defaultCommercialArea.getZones().size() + ", surface: " + defaultCommercialArea.getSurface());
		logger.info("zones in def area " + defaultIndustrialArea.getZoneType().name()  + ": " + defaultIndustrialArea.getZones().size() + ", surface: " + defaultIndustrialArea.getSurface());
	}
	
	/**
	 * Reads information about network from .net.xml file
	 * Sets projection and netOffset
	 */
	private void readNet(String path) {
		NetHandler h = new NetHandler();
		XMLParser.readFile(path, h);
		proj = h.getProj();
		netOffset = h.getNetOffset();
	}
	
	/**
	 * Reads zones from .osm.xml files
	 * Populates zones map (Commercial, Residential, Industrial) with 'Zone' objects.
	 */
	private void readZones(String baseFolder, String baseName, String fileExtension) {
		OSMHandler h = new OSMHandler(proj, netOffset);
		File folder = new File(baseFolder);
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles) {
			if (f.isFile() && f.getName().startsWith(baseName) && f.getName().endsWith(fileExtension)) {
				XMLParser.readFile(f.getPath(), h);
			}
		}
		zones = h.getZones();
		sumResidentialSurface = h.getSumResidentialSurface();
		sumCommercialSurface = h.getSumCommercialSurface();
		sumIndustrialSurface = h.getSumIndustrialSurface();
		
	}

	/**
	 * Reads areas from .areas.xml file.
	 * Reads probabilities of zone types.
	 * Populates areas list with read areas and default areas.
	 */
	private void readAreas(String path) {
		AreasHandler h = new AreasHandler();
		XMLParser.readFile(path, h);
		areas = h.getAreas();
		ZoneType.RESIDENTIAL.setProbability(h.getResidentialTypeProbability());
		ZoneType.COMMERCIAL.setProbability(h.getCommercialTypeProbability());
		ZoneType.INDUSTRIAL.setProbability(h.getIndustrialTypeProbability());
		defaultResidentialArea = new Area(1 / h.getCommercialAreasSumProbability(), ZoneType.RESIDENTIAL);
		defaultCommercialArea = new Area(1 / h.getIndustrialAreasSumProbability(), ZoneType.COMMERCIAL);
		defaultIndustrialArea = new Area(1 / h.getResidentialAreasSumProbability(), ZoneType.INDUSTRIAL);
		// XXX czy default areas musza byc na liscie areas?
		//areas.add(defaultCommercialArea);
		//areas.add(defaultIndustrialArea);
		//areas.add(defaultResidentialArea);
	}
	
	public void setDefaultIndustrialAreaProbability(double defaultIndustrialAreaProbability) {
		this.defaultIndustrialArea.setProbability(defaultIndustrialAreaProbability);
	}
	
	public void setDefaultCommercialAreaProbability(double defaultCommertialAreaProbability) {
		this.defaultCommercialArea.setProbability(defaultCommertialAreaProbability);
	}
	
	public void setDefaultResidentialAreaProbability(double defaultResidentialAreaProbability) {
		this.defaultResidentialArea.setProbability(defaultResidentialAreaProbability);
	}

	/**
	 * Assign to each zone the area (based on euclidean distance)
	 * Populate list of zones for each area
	 */
	private void assignZonesToAreas() {
		// check each point in zone to which area belongs
		for (Zone zone : zones.values()) {
			for (Area area : areas) {
				if (zone.area == null && area.getZoneType() == zone.type) {
					for (Point2D.Double p : zone.points) {
						if (area.getRadius() > MathHelper.euclideanDistance(area.getX(), area.getY(), p.x, p.y)) {
							zone.area = area;
							break;
						}
					}
				}
			}
		}
		// create lists of zones that overlap with area
		for (Zone zone : zones.values()) {
			if (zone.area == null) {
				if (zone.type == ZoneType.COMMERCIAL) {
					zone.area = defaultCommercialArea;
				}
				else if (zone.type == ZoneType.INDUSTRIAL) {
					zone.area = defaultIndustrialArea;
				}
				else if (zone.type == ZoneType.RESIDENTIAL) {
					zone.area = defaultResidentialArea;
				}
			}
			zone.area.addZone(zone);
			zone.area.addSurface(zone.surface);
		}
	}
	
	public void computeZonesProbabilities() {
		for (Zone zone : zones.values()) {
			zone.probability = (zone.surface / zone.area.getSurface()) * zone.type.getProbability() * zone.area.getProbability();
		}
	}
	
	/**
	 * These types are used to generate vehicles, equally distributed.
	 */
	private void readVehicleTypes(String path) {
		VehicleTypesHandler h = new VehicleTypesHandler();
		XMLParser.readFile(path, h);
		vtypes = h.getVtypes();
	}
	
	/**
	 * - baseName.loop.xml files.
	 * - Real data used as input for outer traffic.
	 * - Each real counting loop is linked to an edge (must exist in the .net.xml file)
	 * - For each loop a Loop object is created.
	 * - For each loop, flows are created: one per hour.
	 */
	public void readLoops(String path) {
		LoopHandler h = new LoopHandler(stopHour);
		XMLParser.readFile(path, h);
		loops = h.getLoops();
	}
	
	/**
	 * Initializes graph from dgs file if exists, otherwise reads net.xml file, generate dgs file and initialize graph.
	 */
	private void readGraph(String graphPath, String netPath) {
		graph = new MultiGraph("roadNetwork", false, true);
		File graphFile = new File(graphPath);
		//if (!graphFile.exists()) {
			logger.info("generating the DGS file...");
			SumoNetworkToDGS netParser = new SumoNetworkToDGS(baseFolder, baseName);
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(netParser);
				parser.parse(new InputSource(netPath));
				logger.info("DGS file generated");
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		//}
		try {
			logger.info("reading the DGS file...");
			graph.read(baseFolder + baseName + ".dgs");
			logger.info("graph initialized, nodes: " + graph.getNodeCount() + ", edges: " + graph.getEdgeCount());
		} 
		catch (ElementNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (GraphParseException e) {
			e.printStackTrace();
		}
		checkIfGraphContainInductionLoops();
		checkEdgesContainAttribute("weight");
	}
		
	/*
	 * Checks if induction loops' base edges exist in the graph so that a Dijkstra
	 */
	private void checkIfGraphContainInductionLoops() {
		for (Loop loop : loops) {
			if (graph.getNode(loop.getEdge()) == null) {
				logger.error("Error: Induction loop " + loop.getId() + " from edge " + loop.getEdge() + " is missing in the graph");
			}
		}
	}

	/*
	 * Checks if edges have an attribute for computing shortest path 
	 */
	private void checkEdgesContainAttribute(String attrName) {
		int hasIt = 0;
		for (org.graphstream.graph.Node n : graph.getNodeSet()) {
			if (n.getAttribute(attrName) != null) {
				hasIt++;
			}
		}
		if(hasIt != graph.getNodeCount()) {
			logger.warn(hasIt + " nodes have the \"weight\" attribute over " + graph.getNodeCount());
		}
	}
	
	/**
	 * Computes shortest paths from each source zone to a referenceNode and from referenceNode to each destination zone.
	 * Zones without a connection with the referenceNode are deleted, probabilities are recomputed.
	 */
	public void computeDijkstra() {
		
		referenceDjk = new Dijkstra(Dijkstra.Element.NODE, "referenceDjk","weight");
		referenceDjk.init(graph);
		referenceDjk.setSource(graph.getNode(referenceNodeId));
		referenceDjk.compute();
		
		logger.info("computing paths from each induction loop...");
		for (Loop loop : loops) {
			Dijkstra djk = new Dijkstra(Dijkstra.Element.NODE, loop.getEdge(),"weight");
			djk.init(graph);
			djk.setSource(graph.getNode(loop.getEdge()));
			djk.compute();
			loop.setDijkstra(loop.getEdge());
		}

		ArrayList<String> zonesToRemove = new ArrayList<String>();
		
		logger.info("computing from residential zones to a referenceNode... ");
		zonesToRemove.addAll(checkConnectivityOfSourceZones(ZoneType.RESIDENTIAL,5));
		
		logger.info("computing path from a random point in each zone to a reference node. This takes a while... %n"); 
		zonesToRemove.addAll(checkConnectvityOfDestinationZones(5));
		
		logger.info("removing isolated zones " + zonesToRemove.size());
		removeZones(zonesToRemove);
		
		logger.info("updating probabilities");
		updateProbabilities();
		

		printAreasInfo();
	}

	/**
	 * Checks if there is a path from a node in zone to the referenceNode
	 * Remembers the sourceNode of the path in zone.sourceNode
	 * @param zoneType type of zone to check
	 * @param maxNodesCount maximum number of trials to pick up a  node from zone, if any of nodes has a path, then zone is considered as isolated
	 * @return list of ids of isolated zones
	 */
	public ArrayList<String> checkConnectivityOfSourceZones(ZoneType zoneType, int maxNodesCount) {
		if (maxNodesCount < 1) {
			maxNodesCount = 5;
		}
		ArrayList<String> zonesToRemove = new ArrayList<String>();
		for (Zone zone : zones.values()) {
			if (zoneType == null || (zoneType != null && zone.type == zoneType)) {
				Node node = null;
				Dijkstra djk = null;
				boolean unreachable=true;
				int limit = 0;
				do {
					if (limit > maxNodesCount) {
						zonesToRemove.add(zone.id);
						break;
					}
					node = zone.getRandomNode(graph.getNodeIterator());
					if (getPathLength(node,	graph.getNode(referenceNodeId)) != Double.POSITIVE_INFINITY) {
						unreachable=false;
					}
					limit++;
				} while (unreachable);
				zone.shortestPath = node.getId();
				zone.sourceNode = node;
				djk = null;
			}
		}
		return zonesToRemove;
	}
	
	/**
	 * Populates zone.near_nodes and computes Dijkstra for each node
	 * Checks if a path from a referenceNode to the each node exists
	 * @param maxNearNodesCount the maximum number of near nodes for each zone
	 * @return Zones that don't have a path from a reference node
	 */
	public ArrayList<String> checkConnectvityOfDestinationZones(int maxNearNodesCount) {
		if (maxNearNodesCount < 1) {
			maxNearNodesCount = 5;
		}
		int pickupRepeatCount = 5;
		ArrayList<String> zonesToRemove = new ArrayList<String>();
		for (Zone zone : zones.values()) {
			for (int i = 0; i < maxNearNodesCount; i++) {
				int times = 0;
				Node node = null;
				do {
					node = zone.getRandomNode(graph.getNodeIterator());
					// test if there is a path from a reference node to this node
					if (referenceDjk.getPathLength(node) == Double.POSITIVE_INFINITY ) {
						node = null;
					}
					times++;
				}  while (node == null && times <= pickupRepeatCount);
				if (node != null) {
					zone.near_nodes.add(node);
				}
			}
			if(zone.near_nodes.size()==0) {
				zonesToRemove.add(zone.id);
			}
		}
		return zonesToRemove;
	}
	
	/**
	 * Updates probabilities after the all zone removing stuff
	 */
	private void updateProbabilities() {
		for(Area area : areas){
			area.setSurface(0);
		}
		defaultCommercialArea.setSurface(0);
		defaultResidentialArea.setSurface(0);
		defaultIndustrialArea.setSurface(0);
		sumResidentialSurface = 0;
		for (Zone zone : zones.values()) {
			zone.area.addSurface(zone.surface);
			if (zone.type == ZoneType.RESIDENTIAL) {
				sumResidentialSurface += zone.surface;
			}
		}
		computeZonesProbabilities();
	}
	
	private void removeZones(ArrayList<String> zonesToRemove) {
		for (String zoneId : zonesToRemove) {
			zones.remove(zoneId);
		}
	}
	
	public ArrayList<Trip> generateSortedTrips() {
		trips = new ArrayList<Trip>();
		random = new Random(System.currentTimeMillis()); // reset seed!
		int currentHour = 0;
		double currentTime = 0;
		int innerCounter = 0;
		int outerCounter = 0;
		Loop loop;
		Flow flow;
		logger.info("generating sorted trips....");
		if (loops.isEmpty()) {
			readLoops(baseFolder + baseName + ".loop.xml");
		}
		while (!loops.isEmpty()) {
		 	String vehicleId = vtypes.get((int)(org.util.Random.next() * vtypes.size())).getId();
		 	Node sourceNode = null;
		 	Trip trip = null;
		 	if (random.nextDouble() < insideFlowRatio) {
		 		// inside flow
				Zone zone = pickUpOneZone(ZoneType.RESIDENTIAL);
				sourceNode = zone.sourceNode;
				innerCounter++;
			} 
			else {
				// outside flow
				loop = loops.pollFirst();
				flow = loop.getFlows().pollFirst();
				currentTime = flow.getTime();
				if (flow.getNextVehicle() == flow.TRUCK && vtypes.size() > 5) {
					vehicleId = vtypes.get(5).getId();
				}
				if (flow.next()) {
					outerCounter++;
					sourceNode = graph.getNode(loop.getEdge());
					loop.addFlow(flow);
					// logger:
					if (currentHour != flow.getHour()) {
					     if (currentHour != 0) {
					    	 logger.info("flows for hour " + currentHour + " (innerTraffic: " + innerCounter + ", outerTraffic: " + outerCounter + ", sum: " + (innerCounter + outerCounter)+ ")");	  	
					     }
					     currentHour = flow.getHour();
					}
				}
				if (loop.hasFlow()) {
					loops.add(loop);
				}
			}
		 	trip = generateTrip(sourceNode, (int)currentTime, vehicleId, (outerCounter + innerCounter));
		 	if (trip != null && currentTime < stopTime) {
		 		trips.add(trip);
		 	}
		}	
		logger.info("flows for hour " + currentHour + " (innerTraffic: " + innerCounter + ", outerTraffic: " + outerCounter + ", sum: " + (innerCounter + outerCounter)+ ")");	
		return trips;
	}
	
	public ArrayList<Trip> generateTrips() {
		trips = new ArrayList<Trip>();
		//random = new Random(); // reset seed!
		double currentTime = 0;
		int innerCounter = 0;
		int outerCounter = 0;
		for (Loop loop : loops) {
			Node sourceNode = graph.getNode(loop.getEdge());
			for (Flow flow : loop.getFlows()) {
				if (flow.getHour() <= stopHour) {
					for (int i = 0; i < flow.getVehicles(); i++) {
						currentTime = flow.getTime();
						String vehicleType = vtypes.get((int)(org.util.Random.next() * vtypes.size())).getId();
						Trip trip = generateTrip(sourceNode, (int)currentTime, vehicleType, (outerCounter + innerCounter));
						if (trip != null) {
							outerCounter++;
							trips.add(trip);
						}
						// may generate additionally an inside flow
						if (random.nextDouble() < insideFlowRatio) {
							Zone zone = pickUpOneZone(ZoneType.RESIDENTIAL);
							sourceNode = zone.sourceNode;
							vehicleType = vtypes.get((int)(org.util.Random.next() * vtypes.size())).getId();
							trip = generateTrip(sourceNode, (int)currentTime, vehicleType, (outerCounter + innerCounter));
							if (trip != null) {
								innerCounter++;
								trips.add(trip);
							}
						}
						flow.next();
					}
				}
				flow.resetTime();
			}
		}
		logger.info("generated trips: " + " inner: " + innerCounter + ", outer: " + outerCounter + ", sum: " + (innerCounter+outerCounter));
		return trips;
	}
		
	private Trip generateTrip(Node sourceNode, int currentTime, String vehicleType, int vehicleCounter) {
		Trip trip = null;
		if (sourceNode == null) {
			return trip;
		}
		Zone zone = pickUpOneZone(null);
		if (zone == null) {
			logger.warn("zone null!");
			zone = pickUpOneZone(null);
		}
		Node destinationNode = zone.getDestinationNode();
		if (destinationNode == null) {
			//logger.warn("Initialize and compute dijkstra first. There is no path from edge " + sourceNode.getId() + "to a random node ");
			return trip;
		}
		trip = new Trip("_h" + (currentTime/3600) + "_" + vehicleCounter);
		trip.setSourceId(sourceNode.getId());
		trip.setDestinationId(destinationNode.getId());
		trip.setDestinationZoneType(zone.type);
		Path path = getPath(sourceNode, destinationNode);
		trip.setDepartTime(currentTime);
		trip.setVehicleId(vehicleType);
		trip.setRoute(path);
		return trip;
	}
	
	private Zone pickUpOneZone(ZoneType zoneType) {
		Zone zone = null;
		int maxTrials = 10;
		int trials = 0;
		while (zone == null && (trials++ < maxTrials)) {
			double rand = random.nextDouble();
			double sum = 0.0;
			for (Zone z : zones.values()) {
				if (zoneType != null && z.type == zoneType) {
					sum += z.surface;
					if (sum > (rand * sumResidentialSurface)) {
						zone = z;
						break;
					}
				}
				else if (zoneType == null) {
					sum += z.probability;
					if (sum > rand) { // select a zone based on its proba
						zone = z;
						break;
					}
				}
			}
		}
		return zone;
	}
	
	
	public Path getPath(String sourceNodeId, String destinationNodeId) {
		return getPath(graph.getNode(sourceNodeId), graph.getNode(destinationNodeId));
	}
	
	/**
	 * @param djk
	 * @param sourceNode
	 * @return
	 */
	public Path getPath(Node sourceNode, Node destinationNode) {
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, sourceNode.getId(), "weight");
		dijkstra.setSource(sourceNode);	
		Path path = null;
		try { 
			path = dijkstra.getPath(destinationNode);
		}
		catch (Exception e) {
			dijkstra.init(graph);
			dijkstra.compute();
			path = dijkstra.getPath(destinationNode);
		}
		if (path.empty()) {
			return null;
		} else {
			return path;
		}
	}
	
	public double getPathLength(Node sourceNode, Node destinationNode) {
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, sourceNode.getId(), "weight");
		dijkstra.setSource(sourceNode);	
		double length = Double.POSITIVE_INFINITY;
		try { 
			length = dijkstra.getPathLength(destinationNode);
		}
		catch (Exception e) {
			dijkstra.init(graph);
			dijkstra.compute();
			length = dijkstra.getPathLength(destinationNode);
		}
		return length;
	}

	public ArrayList<Node> getNodes(ArrayList<String> edgeIds) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (String edgeId : edgeIds) {
			Node node = graph.getNode(edgeId.trim());
			nodes.add(node);
		}
		return nodes;
	}
	
}
