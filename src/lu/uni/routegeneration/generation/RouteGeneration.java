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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import jcell.Individual;

import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.helpers.AreasHandler;
import lu.uni.routegeneration.helpers.LoopHandler;
import lu.uni.routegeneration.helpers.NetHandler;
import lu.uni.routegeneration.helpers.OSMHandler;
import lu.uni.routegeneration.helpers.TextFileParser;
import lu.uni.routegeneration.helpers.VehicleTypesHandler;
import lu.uni.routegeneration.helpers.XMLParser;
import lu.uni.routegeneration.ui.EditorListener;
import lu.uni.routegeneration.ui.EditorPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphParseException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.jhlabs.map.proj.Projection;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;

/**
 * Main class that handles all the process of creating mobility traces
 */
public class RouteGeneration {

	// Define a static logger variable so that it references the Logger instance named "RouteGeneration".
	static Logger logger = Logger.getLogger(RouteGeneration.class);
	   
	public static void main(String[] args) {
		
		// Set up a simple configuration that logs on the console.
	    BasicConfigurator.configure();
	    //logger.setLevel(Level.WARN);
	    
	    String baseFolder = "./test/Luxembourg/";
	    String baseName = "Luxembourg";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		String outputFolder = dateFormat.format(Calendar.getInstance().getTime()) + "/";
	    		
		RouteGeneration rg = new RouteGeneration(baseFolder, baseName);
		
		//rg.displayGraph();
		
		rg.readIsolatedSourceNodes("./test/Luxembourg/isolatedNodes.txt");
		rg.generateSumoFlows(baseFolder + outputFolder + baseName + ".flows.xml", baseFolder + outputFolder);
		rg.writeRoutesFromFlows(baseFolder + outputFolder + baseName + ".flows.xml", baseFolder + outputFolder + baseName + ".rou.xml");
		rg.writeIsolatedSourceNodes("./test/Luxembourg/isolatedNodes.txt");
		
	}
	
	// ----------- PARAMETERS ----------
	
	private String baseName = "Luxembourg"; // Project name. Is assumed to be the base name of all configuration files (ex. MyProject.rou.xml, MyProject.net.xml)
	private String baseFolder = "./test/Luxembourg/"; // Path that to the folder containing configuration files.
	
	private Random random;
	private int stopHour = 11; // Time of the running simulation (hours)
	private int stopTime = stopHour * 3600;
	private int currentHour = 0;
	private double currentTime = 0;
	private Point2D.Double netOffset;	
	private Projection proj;
	private HashMap<String, Zone> zones;
	private ArrayList<Area> areas;
	private ArrayList<VType> vtypes;
	private TreeSet<Loop> loops;
	private Loop nextLoop;
	private double insideFlowRatio = 0.4;
	private Flow nextFlow;
	private Graph graph;
	private ArrayList<Node> sourceNodes;
	private ArrayList<Node> destinationNodes;
	
	private List<String> isolatedSourceNodes = new ArrayList<String>();
	
	private double sumResidentialSurface = 0.0;
	private Area defaultIndustrialArea;
	private Area defaultCommercialArea;
	private Area defaultResidentialArea;
	
	private EditorPanel editorPanel;
	
	// ----------- Getters & Setters ----------

	/**
	 * @return the list of vehicle types
	 */
	public ArrayList<VType> getVTypes() { 
		return vtypes; 
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

	// ----------- Cunstructors  ----------
	public RouteGeneration() {
		initialize();
	}
	
	public RouteGeneration(String baseFolder, String baseName) {
		this.baseFolder = baseFolder;
		this.baseName = baseName;
		initialize();
	}
	
	private void initialize() {
		random = new Random(System.currentTimeMillis());
		
		readNet(baseFolder + baseName + ".net.xml"); 
		readZones(baseFolder, baseName, ".osm.xml");
		readAreas(baseFolder + baseName + ".areas.xml");
		readVehicleTypes(baseFolder + baseName + ".veh.xml");	
		readLoops(baseFolder + baseName + ".loop.xml");
		
		assignZonesToAreas();
		computeZonesProbabilities();
		
		initializeGraph();
		
	}
	
	public void displayGraph() {

		double min_x_boundary = Double.MAX_VALUE;
		double min_y_boundary = Double.MAX_VALUE;
		double max_x_boundary = Double.MIN_VALUE;
		double max_y_boundary = Double.MIN_VALUE;
		for (Zone z : zones.values()) {
			if (z.min_x_boundary < min_x_boundary)
				min_x_boundary = z.min_x_boundary;
			if (z.max_x_boundary > max_x_boundary)
				max_x_boundary = z.max_x_boundary;
			if (z.min_y_boundary < min_y_boundary)
				min_y_boundary = z.min_y_boundary;
			if (z.max_y_boundary > max_y_boundary)
				max_y_boundary = z.max_y_boundary;
		}
		
		editorPanel = new EditorPanel(min_x_boundary, min_y_boundary, max_x_boundary, max_y_boundary);
		editorPanel.setBackground(Color.white);
		editorPanel.setZones(zones);
		//editorPanel.setAreas(areas);

		EditorListener ae = new EditorListener(editorPanel);
		ae.run();
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}		
	}
	
	/**
	 * Reads information about network from .net.xml file
	 * Sets projection and netOffset
	 */
	private void readNet(String path) {
		NetHandler h = new NetHandler();
		logger.info("reading .net.xml file...");
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
		logger.info("reading .osm.xml files...");
		for (File f : listOfFiles) {
			if (f.isFile() && f.getName().startsWith(baseName) && f.getName().endsWith(fileExtension)) {
				XMLParser.readFile(f.getPath(), h);
			}
		}
		zones = h.getZones();
		sumResidentialSurface = h.getSumResidentialSurface();
		logger.info("read " + zones.size() + " zones");
	}

	/**
	 * Reads areas from .areas.xml file.
	 * Reads probabilities of zone types.
	 * Populates areas list with read areas and default areas.
	 */
	private void readAreas(String path) {
		logger.info("reading .area.xml file...");
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
		logger.info("added " + areas.size() + " areas.");
	}
	
	/**
	 * Assign to each zone the area (based on euclidean distance)
	 * Populate list of zones for each area
	 */
	private void assignZonesToAreas() {
		logger.info("assigning zones to areas...");
		// check each point in zone to which area belongs
		for (Zone zone : zones.values()) {
			for (Area area : areas) {
				if (zone.area == null && area.getZoneType() == zone.type) {
					for (Point2D.Double p : zone.points) {
						if (area.getRadius() > euclideanDistance(area.getX(), area.getY(), p.x, p.y)) {
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
	
	private void computeZonesProbabilities() {
		for (Zone zone : zones.values()) {
			zone.probability = (zone.surface / zone.area.getSurface()) * zone.type.getProbability() * zone.area.getProbability();
		}
	}
	
	/**
	 * These types are used to generate vehicles, equally distributed.
	 */
	private void readVehicleTypes(String path) {
		logger.info("reading vehicle types from .veh.xml file...");
		VehicleTypesHandler h = new VehicleTypesHandler();
		XMLParser.readFile(path, h);
		vtypes = h.getVtypes();
		logger.info("read " + vtypes.size() + " vehicle types");
	}
	
	/**
	 * - baseName.loop.xml files.
	 * - Real data used as input for outer traffic.
	 * - Each real counting loop is linked to an edge (must exist in the .net.xml file)
	 * - For each loop a Loop object is created.
	 * - For each loop, flows are created: one per hour.
	 */
	private void readLoops(String path) {
		LoopHandler h = new LoopHandler(stopHour);
		logger.info("reading .loop.xml file...");
		XMLParser.readFile(path, h);
		loops = h.getLoops();
		logger.info("read " + loops.size() + " induction loops");	
		for (Loop loop : loops) {
			logger.info("loop " + loop.getId() + ": " + loop.getTotalFlow());
		}
	}
	
	/**
	 * Initializes graph from dgs file if exists, otherwise reads net.xml file, generate dgs file and initialize graph.
	 */
	private void initializeGraph() {
		logger.info("initializing graph...");
		graph = new MultiGraph("roadNetwork", false, true);
		File graphFile = new File(baseFolder + baseName + ".dgs");
		if (!graphFile.exists()) {
			logger.info("generating the DGS file...");
			SumoNetworkToDGS netParser = new SumoNetworkToDGS(baseFolder, baseName);
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(netParser);
				parser.parse(new InputSource(baseFolder + baseName + ".net.xml"));
				logger.info("DGS file generated");
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
		try {
			logger.info("loading the DGS file...");
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
				logger.error("Error: Induction loop from edge " + loop.getEdge() + " is missing in the graph");
			}
		}
	}

	/*
	 * Checks if edges have an attribute for computing shortest path 
	 */
	private void checkEdgesContainAttribute(String attrName) {
		int hasIt = 0;
		for (org.graphstream.graph.Node n : graph.getNodeSet()) {
			if (n.getAttribute(attrName) != null)
				hasIt++;
		}
		if(hasIt != graph.getNodeCount()) {
			logger.warn(hasIt + " nodes have the \"weight\" attribute over " + graph.getNodeCount());
		}
	}
	
	public Path createRandomPath(String djk, Node source) {
		Path p = getShortestPath(djk, source, pickUpOneDestination());
		if (p.empty()) {
			return null;
		} else {
			return p;
		}
	}
	
	/**
	 * @param djk
	 * @param source
	 * @param pickUpOneDestination
	 * @return
	 */
	private Path getShortestPath(String djk, Node source, Node target) {
		Dijkstra dummyDjk = new Dijkstra(Dijkstra.Element.EDGE,djk,"weight");
		dummyDjk.setSource(source);
		// NOTE requires that dijkstra was previously computed !
		return dummyDjk.getPath(target);
	}
	
	private Zone pickUpOneZone(ZoneType zoneType) {
		Zone zone = null;
		// XXX there should be always a zone selected but just in case there is an exit condition
		int maxTrials = 5;
		int trials = 0;
		while (zone == null && (maxTrials == -1 || trials++ < maxTrials)) {
			//random = new Random(System.currentTimeMillis());
			double rand = random.nextDouble();
			//logger.info("pick up zone, random: " + rand);
			double sum = 0.0;
			for (Zone z : zones.values()) {
				if (zoneType == null || (zoneType != null && z.type == zoneType)) {
					sum += z.surface;
					if (sum > (rand * sumResidentialSurface)) {
						zone = z;
						break;
					}
				}
			}
		}
		return zone;
	}
	
	/**
	 * @return a random node from previously remembered near_nodes in random zone. 
	 * Nodes in zone are restricted to near_nodes - computed while Dijsktra (max 5 in each zone)
	 */
	private Node pickUpOneDestination() {
		// select a zone based on its proba
		Zone zone = pickUpOneZone(null);
		//random = new Random(randomSeed);
		int randNode = (int) (random.nextDouble() * zone.near_nodes.size());
		return zone.near_nodes.get(randNode);
	}

	/**
	 * @param zone
	 * @return a random noede by a random point(x,y) in the zone
	 */
	private Node nodeInZone(Zone zone) {
		Point2D.Double point = pointInZone(zone);
		return getClosestNode(point);
	}
	
	private Point2D.Double pointInZone(Zone zone) {
		Point2D.Double point = new Point2D.Double();
		//random = new Random(randomSeed);
		do {
			point.x = random.nextDouble()
					* (zone.max_x_boundary - zone.min_x_boundary)
					+ zone.min_x_boundary;
			point.y = random.nextDouble()
					* (zone.max_y_boundary - zone.min_y_boundary)
					+ zone.min_y_boundary;
		} while (!isIn(point, zone));
		return point;
	}
	
	private boolean isIn(Point2D.Double point, Zone zone) {
		Point2D.Double other = new Point2D.Double(zone.max_x_boundary, point.y);
		int n = 0;
		for (int i = 0; i < zone.points.size() - 1; i++) {
			if (intersect(point, other, zone.points.get(i), zone.points.get(i + 1))) {
				n++;
			}
		}
		return n % 2 == 1;
	}

	private boolean intersect(Point2D.Double A, Point2D.Double B, Point2D.Double C, Point2D.Double D) {
		return ccw(A, C, D) != ccw(B, C, D) && ccw(A, B, C) != ccw(A, B, D);
	}
	
	private boolean ccw(Point2D.Double A, Point2D.Double B, Point2D.Double C) {
		return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
	}

	private double euclideanDistance(double x, double y, double x2, double y2) {
		return Math.sqrt(Math.pow((x - x2), 2) + Math.pow((y - y2), 2));
	}

	private Node getClosestNode(Point2D.Double p) {
		Iterator<? extends Node> it = graph.getNodeIterator();
		Node closestNode = it.next();
		double closestX = (Double) closestNode.getAttribute("x");
		double closestY = (Double) closestNode.getAttribute("y");
		double closestDist = Math.sqrt(Math.pow(closestX - p.x, 2.0) + Math.pow(closestY - p.y, 2.0));
		while (it.hasNext()) {
			Node currentNode = it.next();
			if (currentNode.getDegree() > 0) {
				double currentX = (Double) currentNode.getAttribute("x");
				double currentY = (Double) currentNode.getAttribute("y");
				double currentDist = Math.sqrt(Math.pow(currentX - p.x, 2.0) + Math.pow(currentY - p.y, 2.0));
				if (currentDist <= closestDist) {
					closestNode = currentNode;
					closestDist = currentDist;
				}
			}
		}
		return closestNode;
	}
	

	public static String pathToString(Path path) {
		StringBuilder sb = new StringBuilder();
		List<Node> l = path.getNodePath();
		for (int i = 0; i < l.size() ; i++) {
			l.get(i).addAttribute("ui.class", "path");
			sb.append(l.get(i).getId());
			if (i < l.size()-1) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
	// SUMO flows generation
	
	private void writeRoutesFromFlows(String flowsPath, String rouPath) {
		logger.info("__Starting writing SUMO routes.");
		
		try {
			StreamResult sr = new StreamResult(rouPath);
			final TransformerHandler tfh = XMLParser.xmlMain(sr);
			final AttributesImpl ai = new AttributesImpl();
			tfh.startElement("", "", "routes", ai);
			XMLParser.writeVTypes(vtypes, tfh, ai);
			
			DefaultHandler h = new DefaultHandler() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					super.startElement(uri, localName, qName, attributes);
					if (qName.equals("flow")) {
						String id = attributes.getValue("id");
						if (id == null) {
							return;
						}
						String from = attributes.getValue("from");
						String to = attributes.getValue("to");
						int begin = Integer.parseInt(attributes.getValue("begin"));
						//String end = attributes.getValue("end");					
						String type = attributes.getValue("type");
						//String number = attributes.getValue("number);
						String route = getRoute(from, to);
						XMLParser.writeRoute(id, type, begin, route, tfh, ai);
					}
				}
			};
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(h);
				logger.info("reading flows from .flows.xml file and writing rou.xml...");
				parser.parse(new InputSource(flowsPath));
				
				tfh.endElement("", "", "routes");
				logger.info("Done routes.");
				tfh.endDocument();
				
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getRoute(String from, String to) {
		if (from == null || to == null) {
			return null;
		}
		String route = null;
		Dijkstra djk = new Dijkstra(Dijkstra.Element.NODE, from, "weight");
		djk.init(graph);
		Node nodeFrom = graph.getNode(from);
		Node nodeTo = graph.getNode(to);
		djk.setSource(nodeFrom);	
		try {
			route = pathToString(djk.getPath(nodeTo));
		}
		catch (Exception ex) {
			djk.compute();
			route = pathToString(djk.getPath(nodeTo));
		}
		return route;
	}
	
	public void generateSumoFlows(String path, String outputDirPath) {
		
		logger.info("__Starting Sumo flows generation.");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
		
		try {
			File file = new File(outputDirPath);
			if (!file.exists()) {
				File dir = new File(outputDirPath);  
				dir.mkdir();
			}
			StreamResult sr = new StreamResult(path);
			TransformerHandler tfh = XMLParser.xmlMain(sr);
			AttributesImpl ai = new AttributesImpl();
			tfh.startElement("", "", "flows", ai);
			
			int vehicleCounter = 0;
			int innerTrafficCounter = 0;
			int outerTrafficCounter = 0;
			
			sourceNodes = new ArrayList<Node>();
			destinationNodes = new ArrayList<Node>();
			
			while (!loops.isEmpty()) {
				String vehicleId = vtypes.get((int)(org.util.Random.next() * vtypes.size())).getId();
				Node sourceNode = null;
				Node destinationNode = null;
				// inside flow OR outside flow?
				if (random.nextDouble() < insideFlowRatio) {
					// inside flow
					sourceNode = getSourceNode(ZoneType.RESIDENTIAL, 5, 5);
					if (sourceNode == null) {
						logger.warn("Any random residential node was chosen");
						continue;
					}
					destinationNode = getDestinationNode(sourceNode, 5, 5, null, false);
					if (destinationNode == null) {
						logger.warn("Inside flow. There is no path from loop edge " + sourceNode.getId() + "to a random node ");
						continue;
					}		
					vehicleCounter++;
					innerTrafficCounter++;
					String id = "_h" + currentHour + "_c" + vehicleCounter;
					XMLParser.writeFlow(id, sourceNode.getId(), destinationNode.getId(), (int)currentTime, stopTime, vehicleId, vehicleCounter, tfh, ai); 
				} 
				else {
					// outside flow
					nextLoop = loops.pollFirst();
					nextFlow = nextLoop.getAndRemoveNextFlow();
					currentTime = nextFlow.getTime();
					if (nextFlow.next()) {
						sourceNode = graph.getNode(nextLoop.getEdge());
						destinationNode = getDestinationNode(sourceNode, 10, 10, null, false);
						if (destinationNode == null) {
							logger.warn("Outside flow. There is no path from loop edge " + sourceNode.getId() + "to a random node ");
						}
						else {
							if (currentHour != nextFlow.getHour()) {
								logger.info("  _writing flows for hour " + currentHour + " (innerTraffic: " + innerTrafficCounter + ", outerTraffic: " + outerTrafficCounter + ")");
								currentHour = nextFlow.getHour();
								String name = dateFormat.format(Calendar.getInstance().getTime()) + ".pdf";
								//editorPanel.setSources(sourceNodes);
							    //editorPanel.generateSnapshot(outputDirPath + name);	
							    sourceNodes.clear();
							}
							vehicleCounter++;
							outerTrafficCounter++;
							String id = "_h" + currentHour + "_c" + vehicleCounter;
							XMLParser.writeFlow(id, sourceNode.getId(), destinationNode.getId(), (int)currentTime, stopTime, vehicleId, vehicleCounter, tfh, ai); 
						}
						nextLoop.addFlow(nextFlow);
						loops.add(nextLoop);
					}
					else {
						nextLoop.removeFlow(nextFlow);
					}
					if (nextLoop.hasFlow()) {
						loops.add(nextLoop);
					}
				}
				if (sourceNode != null && destinationNode != null) {
					sourceNodes.add(sourceNode);
					destinationNodes.add(destinationNode);
					//editorPanel.drawPoint(new Point2D.Double((Double)sourceNode.getAttribute("x"), (Double)sourceNode.getAttribute("y")), Color.magenta);
				}
			}
			tfh.endElement("", "", "flows");
			logger.info("Done flows.");
			tfh.endDocument();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Node getDestinationNode(Node sourceNode, int randomZonesLimit, int randomNodesLimit, Path returnPath, boolean freeMemory) {
		
		Dijkstra djk = new Dijkstra(Dijkstra.Element.NODE, sourceNode.getId(),"weight");
		djk.init(graph);
		djk.setSource(sourceNode);	
		
		Node node = null;
		Zone zone = null;
		int randomZonesCount = 0;
		boolean unreachable = true;
		Path path = null;
		while (zone == null && randomZonesCount < randomZonesLimit && unreachable) {
			zone = pickUpOneZone(null);
			if (zone != null) {
				int randomNodesCount = 0;
				while (unreachable && randomNodesCount < randomNodesLimit) {
					node = nodeInZone(zone);
					if (node != null) {
						try {
							path = djk.getPath(node);
						}
						catch (Exception ex) {
							djk.compute();
							path = djk.getPath(node);
						}
						// checks if there is a path from the node in the zone to the referenceNode
						if (djk.getPathLength(graph.getNode(node.getId())) != Double.POSITIVE_INFINITY) {
							unreachable=false;
						}
					}
					randomNodesCount++;
				}
				if (unreachable) {
					zone = null;
				}
			}
			randomZonesCount++;
		} 
		returnPath = path;
		if (unreachable) {
			node = null;
			path = null;
		}
		if (freeMemory) {
			djk.clear();
		}
		return node;
	}
	
	private Node getSourceNode(ZoneType zoneType, int randomZonesLimit, int randomNodesLimit) {
		Node node = null;
		Zone zone = null;
		int randomZonesCount = 0;
		while (zone == null && randomZonesCount < randomZonesLimit) {
			zone = pickUpOneZone(zoneType);
			if (zone != null) {
				node = null;
				int randomNodesCount = 0;
				while ((node == null && randomNodesCount < randomNodesLimit) || (node!=null && isolatedSourceNodes.contains(node.getId()))) {
					node = nodeInZone(zone);
					randomNodesCount++;
				}
			}
			randomZonesLimit++;
		} 
		return node;
	}
	
	private void readIsolatedSourceNodes(String path) {
		File f = new File(path);
		if(f.exists()) {
			isolatedSourceNodes = TextFileParser.readStringList(path);
			logger.info("read " + isolatedSourceNodes.size() + " isolated source nodes. ");
			//logger.info("removing " + isolatedSourceNodes.size() + "isolated source nodes... ");
			//removeNodes(isolatedSourceNodes);		
			//logger.info("updating probabilities...");
			//updateProbabilities();
		}
		
	}
	
	private void writeIsolatedSourceNodes(String path) {
		logger.info("writing " + isolatedSourceNodes.size() + " isolated source nodes. ");
		TextFileParser.writeStringList(isolatedSourceNodes, path);
	}

	// optimization
	
	public double evaluate(Individual ind) {
		// TODO Implement
		return 0;
	}
	
	public HashMap<String, Detector> getCurrentSolution()
	{
		// TODO Implement
		return null;
	}
	
	public HashMap<String, Detector> getControls()
	{
		// TODO Implement
		return null;
	}

	
}
