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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import jcell.Individual;

import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.evaluation.RealEvaluation;
import lu.uni.routegeneration.net.RGServer;
import lu.uni.routegeneration.ui.AreasEditor;

import org.graphstream.algorithm.DijkstraFH;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
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
 * Main class that handles all the process of creating mobility traces based on
 */
public class RouteGeneration {

	// ----------- PARAMETERS ----------
	/**
	 * Project name. Is assumed to be the base name of all configuration files
	 * (ex. MyProject.rou.xml, MyProject.net.xml)
	 */
	String baseName = "LuxembourgVille";

	/**
	 * Path that to the folder containing configuration files.
	 */
	String baseFolder = "./test/";

	
	/**
	 * 
	 */
	int stopHour = 11;


	HashMap<String, Detector> currentSolution;

	int stopTime = stopHour * 3600;

	String projParameter = "+proj=utm + zone=31 +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
	Point2D.Double netOffset;
	public HashMap<String, Zone> zones;
	public ArrayList<Area> areas;
	Projection proj;
	public ArrayList<Lane> edges;
	HashMap<String, Point2D.Double> nodes;

	Vector<Point2D.Double> destinations;

	int val = 0;

	ArrayList<Loop> loops;
	Graph graph;

	// Xml writing
	BufferedReader br;
	StreamResult sr;
	TransformerHandler tfh;
	AttributesImpl ai;
	AreasEditor ae;
	Loop nextLoop;
	double insideFlowRatio = 0.5;
	
	/**
	 * @return the stopHour
	 */
	public int getStopHour() {
		return stopHour;
	}

	/**
	 * @param stopHour
	 *            the stopHour to set
	 */
	public void setStopHour(int stopHour) {
		this.stopHour = stopHour;
	}

	/**
	 * @return the baseName
	 */
	public String getBaseName() {
		return baseName;
	}

	/**
	 * @param baseName
	 *            the baseName to set
	 */
	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	/**
	 * @return the baseFolder
	 */
	public String getBaseFolder() {
		return baseFolder;
	}

	/**
	 * @param baseFolder
	 *            the baseFolder to set
	 */
	public void setBaseFolder(String folderName) {
		this.baseFolder = folderName;
	}

	/**
	 * @return the insideFlowRatio
	 */
	public double getInsideFlowRatio() {
		return insideFlowRatio;
	}

	/**
	 * @param insideFlowRatio
	 *            the insideFlowRatio to set
	 */
	public void setInsideFlowRatio(double insideFlowRatio) {
		this.insideFlowRatio = insideFlowRatio;
	}

	double outsideFlow[];
	int currentHour = 0;

	double sumResidentialSurface = 0.0;

	Flow nextFlow;
	
	
	public int port=0;

	private String styleSheet = "graph { padding: 60px; fill-color:#eeeeee;}"
			+ "node { z-index:3; size: 1px; fill-color: #777777; }"
			+ "node.internal{ fill-color: #BB4444; }"
			+ "edge  { fill-color: #404040; size: 1px;}"
			+ "sprite {text-style:bold; text-color: #555555;  fill-color:#eeeeee; }"
			+ "edge.path {fill-color: #ff4040;}";
	double currentTime = 0;

	private Area defaultAreaIND;

	private Area defaultAreaCOM;

	private Area defaultAreaRES;

	private RealEvaluation evaluator;

	private HashMap<Node, String> realNodes;

	private String referenceNodeId= "9647221";
	private DijkstraFH referenceDjk;

	private ArrayList<Zone> zonesToRemove;
	
	public Path createRandomPath(String djk, Node source) {

		Path p = getShortestPath(djk, source, pickUpOneDestination());
		if (p.empty()) {
			return null;
		} else {
			return p;

			/*
			 * StringBuilder sb = new StringBuilder();
			 * 
			 * List<Node> l = p.getNodePath(); for (int i = l.size() - 1; i >=
			 * 0; i--) { l.get(i).addAttribute("ui.class", "path");
			 * sb.append(l.get(i).getId()); if (i > 0) sb.append(" "); }
			 * 
			 * return sb.toString();
			 */
		}
	}

	/**
	 * @param djk
	 * @param source
	 * @param pickUpOneDestination
	 * @return
	 */
	private Path getShortestPath(String djk, Node source,
			Node target) {
		DijkstraFH dummyDjk = new DijkstraFH(DijkstraFH.Element.EDGE,djk);
		dummyDjk.setSource(source);
		return dummyDjk.getPath(target);
	}

	private Point2D.Double pointInZone(Zone zone) {
		Point2D.Double point = new Point2D.Double();
		do {
			point.x = Math.random()
					* (zone.max_x_boundary - zone.min_x_boundary)
					+ zone.min_x_boundary;
			point.y = Math.random()
					* (zone.max_y_boundary - zone.min_y_boundary)
					+ zone.min_y_boundary;
		} while (!isIn(point, zone));

		
		return point;
	}

	/**
	 * @return
	 */
	private Node pickUpOneDestination() {
		// select a zone based on its proba
		Zone zone = null;
		while (zone == null) {
			double draw = Math.random();
			double sum = 0.0;
			for (Zone z : zones.values()) {
				sum += z.probability;
				if (sum > draw) {
					zone = z;
					break;
				}
			}
		}

		int randNode = (int) (Math.random() * zone.near_nodes.size());
		return zone.near_nodes.get(randNode);

	}

	private Node getClosestNode(Point2D.Double p) {

		Iterator<? extends Node> it = graph.getNodeIterator();
		Node closestNode = it.next();
		double closestX = (Double) closestNode.getAttribute("x");
		double closestY = (Double) closestNode.getAttribute("y");
		double closestDist = Math.sqrt(Math.pow(closestX - p.x, 2.0)
				+ Math.pow(closestY - p.y, 2.0));

		while (it.hasNext()) {
			Node currentNode = it.next();
			if (currentNode.getDegree() > 0) {
				double currentX = (Double) currentNode.getAttribute("x");
				double currentY = (Double) currentNode.getAttribute("y");
				double currentDist = Math.sqrt(Math.pow(currentX - p.x, 2.0)
						+ Math.pow(currentY - p.y, 2.0));
				if (currentDist <= closestDist) {
					closestNode = currentNode;
					closestDist = currentDist;
				}
			}

		}
		return closestNode;
	}

	/**
	 * @param point
	 * @param zone
	 * @return
	 */
	private boolean isIn(Point2D.Double point, Zone zone) {
		Point2D.Double other = new Point2D.Double(zone.max_x_boundary, point.y);
		int n = 0;
		for (int i = 0; i < zone.points.size() - 1; i++) {
			if (intersect(point, other, zone.points.get(i),
					zone.points.get(i + 1)))
				n++;
		}
		return n % 2 == 1;
	}

	private boolean ccw(Point2D.Double A, Point2D.Double B, Point2D.Double C) {
		return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
	}

	private boolean intersect(Point2D.Double A, Point2D.Double B,
			Point2D.Double C, Point2D.Double D) {
		return ccw(A, C, D) != ccw(B, C, D) && ccw(A, B, C) != ccw(A, B, D);
	}

	public RouteGeneration() {
		
		/*
		Thread t = new Thread(){
			public void run() {org.util.ui.MemoryMonitor.show();};
		};
		t.start();
		*/

		//org.util.Environment.getGlobalEnvironment().readCommandLine(args);
		//org.util.Environment.getGlobalEnvironment().initializeFieldsOf(this);

		evaluator = new RealEvaluation();
		currentSolution = new HashMap<String, Detector>();
		for(String id : evaluator.controls.keySet()){
			currentSolution.put(id, new Detector(stopHour));
		}
		File folder = new File(baseFolder);

		zones = new HashMap<String, Zone>();
		areas = new ArrayList<Area>();
		edges = new ArrayList<Lane>();
		destinations = new Vector<Point2D.Double>();
		DefaultHandler h;
		ArrayList<Lane> Edges = new ArrayList<Lane>();

		outsideFlow = new double[stopHour];

		// -------------------------------------------------------------------
		// -------------------------- NET FILE -------------------------------
		System.out.println("__Netfile");

		String netFile = baseName + ".net.xml";
		h = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, qName, attributes);
				if (qName.equals("location")) {
					projParameter = attributes.getValue("projParameter");
					proj = ProjectionFactory
							.fromPROJ4Specification(projParameter.split(" "));
					String offset = attributes.getValue("netOffset");
					String[] toffset = offset.split(",");
					netOffset = new Point2D.Double();
					netOffset.x = Double.parseDouble(toffset[0]);
					netOffset.y = Double.parseDouble(toffset[1]);

				}

				if (qName.equals("lane")) {
					Lane e = new Lane();
					String shape = attributes.getValue("shape");
					for (String point : shape.split(" ")) {
						Point2D.Double p = new Point2D.Double();
						String[] xy = point.split(",");
						p.x = Double.parseDouble(xy[0]);
						p.y = Double.parseDouble(xy[1]);
						e.shape.add(p);
					}
					edges.add(e);
				}

			}
		};
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
			parser.parse(new InputSource(baseFolder + netFile));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		// -------------------------------------------------------------------
		// ---------------------- OpenStreetMap FILE -------------------------
		// - Read the file for zones.
		// - create zones (Commercial, Residential, Industrial) with 'Zone'
		// objects.
		// - Store zones in the 'zones' map.
		//
		// - Q : is it possible to store one Dijkstra on one node of each
		// residential zone?
		// - pros: speed up computation
		// - cons: larger files stored, strengthen dependency to DGS file
		System.out.println("__Zones");

		String OSMFile = baseName + ".osm.xml";

		zones = new HashMap<String, Zone>();
		nodes = new HashMap<String, Point2D.Double>();

		class OSMHandler extends DefaultHandler {
			Zone zone = null;

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, qName, attributes);

				if (qName.equals("node")) {
					double x = Double.parseDouble(attributes
							.getValue(attributes.getIndex("lon")));
					double y = Double.parseDouble(attributes
							.getValue(attributes.getIndex("lat")));
					Point2D.Double dest = new Point2D.Double();
					proj.transform(x, y, dest);
					dest.x = dest.x + netOffset.x;
					dest.y = dest.y + netOffset.y;
					nodes.put(attributes.getValue("id"), dest);
				} else if (qName.equals("way")) {
					if (zones.get(attributes.getValue("id")) == null) {
						zone = new Zone();
						zone.id = attributes.getValue("id");
					}
				} else if (qName.equals("nd") && zone != null) {
					zone.points.add(nodes.get(attributes.getValue("ref")));
				} else if (qName.equals("tag") && zone != null) {
					if (attributes.getValue("k").equals("landuse")) {
						String landuse = attributes.getValue("v");
						if (landuse.equals("residential")) {
							zone.type = ZoneType.RESIDENTIAL;
						} else if (landuse.equals("industrial")) {
							zone.type = ZoneType.INDUSTRIAL;
						} else if (landuse.equals("commercial")
								|| landuse.equals("retail")) {
							zone.type = ZoneType.COMMERCIAL;
						}
					} else if (attributes.getValue("k").equals("shop")
							|| attributes.getValue("k").equals("amenity")) {
						zone.type = ZoneType.COMMERCIAL;
						
					}
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (qName.equals("way") && zone != null) {
					if (zone.type != null) {

						// compute area of the zone
						zone.surface = 0.0;
						for (int i = 0; i < zone.points.size() - 1; i++) {
							zone.surface += zone.points.get(i).x
									* zone.points.get(i + 1).y
									- zone.points.get(i + 1).x
									* zone.points.get(i).y; // x0*y1 - x1*y0
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
							if (p.x < zone.min_x_boundary)
								zone.min_x_boundary = p.x;
							if (p.x > zone.max_x_boundary)
								zone.max_x_boundary = p.x;
							if (p.y < zone.min_y_boundary)
								zone.min_y_boundary = p.y;
							if (p.y > zone.max_y_boundary)
								zone.max_y_boundary = p.y;
						}
						zones.put(zone.id, zone);
					}
					zone = null;
				}
			}
		}
		;
		h = new OSMHandler();

		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles) {
			if (f.isFile() && f.getName().startsWith(baseName)
					&& f.getName().endsWith(".osm.xml")) {
				try {
					XMLReader parser = XMLReaderFactory.createXMLReader();
					parser.setContentHandler(h);
					parser.parse(new InputSource(new FileInputStream(f)));
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		}

		// -------------------------------------------------------------------
		// ---------------- Read Areas from .areas.xml file -------------------
		//
		// - File is read and the areas are created (Area class).
		// - Areas are stored in the `areas` list.
		// - Zone types probabilities are set.

		//
		// An `.areas.xml` example file::

		/*
		 * <areas residential_proba="5" commercial_proba="80"
		 * industrial_proba="15"> <area id="1" type="COMMERCIAL" x="20418"
		 * y="14500" radius="1500" probability="10"/> <area id="2"
		 * type="COMMERCIAL" x="22400" y="16700" radius="1500"
		 * probability="15"/> </areas>
		 */
		System.out.println("__Areas");

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
		// areas.add(defaultAreaCOM);
		// areas.add(defaultAreaIND);
		// areas.add(defaultAreaRES);

		File file = new File(areasFile);
		if (file.exists()) {
			class AreasHandler extends DefaultHandler {
				Zone zone = null;
				double sumRES = 0.0;
				double sumCOM = 0.0;
				double sumIND = 0.0;

				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					super.startElement(uri, localName, qName, attributes);
					if (qName.equals("areas")) {

						double sum = 0.0;
						sum += ZoneType.RESIDENTIAL.probability = Double
								.parseDouble(attributes
										.getValue("residential_proba"));

						sum += ZoneType.COMMERCIAL.probability = Double
								.parseDouble(attributes
										.getValue("commercial_proba"));

						sum += ZoneType.INDUSTRIAL.probability = Double
								.parseDouble(attributes
										.getValue("industrial_proba"));
						ZoneType.RESIDENTIAL.probability /= sum;
						ZoneType.COMMERCIAL.probability /= sum;
						ZoneType.INDUSTRIAL.probability /= sum;
						System.out
								.println("sum proba types:"
										+ (ZoneType.RESIDENTIAL.probability
												+ ZoneType.COMMERCIAL.probability + ZoneType.INDUSTRIAL.probability));

					}
					if (qName.equals("area")) {
						Area a = new Area();
						a.id = attributes.getValue("id");
						a.x = Double.parseDouble(attributes.getValue("x"));
						a.y = Double.parseDouble(attributes.getValue("y"));
						a.radius = Double.parseDouble(attributes
								.getValue("radius"));
						a.probability = Double.parseDouble(attributes
								.getValue("probability"));
						String type = attributes.getValue("type");
						if (type.equals("RESIDENTIAL")) {
							a.type = ZoneType.RESIDENTIAL;
							sumRES += a.probability;
						} else if (type.equals("INDUSTRIAL")) {
							a.type = ZoneType.INDUSTRIAL;
							sumIND += a.probability;
						} else if (type.equals("COMMERCIAL")) {
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
					System.out.printf("areas proba... %f %f %f%n", sumCOM,
							sumIND, sumRES);
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
						System.out.printf("area proba %s: %f%n", a.id,
								a.probability);
					}
					defaultAreaCOM.probability /= sumCOM;
					defaultAreaIND.probability /= sumIND;
					defaultAreaRES.probability /= sumRES;

					System.out
							.printf("sum proba areas: %f %f %f%n", sc, si, sr);
				}
			}
			;
			h = new AreasHandler();
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(h);
				parser.parse(new InputSource(new FileInputStream(file)));
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}

		// -------------------------------------------------------------------
		// ------------- compute which areas on which zone -----------------

		System.out.println("__Areas<->Zones");

		for (Zone z : zones.values()) {
			for (Area a : areas) {
				if (z.area == null && a.type == z.type) {
					for (Point2D.Double p : z.points) {
						if (a.radius > euclideanDistance(a.x, a.y, p.x, p.y)) {
							//System.out.println(z.id + " in area" + a.id);
							z.area = a;
							break;
						}
					}
				}

			}
		}

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

		// -------------------------------------------------------------------
		// ------------------------ Read loops and flows ---------------------
		//
		// - baseName.loop.xml files.
		// - Real data used as input for outer traffic.
		// - Each real counting loop is linked to an edge (must exist in the
		// .net.xml file)
		// - For each loop a Loop object is created.
		// - For each loop, flows are created: one per hour.
		System.out.println("__Flows");

		loops = new ArrayList<Loop>();
		h = new DefaultHandler() {
			Flow currentFlow = null;
			Loop currentLoop = null;

			// boolean okCar = false;
			// boolean okTruck = false;

			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, qName, attributes);
				if (qName.equals("loop")) {
					currentLoop = new Loop();
					currentLoop.id = attributes.getValue(attributes
							.getIndex("id"));
					currentLoop.edge = attributes.getValue(attributes
							.getIndex("edge"));

				} else if (qName.equals("flow")) {
					int h = (int) Double.parseDouble(attributes
							.getValue(attributes.getIndex("hour")));

					currentFlow = new Flow(RouteGeneration.this);
					currentFlow.hour = h;
					currentFlow.loop = currentLoop;
					currentFlow.car = (int) Double.parseDouble(attributes
							.getValue("cars"));
					currentFlow.truck = (int) Double.parseDouble(attributes
							.getValue("trucks"));
					if (h <= stopHour) {
						outsideFlow[currentFlow.hour - 1] += currentFlow.car
								+ currentFlow.truck;
					}
				}

			}

			@Override
			public void endElement(String uri, String localName, String qName)
					throws SAXException {
				if (qName.equals("loop")) {
					loops.add(currentLoop);
				} else if (qName.equals("flow")) {
					currentLoop.flows.add(currentFlow);
				}
			}

		};
		try {
			// ---------------- Read loops from file ---------------------
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
			parser.parse(new InputSource(baseFolder + baseName + ".loop.xml"));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

		// -------------------------------------------------------------------
		// -------- Initialize the Graph for ShortestPath computation---------
		System.out.println("__Graph");

		graph = new MultiGraph("ok", false, true);
		graph.addAttribute("ui.stylesheet", styleSheet);

		// ------ For a graphical output of the graph (very slow...)
		// graph.addAttribute("ui.antialias");
		// graph.display(false);

		File dgsf = new File(baseFolder + baseName + ".dgs");
		if (!dgsf.exists()) {
			System.out.print("Generating the DGS file...");
			SumoNetworkToDGS netParser = new SumoNetworkToDGS(baseFolder,
					baseName);
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(netParser);
				parser.parse(new InputSource(baseFolder + baseName + ".net.xml"));
				System.out.println("OK");
			} catch (Exception ex) {
				System.out.println("ERROR");
				ex.printStackTrace(System.err);
			}

		}

		try {
			System.out.print("Loading the DGS file...");
			graph.read(baseFolder + baseName + ".dgs");
			System.out.println("OK");
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GraphParseException e) {
			e.printStackTrace();
		}

		realNodes = new HashMap<Node, String>();
		for (Detector d : evaluator.controls.values()) {
			realNodes.put(graph.getNode(d.edge), d.id);
		}
		// -------------------------------------------------------------------
		// ------------------------ test sources from loops ------------------
		//
		// - Loops' base edge have to exist in the graph so that a Dijkstra
		// shortest path can be computed.

		for (Loop loop : loops) {
			if (graph.getNode(loop.edge) == null) {
				System.out.printf("Error: source %s missing in the graph%n",
						loop.edge);
			}
		}
		int hasIt = 0;
		for (org.graphstream.graph.Node n : graph.getNodeSet()) {
			if (n.getAttribute("weight") != null)
				hasIt++;
		}
		System.out.printf("%d nodes have the \"weight\" attribute over %d%n",
				hasIt, graph.getNodeCount());

		referenceDjk = new DijkstraFH(DijkstraFH.Element.NODE, "referenceDjk","weight");
		referenceDjk.init(graph);
		referenceDjk.setSource(graph.getNode(referenceNodeId));
		referenceDjk.compute();
		// -------------------------------------------------------------------
		// ---------- generate shortest paths for outer zones ----------
		System.out.println("__Flows ShortestPaths");
		for (Loop loop : loops) {
			DijkstraFH djk = new DijkstraFH(DijkstraFH.Element.NODE, loop.edge,"weight");
			djk.init(graph);
			djk.setSource(graph.getNode(loop.edge));
			djk.compute();
			loop.dijkstra = loop.edge;
		}

		// -------------------------------------------------------------------
		// ---------- generate shortest paths for RESIDENTIAL zones ----------
		//
		// Generate one shortest path for each Residential zone so as to be able
		// to create INNER traffic.
		//
		// This process is VERY slow and should be changed for optimization
		// purposed. One possibility : store the shortest path in the DGS file.
		//
		System.out.println("__Residential Zones ShortestPaths");

		int zone_count = 0;
		zonesToRemove = new ArrayList<Zone>();

		for (Zone z : zones.values()) {
			zone_count++;

			if (z.type == ZoneType.RESIDENTIAL) {
				System.out
						.printf("Shortest path for INNER TRAFFIC (residential zones) %d over %d  %n",
								zone_count, zones.size());
				//Path path = null;
				Node n = null;
				DijkstraFH djk = null;
				boolean unreachable=true;
				int limit = 0;
				do {
					if (limit > 5) {
						zonesToRemove.add(z);
						System.out.printf("zone %s should be removed.%n", z.id);
						break;

					}
					Point2D.Double point = pointInZone(z);
					n = getClosestNode(point);
					djk = new DijkstraFH(DijkstraFH.Element.NODE, n.getId(),"weight");
					djk.init(graph);
					djk.setSource(n);
					djk.compute();
					// a reference node that ensures this zone can reach the
					// network.
					if (djk.getPathLength(graph.getNode(referenceNodeId))!=Double.POSITIVE_INFINITY){
						unreachable=false;
					}
					//path = getShortestPath(n.getId(),n,graph.getNode(referenceNodeId));
					
					limit++;
				} while (unreachable);
				z.shortestPath = n.getId();
				z.sourceNode = n;
				djk = null;

			}
		}

		for (Zone z : zonesToRemove) {
			zones.remove(z.id);
		}
		if(zonesToRemove.size()>0){
			System.out.printf("Removing %d zones for having no route to the reste of the map.%n",zonesToRemove.size());
		}
		zonesToRemove.clear();

		for (Zone z : zones.values()) {
			fillZoneNodes(z);
		}
		if(zonesToRemove.size()>0){
			System.out.printf("Removing %d zones for being unreachable from the rest of the map.%n",zonesToRemove.size());
		}
		for (Zone z : zonesToRemove) {
			zones.remove(z.id);
		}
		
		
		// update probas after the all zone removing stuff
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

			z.probability = (z.surface / z.area.sumSurfaceZones)
					* z.type.probability * z.area.probability;
		}

		
		
		
		
		System.out.println("Init Done.");
		if(port!=0){
			System.out.println("Starting listenning on port "+port);
			try {
				new RGServer(this, port);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// does the flow generation process
	public void flowGeneration() {

		for (Loop loop : loops) {

			for (Flow flow : loop.flows) {
				if (flow.hour > stopHour)
					continue;
				Path path;
				for (int cars = 0; cars < flow.car; cars++) {
					do{
						path = createRandomPath(loop.dijkstra,graph.getNode(loop.edge));
						if(path == null){
							System.out.println("Outer raffic infinit loop: "+loop.edge+" flow:"+flow.hour );
						}
					}while(path==null);
					flowGenerationUp(flow, path);

					// inside flow
					if (Math.random() < insideFlowRatio) {
						do{
							//System.out.print(">");
							path = goInsideFlow();
							//System.out.println("<");
						}while(path==null);
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
					d = currentSolution.put(cNode, new Detector(
							stopHour));
				}
				d.vehicles[flow.hour - 1] += 1;
				// should break here...
			}
		}
	}

	private Path goInsideFlow() {
		//System.out.println(">InsideFlow");
		Zone zone = null;
		String path = null;

		double rand = Math.random();
		zone = null;
		double sum = 0.0;
		for (Zone z : zones.values()) {
			if (z.type == ZoneType.RESIDENTIAL) {
				sum += z.surface;
				if (sum > (rand * sumResidentialSurface)) {
					zone = z;
					break;
				}
			}
		}
		if (zone == null) {
			System.out.printf("zone is NULL in goInsideFlow !!! rand=%f sum=%f%n",rand,sum);
			return (null);
		}
		Path p = null;
		int count = 0;
		do {
			if (count > 3) {
				System.out.println("infinite loop on zone " + zone.id);
				return (null);
			}
			p = createRandomPath(zone.shortestPath, zone.sourceNode);
			count++;

		} while (p == null);
		return (p);
	}

	private double euclideanDistance(double x, double y, double x2, double y2) {
		return Math.sqrt(Math.pow((x - x2), 2) + Math.pow((y - y2), 2));
	}

	public void xmlMain() throws Exception {
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
				.newInstance();

		tfh = tf.newTransformerHandler();
		Transformer serTf = tfh.getTransformer();
		serTf.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		serTf.setOutputProperty(OutputKeys.INDENT, "yes");
		tfh.setResult(sr);
		tfh.startDocument();
		ai = new AttributesImpl();
	}

	public void fillZoneNodes(Zone z) {
		for (int i = 0; i < 5; i++) {
			int times = 0;
			Node n = null;
			do {
				Point2D.Double point = pointInZone(z);
				n = getClosestNode(point);
				// test this node
				if (referenceDjk.getPathLength(n) == Double.POSITIVE_INFINITY ) {
					n = null;
				}
				times++;
			} while (n == null && times <= 5);
			if (n != null) {
				z.near_nodes.add(n);
			}
		}
		if(z.near_nodes.size()==0){
			zonesToRemove.add(z);
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

		
		ZoneType.RESIDENTIAL.probability = (Double)ind.getAllele(0);
		ZoneType.INDUSTRIAL.probability = (Double)ind.getAllele(1);
		ZoneType.COMMERCIAL.probability = (Double)ind.getAllele(2);
		insideFlowRatio = (Double)ind.getAllele(3);
		for (int i = 4; i < ind.getLength(); i++) {
			areas.get(i - 4).probability = (Double)ind.getAllele(i);
		}
		return doEvaluate();
	}
	
	private double doEvaluate(){
		long start = System.currentTimeMillis();
		double fitness = 0;

		double sum = ZoneType.RESIDENTIAL.probability
				+ ZoneType.INDUSTRIAL.probability
				+ ZoneType.COMMERCIAL.probability;
		ZoneType.RESIDENTIAL.probability /= sum;
		ZoneType.INDUSTRIAL.probability /= sum;
		ZoneType.COMMERCIAL.probability /= sum;
		
		double sumRES = 1;
		double sumCOM = 1;
		double sumIND = 1;
		for (Area a : areas) {
			switch (a.type) {
			case COMMERCIAL:
				sumCOM += a.probability;
				break;
			case INDUSTRIAL:
				sumIND += a.probability;
				break;
			case RESIDENTIAL:
				sumRES += a.probability;
				break;
			}
			//System.out.printf("area proba %s: %f%n", a.id, a.probability);
		}
		for (Area a : areas) {
			switch (a.type) {
			case COMMERCIAL:
				a.probability /= sumCOM;
				break;
			case INDUSTRIAL:
				a.probability /= sumIND;
				break;
			case RESIDENTIAL:
				a.probability /= sumRES;
				break;
			}
			//System.out.printf("area proba %s: %f%n", a.id, a.probability);
		}

		defaultAreaCOM.probability = 1/sumCOM;
		defaultAreaIND.probability = 1/sumIND;
		defaultAreaRES.probability = 1/sumRES;

		// recompute probabilities !!

		for (Zone z : zones.values()) {

			z.probability = (z.surface / z.area.sumSurfaceZones)
					* z.type.probability * z.area.probability;
		}

		for(Detector d : currentSolution.values()){
			d.reset();
		}
		flowGeneration();
		fitness = evaluator.compareTo(currentSolution);

		
		System.out.printf("%.1f s%n",(System.currentTimeMillis()-start)/1000.0);
		return fitness;

	}
	

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


	public static void main(String[] args) {
		RouteGeneration rg = new RouteGeneration();
		rg.doEvaluate();
		double []  res = rg.evaluator.eachDetectorCompareTo(rg.currentSolution);
		int i=0;
		for(String key : rg.currentSolution.keySet()){
			System.out.printf("%s ",key);
		}
		System.out.println();
		for(double d : res){
			System.out.printf("%.0f ",d);
		}
		System.out.println();
		//new ApproximativeEvaluation(args);
	}
}
