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
import org.graphstream.graph.Edge;
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
			
		rg.printTripInfo();
		
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
	long randomSeed = 123456L;
	
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
		
		printAreasInfo();
		
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
		System.out.println("area type \t zones count \t surface \t probability");
		for (Area area : areas) {
			if (area.getZones() != null) {
				System.out.println(area.getZoneType().name() + " \t " + area.getZones().size() + " \t " + area.getSurface() + " \t " + area.getProbability());
			}	
		}
		System.out.println(defaultResidentialArea.getZoneType().name() + " \t " + defaultResidentialArea.getZones().size() + " \t " + defaultResidentialArea.getSurface() + " \t " + defaultResidentialArea.getProbability());
		System.out.println(defaultCommercialArea.getZoneType().name() + " \t " + defaultCommercialArea.getZones().size() + " \t " + defaultCommercialArea.getSurface() + " \t " + defaultCommercialArea.getProbability());
		System.out.println(defaultIndustrialArea.getZoneType().name() + " \t " + defaultIndustrialArea.getZones().size() + " \t " + defaultIndustrialArea.getSurface() + " \t " + defaultIndustrialArea.getProbability());
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
		double prob = h.getResidentialAreasSumProbability();
		defaultResidentialArea = new Area(1 - h.getResidentialAreasSumProbability(), ZoneType.RESIDENTIAL);
		defaultCommercialArea = new Area(1 - h.getCommercialAreasSumProbability(), ZoneType.COMMERCIAL);
		defaultIndustrialArea = new Area(1 - h.getIndustrialAreasSumProbability(), ZoneType.INDUSTRIAL);
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
				logger.info("total length of edges: " + netParser.getTotalLength());
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
			for (Area area : areas) {
				area.removeZone(zoneId);
			}
			zones.remove(zoneId);
		}
	}
	

	long[] inner = new long[stopHour];
	long[] outer = new long[stopHour];
	
	public ArrayList<Trip> generateSortedTrips() {
		trips = new ArrayList<Trip>();
		random = new Random(System.currentTimeMillis()); // reset seed!
		int currentHour = 1;
		int flowHour;
		double currentTime = 1;
		boolean isInner = false;
		inner = new long[stopHour];
		outer = new long[stopHour];
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
		 	isInner = false;
		 	if (random.nextDouble() < insideFlowRatio) {
		 		// inside flow
		 		isInner = true;
				Zone zone = pickUpOneZone(ZoneType.RESIDENTIAL);
				sourceNode = zone.sourceNode;
			} 
			else {
				// outside flow
				loop = loops.pollFirst();
				flow = loop.getFlows().pollFirst();
				if (currentHour != flow.getHour()) {
				     currentHour = flow.getHour();
				}
				currentTime = flow.getTime();
				if (flow.getNextVehicle() == flow.TRUCK && vtypes.size() > 5) {
					vehicleId = vtypes.get(5).getId();
				}
				if (flow.next()) {
					sourceNode = graph.getNode(loop.getEdge());
					loop.addFlow(flow);
				}
				if (loop.hasFlow()) {
					loops.add(loop);
				}
			}
		 	
		 	if (currentTime < stopTime) {
		 		if (isInner) {
		 			inner[currentHour-1]++;
		 		}
		 		else {
		 			outer[currentHour-1]++;
		 		}
		 		trip = generateTrip(sourceNode, (int)currentTime, vehicleId, (inner[currentHour-1] + outer[currentHour-1]));
		 		if (trip != null) {
		 			trips.add(trip);
		 		}
		 	}
		}	
		return trips;
	}
	
	public ArrayList<Trip> generateTrips() {
		trips = new ArrayList<Trip>();
		random = new Random(randomSeed); // reset seed!
		double currentTime = 0;
		inner = new long[stopHour];
		outer = new long[stopHour];
		int currentHour = 0;
		for (Loop loop : loops) {
			Node sourceNode = graph.getNode(loop.getEdge());
			for (Flow flow : loop.getFlows()) {
				if (flow.getHour() <= stopHour) {
					currentHour = flow.getHour()-1;
					for (int i = 0; i < flow.getVehicles(); i++) {
						currentTime = flow.getTime();
						if (currentTime < stopTime) {
							String vehicleType = vtypes.get((int)(org.util.Random.next() * vtypes.size())).getId();
							Trip trip = generateTrip(sourceNode, (int)currentTime, vehicleType, (outer[currentHour] + inner[currentHour]));
							if (trip != null) {
								outer[currentHour]++;
								trips.add(trip);
							}
							// may generate additionally an inside flow
							if (random.nextDouble() < insideFlowRatio) {
								Zone zone = pickUpOneZone(ZoneType.RESIDENTIAL);
								sourceNode = zone.sourceNode;
								vehicleType = vtypes.get((int)(org.util.Random.next() * vtypes.size())).getId();
								trip = generateTrip(sourceNode, (int)currentTime, vehicleType, (outer[currentHour] + inner[currentHour]));
								if (trip != null) {
									inner[currentHour]++;
									trips.add(trip);
								}
							}
						}
						flow.next();
					}
				}
				flow.resetTime();
			}
		}
		return trips;
	}

	public void printTripInfo() {
		long[] res = new long[stopHour];
		long[] com = new long[stopHour];
		long[] ind = new long[stopHour];
		
		for (Trip trip : trips) {
			int hour = (int)trip.getDepartTime()/3600;
			if (trip.getDestinationZoneType().equals(ZoneType.RESIDENTIAL)) {
				res[hour]++;
			}
			else if (trip.getDestinationZoneType().equals(ZoneType.COMMERCIAL)) {
				com[hour]++;
			}
			else if (trip.getDestinationZoneType().equals(ZoneType.INDUSTRIAL)) {
				ind[hour]++;
			}
		}
		
		System.out.println("hour \t inner \t outer \t residential \t commercial \t industrial \t sum");
		for (int i = 0; i < stopHour; ++i) {
			System.out.println(i + " \t " + inner[i] + " \t " + outer[i] + " \t " + res[i] + " \t " + com[i] + " \t " + ind[i]);
		}	
	}

	
	private Trip generateTrip(Node sourceNode, int currentTime, String vehicleType, long vehicleCounter) {
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
	
	
//	public double evaluate(Individual ind) {
//
//		random = new Random(randomSeed); // reset seed!
//
//		//Prints the individual 
//		String individual = "Individual:";
//         for(int i=0; i<ind.getLength();i++) {
//        	 individual += " " + ind.getAllele(i);
//         }
//         System.out.println(individual);
//
//
//		ZoneType.RESIDENTIAL.probability = (Double)ind.getAllele(0)/100;
//		ZoneType.INDUSTRIAL.probability = (Double)ind.getAllele(1)/100;
//		ZoneType.COMMERCIAL.probability = (Double)ind.getAllele(2)/100;
//
//		//Fills in the different Commercial areas probabilities
//		// Zc1/Zc2/Zc3/
//		for (int i = 3; i < 6; i++) {
//			areas.get(i - 3).probability = (Double)ind.getAllele(i)/100;
//		}
//
//		//Fills in the default Commercial area probability
//		//Zcd/
//		defaultAreaCOM.probability = (Double)ind.getAllele(6)/100;
//
//		//Fills in the Industrial area probability
//		//Zi1
//		areas.get(3).probability = (Double)ind.getAllele(7)/100;
//
//		//Fills in the default Industrial area probability
//		//Zid
//		defaultAreaIND.probability = (Double)ind.getAllele(8)/100;
//
//		//Fills in the Residentia area probability
//		//Zr1
//		areas.get(4).probability = (Double)ind.getAllele(9)/100;
//
//		//Fills in the default Residential area probability
//		//Zrd
//		defaultAreaRES.probability = (Double)ind.getAllele(10)/100;
//
//		//Fills in the insideFlowRatio and ShiftingRatio
//		//IR/SR
//		insideFlowRatio = (Double)ind.getAllele(11)/100;
//		shiftingRatio = (Double)ind.getAllele(12)/100;
//
//		return doEvaluate();
//	}
//
//	private double doEvaluate() {
//		long start = System.currentTimeMillis();
//		double fitness = 0;		
//		// recompute probabilities !!
//		for (Zone z : zones.values()) {
//
//			z.probability = (z.surface / z.area.sumSurfaceZones)
//					* z.type.probability * z.area.probability; 
//		}
//		for(Detector d : currentSolution.values()){
//			d.reset();
//
//			//Sets the shiftingRatio for each Detector loop
//			d.setShiftingRatio(shiftingRatio);
//		}
//		flowGeneration();
//		//Applies shiftingRatio for each control point
//		for(Detector d : currentSolution.values()){
//			//Prints counted traffic in control point BEFORE shift
//			//System.out.println(" Before Shift: " + d);
//			//Applies Shifting Ratio
//			d.shift();
//			//Prints counted traffic in control point AFTER shift
//			//System.out.println(" After Shift: " + d);
//		}
//		fitness = evaluator.compareTo(currentSolution);
//
//		System.out.printf("%.1f s%n",(System.currentTimeMillis()-start)/1000.0);
//		return fitness;
//	}
	
//	public String[] getParametersNames() {
//
//		ArrayList<String> paramsNames = new ArrayList<String>();
//
//		// Zone types
//		paramsNames.add("Residential Type");
//		paramsNames.add("Industrial Type");
//		paramsNames.add("Commercial Type");
//
//		// inner traffic ratio
//		paramsNames.add("Inner Traffic");
//
//		// loop through areas
//		for (Area a : areas) {
//			switch(a.type){
//			case COMMERCIAL: paramsNames.add("COM(" + a.id + ")");break;
//			case INDUSTRIAL: paramsNames.add("IND(" + a.id + ")");break;
//			case RESIDENTIAL: paramsNames.add("RES(" + a.id + ")");break;
//			}
//		}
//
//		String[] strings = new String[paramsNames.size()];
//		return paramsNames.toArray(strings);
//	}
//
//	public double[][] getParametersBoundaries() {
//
//		double[] min = new double[4 + areas.size()];
//		double[] max = new double[4 + areas.size()];
//
//		min[0] = 1.0;
//		min[1] = 1.0;
//		min[2] = 1.0;
//
//		min[3] = 0.3;
//
//		max[0] = 100.0;
//		max[1] = 100.0;
//		max[2] = 100.0;
//
//		max[3] = 0.7;
//
//		for (int i = 0; i < areas.size(); i++) {
//			min[4 + i] = 1.0;
//			max[4 + i] = 10.0;
//		}
//
//		double bounds[][] = new double[2][min.length];
//		bounds[0] = min;
//		bounds[1] = max;
//
//		return bounds;
//	}

	
}
