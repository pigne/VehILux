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

import it.polito.appeal.traci.SumoTraciConnection;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import jcell.Individual;

import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.evaluation.RealEvaluation;
import lu.uni.routegeneration.ui.AreasEditor;

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
import com.jhlabs.map.proj.ProjectionFactory;

enum ZoneType {
	RESIDENTIAL(0), INDUSTRIAL(0), COMMERCIAL(0);

	double probability;

	ZoneType(double probability) {
		this.probability = probability;
	}

	double getProbability() {
		return probability;
	}
};

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
	    
		RouteGeneration rg = new RouteGeneration();
		
//		ArrayList<String> isolatedZones = new ArrayList<String>();
//		String isolatedZonesFilePath = "./test/Luxembourg/isolatedZones.txt";
//		File f = new File(isolatedZonesFilePath);
//		if(!f.exists()) {
//			isolatedZones = rg.computeDijkstraAndFindIsolatedZones();
//			writeStringList(isolatedZones, isolatedZonesFilePath);
//		}
//		else {
//			isolatedZones = readStringList(isolatedZonesFilePath);
//		}
//		logger.info("removing " + isolatedZones.size() + "isolated zones... ");
//		rg.removeZones(isolatedZones);		
//		logger.info("updating probabilities...");
//		rg.updateProbabilities();
		
//		rg.generateSumoRoutes();
		
		rg.generateSumoFlows();
		rg.writeRoutesFromFlows();
		
		//ReadTravelTime();
		
		/*
		rg.doEvaluate();
		double []  res = rg.evaluator.eachDetectorCompareTo(rg.currentSolution);
		int i=0;
		for(String key : rg.currentSolution.keySet()){
			System.out.printf("%s ",key);
		}
		logger.info();
		for(double d : res){
			System.out.printf("%.0f ",d);
		}
		logger.info();
		//new ApproximativeEvaluation(args);
		*/
	}
	
	// ----------- PARAMETERS ----------
	
	/**
	 * Project name. Is assumed to be the base name of all configuration files (ex. MyProject.rou.xml, MyProject.net.xml)
	 */
	private String baseName = "Luxembourg";
	
	/**
	 * Path that to the folder containing configuration files.
	 */
	private String baseFolder = "./test/Luxembourg/";
	
	private String projParameter = "+proj=utm + zone=31 +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
	
	private  int port=0;
	
	/**
	 * Time of the running simulation (hours)
	 */
	private int stopHour = 1;

	/**
	 * Random seed for the random events
	 */
	private long randomSeed = 123456L;
	
	private Random random;

	private int stopTime = stopHour * 3600;
	private int currentHour = 0;
	private double currentTime = 0;
	
	private Point2D.Double netOffset;	
	private Projection proj;

	private HashMap<String, Point2D.Double> nodes;
	private HashMap<String, Zone> zones;
	private ArrayList<Area> areas;
	private ArrayList<VType> vtypes;
	private TreeSet<Loop> loops;
	
	private double sumResidentialSurface = 0.0;
	private int vehicleCounter = 0;

	private Loop nextLoop;
	private double insideFlowRatio = 0.4;
	private double outsideFlow[];
	private Flow nextFlow;
	private Graph graph;
	
	// Dijkstra
	private String referenceNodeId= "77813703#1";
	private Dijkstra referenceDjk;
	
	// for editor panel
	private ArrayList<Lane> edges;
	private String styleSheet = "graph { padding: 60px; fill-color:#eeeeee;}"
			+ "node { z-index:3; size: 1px; fill-color: #777777; }"
			+ "node.internal{ fill-color: #BB4444; }"
			+ "edge  { fill-color: #404040; size: 1px;}"
			+ "sprite {text-style:bold; text-color: #555555;  fill-color:#eeeeee; }"
			+ "edge.path {fill-color: #ff4040;}";

	// evaluation
	private HashMap<String, Detector> currentSolution;
	private double shiftingRatio;

	private Area defaultAreaIND;
	private Area defaultAreaCOM;
	private Area defaultAreaRES;
	private RealEvaluation evaluator;
	private HashMap<Node, String> realNodes;

	// Xml
	private BufferedReader br;
	private StreamResult sr;
	private TransformerHandler tfh;
	private AttributesImpl ai;
	private AreasEditor ae;
	
	// ----------- Getters & Setters ----------
	
	/**
	 * @return the AttributesImpl
	 */
	public AttributesImpl getAttributeImp() { 
		return ai; 
	}

	/**
	 * @return the vtypes
	 */
	public ArrayList<VType> getVTypes() { 
		return vtypes; 
	}

	/**
	 * @return the TransformerHandler
	 */
	public TransformerHandler getTransformerHandler() {
		return tfh;
	}

	/**
	 * @return the graph
	 */
	public Graph getGraph() {
		return graph;
	}

	public int getStopTime() {
		return stopTime;
	}
	
	/**
	 * @return the edges (for editor panel)
	 */
	public ArrayList<Lane> getEdges() {
		return edges;
	}
	
	public void incrementVehicleCounter() {
		vehicleCounter++;
	}
	
	public HashMap<String, Zone> getZones() {
		return zones;
	}
	
	/**
	 * @return the shiftingRatio
	 */
	public double getShiftingRatio() {
		return shiftingRatio;
	}
	
	/**
	 * @param shiftingRatio the shiftingRatio to set
	 */
	public void setShiftingRatio(double shiftingRatio) {
		this.shiftingRatio = shiftingRatio;
	}
	
	/**
	 * @return the current solution
	 */
	public HashMap<String, Detector> getCurrentSolution()
	{
		return currentSolution;
	}
	
	/**
	 * @return the control values
	 */
	public HashMap<String, Detector> getControls()
	{
		return this.evaluator.controls;
	}

	// ----------- Cunstructor  ----------
	public RouteGeneration() {
		
//		random = new Random(randomSeed);
		random = new Random(System.currentTimeMillis());
		
		edges = new ArrayList<Lane>();
		zones = new HashMap<String, Zone>();
		nodes = new HashMap<String, Point2D.Double>();
		areas = new ArrayList<Area>();
		vtypes = new ArrayList<VType>();
		loops = new TreeSet<Loop>(); 
		
//		destinations = new Vector<Point2D.Double>();
		outsideFlow = new double[stopHour];

		readNet(); 
		readZones();
		readAreas();
		assignZonesToAreas();
		readVehicleTypes();	
		readLoops();
		
		initializeGraph();

//		// XXX : evaluation
//		evaluator = new RealEvaluation();
//		currentSolution = new HashMap<String, Detector>();
//		for(String id : evaluator.controls.keySet()){
//			Detector det = new Detector(stopHour);
//			det.id = id;
//			currentSolution.put(id, det);
//		}
//		realNodes = new HashMap<Node, String>();
//		for (Detector d : evaluator.controls.values()) {
//			realNodes.put(graph.getNode(d.edge), d.id);
//		}
		
	}
	
	/**
	 * Reads information about network from .net.xml file
	 * set projection and netOffset
	 */
	private void readNet() {
		DefaultHandler h = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, qName, attributes);
				if (qName.equals("location")) {
					projParameter = attributes.getValue("projParameter");
					proj = ProjectionFactory.fromPROJ4Specification(projParameter.split(" "));
					String offset = attributes.getValue("netOffset");
					String[] toffset = offset.split(",");
					netOffset = new Point2D.Double();
					netOffset.x = Double.parseDouble(toffset[0]);
					netOffset.y = Double.parseDouble(toffset[1]);
				}
				// NOTE: doesn't read edges for now (it's only used by editor panel)! should be moved to editor panel class
//				if (qName.equals("lane")) {
//					Lane e = new Lane();
//					String shape = attributes.getValue("shape");
//					for (String point : shape.split(" ")) {
//						Point2D.Double p = new Point2D.Double();
//						String[] xy = point.split(",");
//						p.x = Double.parseDouble(xy[0]);
//						p.y = Double.parseDouble(xy[1]);
//						e.shape.add(p);
//					}
//					edges.add(e);
//				}
			}
		};
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
			logger.info("reading .net.xml file...");
			parser.parse(new InputSource(baseFolder + baseName + ".net.xml"));
//			logger.info("read " + edges.size() + " edges");
		} 
		catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	/**
	 * Reads zones from .osm.xml files
	 * - creates zones map (Commercial, Residential, Industrial) with 'Zone' objects.
	 */
	private void readZones() {
		class OSMHandler extends DefaultHandler {
			Zone zone = null;
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, qName, attributes);

				if (qName.equals("node")) {
					double x = Double.parseDouble(attributes.getValue(attributes.getIndex("lon")));
					double y = Double.parseDouble(attributes.getValue(attributes.getIndex("lat")));
					Point2D.Double dest = new Point2D.Double();
					proj.transform(x, y, dest);
					dest.x = dest.x + netOffset.x;
					dest.y = dest.y + netOffset.y;
					nodes.put(attributes.getValue("id"), dest);
				} 
				else if (qName.equals("way")) {
					if (zones.get(attributes.getValue("id")) == null) {
						zone = new Zone();
						zone.id = attributes.getValue("id");
					}
				} 
				else if (qName.equals("nd") && zone != null) {
					zone.points.add(nodes.get(attributes.getValue("ref")));
				} 
				else if (qName.equals("tag") && zone != null) {
					if (attributes.getValue("k").equals("landuse")) {
						String landuse = attributes.getValue("v");
						if (landuse.equals("residential")) {
							zone.type = ZoneType.RESIDENTIAL;
						} 
						else if (landuse.equals("industrial")) {
							zone.type = ZoneType.INDUSTRIAL;
						} 
						else if (landuse.equals("commercial") || landuse.equals("retail")) {
							zone.type = ZoneType.COMMERCIAL;
						}
					} 
					else if (attributes.getValue("k").equals("shop") || attributes.getValue("k").equals("amenity")) {
						zone.type = ZoneType.COMMERCIAL;
					}
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (qName.equals("way") && zone != null) {
					if (zone.type != null) {
						// compute area of the zone
						zone.surface = 0.0;
						for (int i = 0; i < zone.points.size() - 1; i++) {
							zone.surface += zone.points.get(i).x * zone.points.get(i + 1).y
									- zone.points.get(i + 1).x * zone.points.get(i).y; // x0*y1 - x1*y0
						}
						zone.surface = Math.abs(zone.surface / 2.0);
						if (zone.type == ZoneType.RESIDENTIAL) {
							sumResidentialSurface += zone.surface;
						}
						// compute boundaries of the zone
						zone.min_x_boundary = Double.MAX_VALUE;
						zone.min_y_boundary = Double.MAX_VALUE;
						zone.max_x_boundary = Double.MIN_VALUE;
						zone.max_y_boundary = Double.MIN_VALUE;
						for (Point2D.Double p : zone.points) {
							if (p.x < zone.min_x_boundary) {
								zone.min_x_boundary = p.x;
							}
							if (p.x > zone.max_x_boundary) {
								zone.max_x_boundary = p.x;
							}
							if (p.y < zone.min_y_boundary) {
								zone.min_y_boundary = p.y;
							}
							if (p.y > zone.max_y_boundary) {
								zone.max_y_boundary = p.y;
							}
						}
						zones.put(zone.id, zone);
					}
					zone = null;
				}
			}
		};
		
		DefaultHandler h = new OSMHandler();
		File folder = new File(baseFolder);
		File[] listOfFiles = folder.listFiles();
		logger.info("reading .osm.xml files...");
		for (File f : listOfFiles) {
			if (f.isFile() && f.getName().startsWith(baseName) && f.getName().endsWith(".osm.xml")) {
				try {
					XMLReader parser = XMLReaderFactory.createXMLReader();
					parser.setContentHandler(h);
					parser.parse(new InputSource(new FileInputStream(f)));
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		}
		logger.info("read " + zones.size() + " zones, nodes: " + nodes.size());
	}

	/* Reads areas from .areas.xml file
	 * - Areas are stored in the `areas` list.
	 * - Zone types probabilities are set.
	 * <areas residential_proba="5" commercial_proba="80" industrial_proba="15"> 
	 * <area id="1" type="COMMERCIAL" x="20418" y="14500" radius="1500" probability="10"/> 
	 * <area id="2" type="COMMERCIAL" x="22400" y="16700" radius="1500" probability="15"/> 
	 * </areas>
	 */
	private void readAreas() {
		String areasFile = baseFolder + baseName + ".areas.xml";
		defaultAreaRES = new Area();
		defaultAreaRES.probability = 1;
		defaultAreaRES.type = ZoneType.RESIDENTIAL;
		defaultAreaCOM = new Area();
		defaultAreaCOM.probability = 1;
		defaultAreaCOM.type = ZoneType.COMMERCIAL;
		defaultAreaIND = new Area();
		defaultAreaIND.probability = 1;
		defaultAreaIND.type = ZoneType.INDUSTRIAL;
		areas.add(defaultAreaCOM);
		areas.add(defaultAreaIND);
		areas.add(defaultAreaRES);

		File file = new File(areasFile);
		if (file.exists()) {
			class AreasHandler extends DefaultHandler {
				Zone zone = null;
				double sumRES = 0.0;
				double sumCOM = 0.0;
				double sumIND = 0.0;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					super.startElement(uri, localName, qName, attributes);
					if (qName.equals("areas")) {
						double sum = 0.0;
						sum += ZoneType.RESIDENTIAL.probability = Double.parseDouble(attributes.getValue("residential_proba"));
						sum += ZoneType.COMMERCIAL.probability = Double.parseDouble(attributes.getValue("commercial_proba"));
						sum += ZoneType.INDUSTRIAL.probability = Double.parseDouble(attributes.getValue("industrial_proba"));
						ZoneType.RESIDENTIAL.probability /= sum;
						ZoneType.COMMERCIAL.probability /= sum;
						ZoneType.INDUSTRIAL.probability /= sum;
					}
					if (qName.equals("area")) {
						Area a = new Area();
						a.id = attributes.getValue("id");
						a.x = Double.parseDouble(attributes.getValue("x"));
						a.y = Double.parseDouble(attributes.getValue("y"));
						a.radius = Double.parseDouble(attributes.getValue("radius"));
						a.probability = Double.parseDouble(attributes.getValue("probability"));
						String type = attributes.getValue("type");
						if (type.equals("RESIDENTIAL")) {
							a.type = ZoneType.RESIDENTIAL;
							sumRES += a.probability;
						} 
						else if (type.equals("INDUSTRIAL")) {
							a.type = ZoneType.INDUSTRIAL;
							sumIND += a.probability;
						} 
						else if (type.equals("COMMERCIAL")) {
							a.type = ZoneType.COMMERCIAL;
							sumCOM += a.probability;
						}
						areas.add(a);
					}
				}

				@Override
				public void endDocument() throws SAXException {
					// - Set up the probabilities for areas.
					// - Note: the outside of any area has a basic weight of 1.
					sumRES += 1;
					sumCOM += 1;
					sumIND += 1;
					double sr = 0.0, sc = 0.0, si = 0.0;
					//System.out.printf("areas proba... %f %f %f%n", sumCOM, sumIND, sumRES);
					for (Area a : areas) {
						switch (a.type) {
						case COMMERCIAL:
							a.probability /= sumCOM;
							sc += a.probability;
							break;
						case INDUSTRIAL:
							a.probability /= sumIND;
							si += a.probability;
							break;
						case RESIDENTIAL:
							a.probability /= sumRES;
							sr += a.probability;
							break;
						}
					}
					defaultAreaCOM.probability /= sumCOM;
					defaultAreaIND.probability /= sumIND;
					defaultAreaRES.probability /= sumRES;
					//System.out.printf("sum proba areas: %f %f %f%n", sc, si, sr);
				}
			};
			
			DefaultHandler h = new AreasHandler();
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(h);
				logger.info("reading .area.xml file...");
				parser.parse(new InputSource(new FileInputStream(file)));
				logger.info("read " + areas.size() + " areas");
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}
	
	/**
	 * Assign to each zone the area (based on euclidean distance)
	 * Populate list of zones for each area
	 */
	private void assignZonesToAreas() {
		logger.info("assigning zones to areas...");
		// check each point in zone to which area belongs
		for (Zone z : zones.values()) {
			for (Area a : areas) {
				if (z.area == null && a.type == z.type) {
					for (Point2D.Double p : z.points) {
						if (a.radius > euclideanDistance(a.x, a.y, p.x, p.y)) {
							//logger.info(z.id + " in area" + a.id);
							z.area = a;
							break;
						}
					}
				}
			}
		}
		// create lists of zones that overlap with area
		for (Zone z : zones.values()) {
			if (z.area == null) {
				switch (z.type) {
				case COMMERCIAL:
					z.area = defaultAreaCOM;
					z.area.zones.add(z);
					z.area.sumSurfaceZones += z.surface;
					break;
				case INDUSTRIAL:
					z.area = defaultAreaIND;
					z.area.zones.add(z);
					z.area.sumSurfaceZones += z.surface;
					break;
				case RESIDENTIAL:
					z.area = defaultAreaRES;
					z.area.zones.add(z);
					z.area.sumSurfaceZones += z.surface;
					break;
				}
			} else {
				z.area.zones.add(z);
				z.area.sumSurfaceZones += z.surface;
			}
		}
	}
	
	/**
	 * These types are used to generate vehicles, equally distributed.
	 */
	private void readVehicleTypes() {
		DefaultHandler h = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, qName, attributes);
				if (qName.equals("vType")) {
					VType vt = new VType();
					vt.id = attributes.getValue(attributes.getIndex("id"));
					vt.accel = attributes.getValue(attributes.getIndex("accel"));
					vt.color = attributes.getValue(attributes.getIndex("color"));
					vt.decel = attributes.getValue(attributes.getIndex("decel"));
					vt.length = attributes.getValue(attributes.getIndex("length"));					
					vt.minGap = attributes.getValue(attributes.getIndex("minGap"));
					vt.maxSpeed = attributes.getValue(attributes.getIndex("maxSpeed"));
					vt.sigma = attributes.getValue(attributes.getIndex("sigma"));
					vtypes.add(vt);
				}
			}
		};
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
			logger.info("reading vehicle types from .veh.xml file...");
			parser.parse(new InputSource(baseFolder + baseName + ".veh.xml"));
			logger.info("read " + vtypes.size() + " vehicle types");
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	/**
	 * - baseName.loop.xml files.
	 * - Real data used as input for outer traffic.
	 * - Each real counting loop is linked to an edge (must exist in the .net.xml file)
	 * - For each loop a Loop object is created.
	 * - For each loop, flows are created: one per hour.
	 */
	private void readLoops() {
			DefaultHandler h = new DefaultHandler() {
				Flow currentFlow = null;
				Loop currentLoop = null;
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					super.startElement(uri, localName, qName, attributes);
					if (qName.equals("loop")) {
						currentLoop = new Loop();
						currentLoop.id = attributes.getValue(attributes.getIndex("id"));
						currentLoop.edge = attributes.getValue(attributes.getIndex("edge"));
					} 
					else if (qName.equals("flow")) {
						int h = (int) Double.parseDouble(attributes.getValue(attributes.getIndex("hour")));
						if (h <= stopHour) {						
							currentFlow = new Flow(RouteGeneration.this);
							currentFlow.hour = h;
							currentFlow.loop = currentLoop;
							currentFlow.car = (int) Double.parseDouble(attributes.getValue("cars"));
							currentFlow.truck = (int) Double.parseDouble(attributes.getValue("trucks"));
							outsideFlow[currentFlow.hour - 1] += currentFlow.car + currentFlow.truck;
						}
					}
				}
				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (qName.equals("loop")) {
						loops.add(currentLoop);
					} 
					else if (qName.equals("flow")) {
						if(currentFlow !=null){
							currentFlow.next();
							currentLoop.flows.add(currentFlow);
						}
						currentFlow=null;
					}
				}
			};
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(h);
				logger.info("reading .loop.xml file...");
				parser.parse(new InputSource(baseFolder + baseName + ".loop.xml"));
				logger.info("read " + loops.size() + " induction loops");
			} 
			catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	
	/**
	 * Initializes graph from dgs file if exists, otherwise reads net.xml file, generate dgs file and initialize graph.
	 */
	private void initializeGraph() {
		logger.info("initializing graph...");
		graph = new MultiGraph("ok", false, true);
		graph.addAttribute("ui.stylesheet", styleSheet);
		// ------ For a graphical output of the graph (very slow...)
		// graph.addAttribute("ui.antialias");
		// graph.display(false);
		File dgsf = new File(baseFolder + baseName + ".dgs");
		if (!dgsf.exists()) {
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
	}
		
	/*
	 * Checks if induction loops' base edges exist in the graph so that a Dijkstra
	 */
	private void CheckIfGraphContainInductionLoops() {
		for (Loop loop : loops) {
			if (graph.getNode(loop.edge) == null) {
				logger.error("Error: Induction loop from edge " + loop.edge + " is missing in the graph");
			}
		}
	}

	/*
	 * Checks if edges have an attribute for computing shortest path 
	 */
	private void CheckEdgesContainAttribute(String attrName) {
		int hasIt = 0;
		for (org.graphstream.graph.Node n : graph.getNodeSet()) {
			if (n.getAttribute(attrName) != null)
				hasIt++;
		}
		if(hasIt != graph.getNodeCount()) {
			logger.warn(hasIt + " nodes have the \"weight\" attribute over " + graph.getNodeCount());
		}
	}
	
	private ArrayList<String> computeDijkstraAndFindIsolatedZones() {
		CheckIfGraphContainInductionLoops();
		CheckEdgesContainAttribute("weight");
		
		referenceDjk = new Dijkstra(Dijkstra.Element.NODE, "referenceDjk","weight");
		referenceDjk.init(graph);
		referenceDjk.setSource(graph.getNode(referenceNodeId));
		referenceDjk.compute();
		
		// ---------- generate shortest paths for outer zones ----------
		logger.info("computing the sortest path from each induction loop edge...");
		for (Loop loop : loops) {
			Dijkstra djk = new Dijkstra(Dijkstra.Element.NODE, loop.edge,"weight");
			djk.init(graph);
			djk.setSource(graph.getNode(loop.edge));
			djk.compute();
			loop.dijkstra = loop.edge;
		}

		// ---------- generate shortest paths for RESIDENTIAL zones ----------
		// Generate one shortest path for each Residential zone so as to be able
		// to create INNER traffic.
		// This process is VERY slow and should be changed for optimization
		// purposed. One possibility : store the shortest path in the DGS file.
		logger.info("computing the shortest path from a random node of each residential zone to a reference node ... ");
		
		ArrayList<String> zonesToRemove = new ArrayList<String>();
		
		for (Zone zone : zones.values()) {
			if (zone.type == ZoneType.RESIDENTIAL) {
				Node node = null;
				Dijkstra djk = null;
				boolean unreachable=true;
				int limit = 0;
				do {
					if (limit > 5) {
						zonesToRemove.add(zone.id);
						break;
					}
					node = nodeInZone(zone);
					djk = new Dijkstra(Dijkstra.Element.NODE, node.getId(),"weight");
					djk.init(graph);
					djk.setSource(node);
					djk.compute();
					// checks if there is a path from the node in the zone to the referenceNode
					if (djk.getPathLength(graph.getNode(referenceNodeId)) != Double.POSITIVE_INFINITY) {
						unreachable=false;
					}
					limit++;
				} while (unreachable);
				zone.shortestPath = node.getId();
				zone.sourceNode = node;
				djk = null;
			}
		}
		
//		logger.info("  _checking accessibility of Residential zones. This takes a while... %n"); 
		logger.info("Computing path from a random point in each zone to a reference node. This takes a while... %n"); 
		zonesToRemove.addAll(fillZoneNodes(5));
		
		return zonesToRemove;
	}

	private static ArrayList<String> readStringList(String fileName) {
		ArrayList<String> strings = new ArrayList<String>();
		DataInputStream in;
		try {
			in = new DataInputStream(new FileInputStream(fileName));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				strLine.trim();
				strings.add(strLine);
			}
			in.close();
			return strings;
		}
		catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			return null;
		}
	}
	
	private static void writeStringList(ArrayList<String> strings, String fileName) {
		DataOutputStream out;
		try {
			out = new DataOutputStream(new FileOutputStream(fileName));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			for (String string : strings) {
				bw.write(string);
				bw.newLine();
			}
			bw.flush();
			out.close();
		}
		catch (Exception e) {
			logger.error("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Updates probabilities after the all zone removing stuff
	 */
	private void updateProbabilities() {
		for(Area a : areas){
			a.sumSurfaceZones=0;
		}
		defaultAreaCOM.sumSurfaceZones=0;
		defaultAreaRES.sumSurfaceZones=0;
		defaultAreaIND.sumSurfaceZones=0;
		sumResidentialSurface=0;
		for (Zone z : zones.values()) {
			z.area.sumSurfaceZones += z.surface;
			if (z.type == ZoneType.RESIDENTIAL) {
				sumResidentialSurface += z.surface;
			}
		}
		// recompute probabilities !!
		for (Zone z : zones.values()) {
			z.probability = (z.surface / z.area.sumSurfaceZones) * z.type.probability * z.area.probability;
		}
	}
	
	private void removeZones(ArrayList<String> zonesToRemove) {
		for (String zoneId : zonesToRemove) {
			zones.remove(zoneId);
		}
	}
	
	/**
	 * Populates zone.near_nodes and computes Dijkstra for each node
	 * Checks if a path from a referenceNode to the each node exists
	 * @param maxNearNodesCount the maximum number of near nodes for each zone
	 * @return Zones that don't have a path from a reference node
	 */
	public ArrayList<String> fillZoneNodes(int maxNearNodesCount) {
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
					node = nodeInZone(zone);
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
			//random = new Random(randomSeed);
			double rand = random.nextDouble();
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

	// SUMO route generation
	
	private void writeVTypes() {
		try {
			// ------------- Compute the vtypes ------------------
			for (VType vt : vtypes) {
				ai.clear();
				ai.addAttribute("", "", "accel", "CDATA", vt.accel);
				ai.addAttribute("", "", "color", "CDATA", vt.color);
				ai.addAttribute("", "", "decel", "CDATA", vt.decel);
				ai.addAttribute("", "", "id", "CDATA", vt.id);
				ai.addAttribute("", "", "length", "CDATA", vt.length);
				ai.addAttribute("", "", "minGap", "CDATA", vt.minGap);
				ai.addAttribute("", "", "maxSpeed", "CDATA", vt.maxSpeed);
				ai.addAttribute("", "", "sigma", "CDATA", vt.sigma);
				tfh.startElement("", "", "vType", ai);
				tfh.endElement("", "", "vType");
			}
			// ---------------- the default truck vtype ----------------
			ai.clear();
			ai.addAttribute("", "", "accel", "CDATA", "1.05");
			ai.addAttribute("", "", "color", "CDATA", "0.1,0.1,0.1");
			ai.addAttribute("", "", "decel", "CDATA", "4");
			ai.addAttribute("", "", "id", "CDATA", "truck");
			ai.addAttribute("", "", "length", "CDATA", "15");
			ai.addAttribute("", "", "minGap", "CDATA", "2.5");
			ai.addAttribute("", "", "maxSpeed", "CDATA", "30");
			ai.addAttribute("", "", "sigma", "CDATA", "0");
			tfh.startElement("", "", "vType", ai);
			tfh.endElement("", "", "vType");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateSumoRoutes() {
		
		logger.info("__Starting Sumo route generation.");
		
		try {
			sr = new StreamResult(baseFolder + baseName + ".rou.xml");
			xmlMain();

			tfh.startElement("", "", "routes", ai);

			writeVTypes();

			while (!loops.isEmpty()) {
				// inside flow OR outside flow?
				if (random.nextDouble() < insideFlowRatio) {
					// inside flow
					Path p = goInsideFlow();
					vehicleCounter++;
					ai.clear();
					ai.addAttribute("", "", "id", "CDATA", "resdidentialXXX" /*+ zone.id */+ "_h" + currentHour + "_c" + vehicleCounter);
					ai.addAttribute("", "", "type", "CDATA", vtypes.get((int) (org.util.Random.next() * vtypes.size())).id);
					ai.addAttribute("", "", "depart", "CDATA", "" + (int) currentTime);
					try {
						tfh.startElement("", "", "vehicle", ai);
						ai.clear();
						ai.addAttribute("", "", "edges", "CDATA", pathToString(p));
						tfh.startElement("", "", "route", ai);
						tfh.endElement("", "", "route");
						tfh.endElement("", "", "vehicle");
					} 
					catch (SAXException e) {
						e.printStackTrace();
					}
				} 
				else {
					// outside flow
					nextLoop = loops.pollFirst();
					nextFlow = nextLoop.flows.pollFirst();
					//logger.info("going for outside flow: "+nextFlow);
					currentTime = nextFlow.next;
					if (nextFlow.go()) {
						if (currentHour != nextFlow.hour) {
							logger.info("  _writing flows for hour " + currentHour);
							currentHour = nextFlow.hour;
						}
						nextLoop.flows.add(nextFlow);
						loops.add(nextLoop);
					}
					if (nextLoop.flows.size() > 0) {
						loops.add(nextLoop);
					}
				}
			}
			tfh.endElement("", "", "routes");
			logger.info("Done.");
			tfh.endDocument();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Path goInsideFlow() {
		Zone zone = pickUpOneZone(ZoneType.RESIDENTIAL);
		if (zone == null) {
			logger.error("  _zone is NULL in goInsideFlow!");
			return (null);
		}
		Path p = null;
		int count = 0;
		do {
			if (count > 3) {
				logger.info("  _infinite loop on zone " + zone.id);
				return (null);
			}
			p = createRandomPath(zone.shortestPath, zone.sourceNode);
			count++;
		} 
		while (p == null);
		return (p);
	}

	public static String pathToString(Path path){
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
	
	private void writeRoutesFromFlows() {
		logger.info("__Starting writing SUMO routes.");
		
		try {
			sr = new StreamResult(baseFolder + baseName + ".rou.xml");
			xmlMain();

			tfh.startElement("", "", "routes", ai);

			writeVTypes();
			
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
						String begin = attributes.getValue("begin");
						//String end = attributes.getValue("end");					
						String type = attributes.getValue("type");
						//String number = attributes.getValue("number);
						String route = getRoute(from, to);
						writeRoute(id, type, begin, route);
					}
				}
			};
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(h);
				logger.info("reading flows from .flows.xml file and writing rou.xml...");
				parser.parse(new InputSource(baseFolder + baseName + ".flows.xml"));
				
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
			
			// check the error for flow from -35894685#0 to -24247122
			if (from.equals("-35894685#0")) {
				// no connection between 34072830#0 and 117285293#1
				String badNode1Id = "34072830#0";
				String badNode2Id = "117285293#1";
				Node badNode1 = graph.getNode(badNode1Id);
				Node badNode2 = graph.getNode(badNode2Id);
				Double weight = badNode1.getAttribute("weight");
				Double weight2 = badNode2.getAttribute("weight");
				Dijkstra djk2 = new Dijkstra(Dijkstra.Element.NODE, from, "weight");
				route = pathToString(djk.getPath(graph.getNode("-24247122")));
				String path2 = pathToString(djk.getPath(graph.getNode(referenceNodeId)));
			}
			
		}
		catch (Exception ex) {
			djk.compute();
			route = pathToString(djk.getPath(nodeTo));
		}
		return route;
	}
	
	private void writeRoute(String id, String type, String depart, String edges) {
		ai.clear();
		ai.addAttribute("", "", "id", "CDATA", id);
		ai.addAttribute("", "", "type", "CDATA", type);
		ai.addAttribute("", "", "depart", "CDATA", depart);
		try {
			tfh.startElement("", "", "vehicle", ai);
			ai.clear();
			ai.addAttribute("", "", "edges", "CDATA", edges);
			tfh.startElement("", "", "route", ai);
			tfh.endElement("", "", "route");
			tfh.endElement("", "", "vehicle");
		} 
		catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	public void generateSumoFlows() {
		
		logger.info("__Starting Sumo flows generation.");
		
		try {
			sr = new StreamResult(baseFolder + baseName + ".flows.xml");

			xmlMain();

			tfh.startElement("", "", "flows", ai);
			
			while (!loops.isEmpty()) {
				String vehicleId = vtypes.get((int)(org.util.Random.next() * vtypes.size())).id;
				int numberOfVehicles = 1;
				
				// inside flow OR outside flow?
				if (random.nextDouble() < insideFlowRatio) {
					// inside flow
					Node sourceNode = getResidentialNode(3,5);
					if (sourceNode == null) {
						logger.warn("Any random residential node was chosen");
						continue;
					}
					Node destinationNode = getDestinationNode(sourceNode, 3, 5);
					if (destinationNode == null) {
						logger.warn("Inside flow. There is no path from loop edge " + sourceNode.getId() + "to a random node ");
						continue;
					}		
					vehicleCounter++;
					writeFlow(vehicleCounter, sourceNode.getId(), destinationNode.getId(), (int)currentTime, stopTime, vehicleId, numberOfVehicles); 
				} 
				else {
					// outside flow
					nextLoop = loops.pollFirst();
					nextFlow = nextLoop.flows.pollFirst();
					currentTime = nextFlow.next;
					if (nextFlow.go2()) {
						Node sourceNode = graph.getNode(nextLoop.edge);
						Node destinationNode = getDestinationNode(sourceNode, 10, 10);
						if (destinationNode == null) {
							logger.warn("Outside flow. There is no path from loop edge " + sourceNode.getId() + "to a random node ");
						}
						else {
							vehicleCounter++;
							writeFlow(vehicleCounter, sourceNode.getId(), destinationNode.getId(), (int)currentTime, stopTime, vehicleId, numberOfVehicles); 
							if (currentHour != nextFlow.hour) {
								logger.info("  _writing flows for hour " + currentHour);
								currentHour = nextFlow.hour;
							}
						}
						nextLoop.flows.add(nextFlow);
						loops.add(nextLoop);
					}
					if (nextLoop.flows.size() > 0) {
						loops.add(nextLoop);
					}
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
	
	private Node getDestinationNode(Node sourceNode, int randomZonesLimit, int randomNodesLimit) {
		
		Dijkstra djk = new Dijkstra(Dijkstra.Element.NODE, sourceNode.getId(),"weight");
		djk.init(graph);
		djk.setSource(sourceNode);	
		
		Node node = null;
		Zone zone = null;
		int randomZonesCount = 0;
		boolean unreachable = true;
	
		while (zone == null && randomZonesCount < randomZonesLimit && unreachable) {
			zone = pickUpOneZone(null);
			if (zone != null) {
				int randomNodesCount = 0;
				while (unreachable && randomNodesCount < randomNodesLimit) {
					node = nodeInZone(zone);
					if (node != null) {
						try {
							djk.getPath(node);
						}
						catch (Exception ex) {
							djk.compute();
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
			randomZonesLimit++;
		} 
		if (unreachable) {
			node = null;
		}
		return node;
	}
	
	private Node getResidentialNode(int randomZonesLimit, int randomNodesLimit) {
		Node node = null;
		Zone zone = null;
		int randomZonesCount = 0;
		while (zone == null && randomZonesCount < randomZonesLimit) {
			zone = pickUpOneZone(ZoneType.RESIDENTIAL);
			if (zone != null) {
				int randomNodesCount = 0;
				while (zone == null && randomNodesCount < randomNodesLimit) {
					node = nodeInZone(zone);
					randomNodesCount++;
				}
			}
			randomZonesLimit++;
		} 
		return node;
	}
	
	private void writeFlow(int vehicleCounter, String sourceId, String destinationId, int begin, int end, String vehicleId, int numberOfVehicles) {
		try {
			tfh.startElement("", "", "flow", ai);
			ai.clear();
			ai.addAttribute("", "", "id", "CDATA", "trip" + "_h" + currentHour + "_c" + vehicleCounter);
			ai.addAttribute("", "", "from", "CDATA", "" + sourceId);
			ai.addAttribute("", "", "to", "CDATA", "" + destinationId);
			ai.addAttribute("", "", "begin", "CDATA", "" + begin);
			ai.addAttribute("", "", "end", "CDATA", "" + end);
			ai.addAttribute("", "", "type", "CDATA", "" + vehicleId);
			ai.addAttribute("", "", "number", "CDATA", "" + numberOfVehicles);
			tfh.endElement("", "", "flow");	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Xml writing
	
	public void xmlMain() throws Exception {
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		tfh = tf.newTransformerHandler();
		Transformer serTf = tfh.getTransformer();
		serTf.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		serTf.setOutputProperty(OutputKeys.INDENT, "yes");
		tfh.setResult(sr);
		tfh.startDocument();
		ai = new AttributesImpl();
	}
	
	
	// SUMO TraCI

	private static void ReadTravelTime() {
		
		SumoTraciConnection conn = new SumoTraciConnection(
				"./test/Luxembourg/Luxembourg.sumocfg",  // config file
				12346,                                 // random seed
				false                                  // look for geolocalization info in the map
				);
		try {
			conn.runServer();
			
			// the first two steps of this simulation have no vehicles.
//			conn.nextSimStep();
//			conn.nextSimStep();
			
			for (int i = 0; i < 100; i++) {
				int time = conn.getCurrentSimStep();
				Set<String> vehicles = conn.getActiveVehicles();
				
				//logger.info("At time step " + time + ", there are "+ vehicles.size() + " vehicles: " + vehicles);
				if (vehicles.size()>0) {

					//String aVehicleID = vehicles.iterator().next();
					
//					Vehicle aVehicle = conn.getVehicle(aVehicleID);
//					logger.info("Vehicle " + aVehicleID + " will traverse these edges: " + aVehicle.getCurrentRoute());
					
//					String edgeID = "94661965#0";
//					double travelTime = conn.getEdgeTravelTime(edgeID);
//					logger.info("travelTime of " + edgeID + ": " + travelTime);
				}
				
				conn.nextSimStep();
			}
			String edgeID;
			double travelTime;
			
			edgeID = "50118056#1";
			travelTime = conn.getEdgeTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			edgeID = "56640729#2";
			travelTime = conn.getCurrentTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			edgeID = "50118056#1";
			travelTime = conn.getCurrentTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			edgeID = "56640729#2";
			travelTime = conn.getCurrentTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			conn.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// Parameters
	
	public String[] getParametersNames() {

		ArrayList<String> paramsNames = new ArrayList<String>();

		// Zone types
		paramsNames.add("Residential Type");
		paramsNames.add("Industrial Type");
		paramsNames.add("Commercial Type");

		// inner traffic ratio
		paramsNames.add("Inner Traffic");

		// loop through areas
		for (Area a : areas) {
			switch(a.type){
			case COMMERCIAL: paramsNames.add("COM(" + a.id + ")");break;
			case INDUSTRIAL: paramsNames.add("IND(" + a.id + ")");break;
			case RESIDENTIAL: paramsNames.add("RES(" + a.id + ")");break;
			}
		}

		String[] strings = new String[paramsNames.size()];
		return paramsNames.toArray(strings);
	}

	public double[][] getParametersBoundaries() {

		double[] min = new double[4 + areas.size()];
		double[] max = new double[4 + areas.size()];

		min[0] = 1.0;
		min[1] = 1.0;
		min[2] = 1.0;

		min[3] = 0.3;

		max[0] = 100.0;
		max[1] = 100.0;
		max[2] = 100.0;

		max[3] = 0.7;

		for (int i = 0; i < areas.size(); i++) {
			min[4 + i] = 1.0;
			max[4 + i] = 10.0;
		}

		double bounds[][] = new double[2][min.length];
		bounds[0] = min;
		bounds[1] = max;

		return bounds;
	}
	
	
	// Optimization part
	
	/*
	 * Does the flow generation process
	 */
	public void flowGeneration() {
		for (Loop loop : loops) {
			for (Flow flow : loop.flows) {
				if (flow.hour > stopHour) {
					continue;
				}
				Path path;
				for (int cars = 0; cars < flow.car; cars++) {
					do {
						path = createRandomPath(loop.dijkstra,graph.getNode(loop.edge));
						if (path == null) {
							logger.info("  _outer raffic infinit loop: " + loop.edge+" flow:" + flow.hour );
						}
					} while(path==null);
					flowGenerationUp(flow, path);
					// inside flow
					if (random.nextDouble() < insideFlowRatio) {
						do {
							path = goInsideFlow();
						} while(path==null);
						flowGenerationUp(flow, path);
					}
				}
			}
		}
	}

	/**
	 * @param flow
	 * @param path
	 */
	private void flowGenerationUp(Flow flow, Path path) {
		for (Node n : path.getNodePath()) {
			String cNode = realNodes.get(n);
			if (cNode != null) {
				Detector d = currentSolution.get(cNode);
				if (d == null) {
					d = currentSolution.put(cNode, new Detector(stopHour));
				}
				d.vehicles[flow.hour - 1] += 1;
				// should break here...
			}
		}
	}
	
	public double evaluate(double[] individual) {
		ZoneType.RESIDENTIAL.probability = individual[0];
		ZoneType.INDUSTRIAL.probability = individual[1];
		ZoneType.COMMERCIAL.probability = individual[2];
		insideFlowRatio = individual[3];
		for (int i = 4; i < individual.length; i++) {
			areas.get(i - 4).probability = individual[i];
		}
		return doEvaluate();
	}
	
	public double evaluate(Individual ind) {

		random = new Random(randomSeed); // reset seed!

		//Prints the individual 
		String individual = "Individual:";
		for(int i=0; i<ind.getLength();i++) {
			individual += " " + ind.getAllele(i);
		}
		logger.info(individual);
		
		ZoneType.RESIDENTIAL.probability = (Double)ind.getAllele(0)/100;
		ZoneType.INDUSTRIAL.probability = (Double)ind.getAllele(1)/100;
		ZoneType.COMMERCIAL.probability = (Double)ind.getAllele(2)/100;
			
		//Fills in the different Commercial areas probabilities
		// Zc1/Zc2/Zc3/
		for (int i = 3; i < 6; i++) {
			areas.get(i - 3).probability = (Double)ind.getAllele(i)/100;
		}
		
		//Fills in the default Commercial area probability
		//Zcd/
		defaultAreaCOM.probability = (Double)ind.getAllele(6)/100;
		
		//Fills in the Industrial area probability
		//Zi1
		areas.get(3).probability = (Double)ind.getAllele(7)/100;
		
		//Fills in the default Industrial area probability
		//Zid
		defaultAreaIND.probability = (Double)ind.getAllele(8)/100;
		
		//Fills in the Residentia area probability
		//Zr1
		areas.get(4).probability = (Double)ind.getAllele(9)/100;
		
		//Fills in the default Residential area probability
		//Zrd
		defaultAreaRES.probability = (Double)ind.getAllele(10)/100;
		
		//Fills in the insideFlowRatio and ShiftingRatio
		//IR/SR
		insideFlowRatio = (Double)ind.getAllele(11)/100;
		shiftingRatio = (Double)ind.getAllele(12)/100;
		
		return doEvaluate();
	}
	
	private double doEvaluate() {
		long start = System.currentTimeMillis();
		double fitness = 0;		

		// recompute probabilities !!
		for (Zone z : zones.values()) {
			z.probability = (z.surface / z.area.sumSurfaceZones) * z.type.probability * z.area.probability;
		}

		for(Detector d : currentSolution.values()){
			d.reset();
			//Sets the shiftingRatio for each Detector loop
			d.setShiftingRatio(shiftingRatio);
		}
		flowGeneration();
		
		//Applies shiftingRatio for each control point
		for(Detector d : currentSolution.values()){
			
			//Prints counted traffic in control point BEFORE shift
			//logger.info(" Before Shift: " + d);
			
			//Applies Shifting Ratio
			d.shift();
			
			//Prints counted traffic in control point AFTER shift
			//logger.info(" After Shift: " + d);
		}
		
		fitness = evaluator.compareTo(currentSolution);

		System.out.printf("%.1f s%n",(System.currentTimeMillis()-start)/1000.0);
		return fitness;
	}

}
