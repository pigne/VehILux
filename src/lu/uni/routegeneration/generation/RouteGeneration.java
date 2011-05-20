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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;
import java.io.*;
import java.util.Calendar;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import lu.uni.routegeneration.evaluation.ApproximativeEvaluation;
import lu.uni.routegeneration.ui.AreasEditor;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
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
import java.util.GregorianCalendar;
import jmetal.base.*;
import jmetal.base.variable.*;
import jmetal.util.JMException;

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
public class RouteGeneration extends Problem {

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
	 * Activate the debuging interface.
	 */
	boolean gui = false;

	/**
	 * 
	 */
	int stopHour = 11;

	public static Color colorCOM = new Color(0xCFA9CB); // CFC4CD
	public static Color colorCOM_light = new Color(255, 0, 225, 20);
	public static Color colorRES = new Color(0xAACFCB); // C4CFCE
	public static Color colorRES_light = new Color(0, 255, 255, 20);
	public static Color colorIND = new Color(0xCFC1A9); // CFCBC4
	public static Color colorIND_light = new Color(255, 170, 0, 20);

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

	ArrayList<VType> vtypes;
	//TreeSet<Loop> loops;
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
        EvaluationData evalData;

        ArrayList<AreaType> areaTypes;

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

	private String styleSheet = "graph { padding: 60px; fill-color:#eeeeee;}"
			+ "node { z-index:3; size: 1px; fill-color: #777777; }"
			+ "node.internal{ fill-color: #BB4444; }"
			+ "edge  { fill-color: #404040; size: 1px;}"
			+ "sprite {text-style:bold; text-color: #555555;  fill-color:#eeeeee; }"
			+ "edge.path {fill-color: #ff4040;}";
	double currentTime = 0;


	public String createRandomPath(String djk, Node source) {

		Path p = Dijkstra.getShortestPath(djk, source, pickUpOneDestination());
		if (p.empty()) {
			return null;
		} else {

			StringBuilder sb = new StringBuilder();
			List<Node> l = p.getNodePath();
			for (int i = l.size() - 1; i >= 0; i--) {
				l.get(i).addAttribute("ui.class", "path");
				sb.append(l.get(i).getId());
				if (i > 0)
					sb.append(" ");
			}

			return sb.toString();
		}
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

		if (gui) {
			destinations.add(point);
			ae.mbox.post("Segregation", point);
		}
		return point;
	}

	/**
	 * @return
	 */
	private Node pickUpOneDestination() {
		// select a zone based on its proba
		Zone zone = null;
		while(zone==null){
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

                int randNode = (int)(Math.random()*5);
                if (zone==null) System.out.println("null zone!");
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
			if (intersect(point, other, zone.points.get(i), zone.points
					.get(i + 1)))
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

                areaTypes = new ArrayList<AreaType>();
                addAreaType("Residential", 2);
                addAreaType("Commercial", 4);
                addAreaType("Industrial", 2);

                //inital values of Jmetal.Problem class variables
                numberOfVariables_  = 4;
                numberOfObjectives_ = 1;
                numberOfConstraints_= 0;
                problemName_        = "RouteGeneration";

                try{
                    solutionType_ = new RouteGenSolutionType(this) ;
                    variableType_ = new Class[numberOfVariables_] ;
                    length_       = new int[numberOfVariables_];
                    variableType_[0] = Class.forName("jmetal.base.variable.ArrayInt") ;
                    variableType_[1] = Class.forName("jmetal.base.variable.ArrayInt") ;
                    variableType_[2] = Class.forName("jmetal.base.variable.ArrayInt") ;
                    variableType_[3] = Class.forName("jmetal.base.variable.Int") ;

                } catch(ClassNotFoundException e) {
                    System.out.println("class not found exceptiion");
                }


                Calendar cal = new GregorianCalendar();
                cal = Calendar.getInstance();
                System.out.println("starting program at "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));


                File folder = new File(baseFolder);

		zones = new HashMap<String, Zone>();
		areas = new ArrayList<Area>();
		edges = new ArrayList<Lane>();
                evalData = new EvaluationData();
		destinations = new Vector<Point2D.Double>();
		DefaultHandler h;
		ArrayList<Lane> Edges = new ArrayList<Lane>();

		outsideFlow = new double[stopHour];

		// -------------------------------------------------------------------
		// -------------------------- NET FILE -------------------------------

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
							zone.color = colorRES;
						} else if (landuse.equals("industrial")) {
							zone.type = ZoneType.INDUSTRIAL;
							zone.color = colorIND;
						} else if (landuse.equals("commercial")
								|| landuse.equals("retail")) {
							zone.type = ZoneType.COMMERCIAL;
							zone.color = colorCOM;
						}
					} else if (attributes.getValue("k").equals("shop")
							|| attributes.getValue("k").equals("amenity")) {
						zone.type = ZoneType.COMMERCIAL;
						zone.color = colorCOM;
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

		// ---------------- Read Areas from .areas.xml file -------------------
		//
                // this part is removed and we get area parameters from solution variables now


                // -------------------------------------------------------------------
		// ------------- Read Vehicles types from .veh.xml file --------------
		// 
		// -file example:
		/*
		 * <vtypes> <vtype id="porsche" accel="2" decel="7" sigma="0.6"
		 * length="4" maxspeed="50" color="1,0,0" /> <vtype id="206" accel="1.7"
		 * decel="6" sigma="0.5" length="4" maxspeed="40" color="0.4,1,0.4" />
		 * <vtype id="306" accel="1.7" decel="6" sigma="0.5" length="4.5"
		 * maxspeed="40" color="0.4,0,1" /> <vtype id="twingo" accel="1.4"
		 * decel="6" sigma="0.8" length="3.5" maxspeed="35" color="0.4,0.8,1" />
		 * <vtype id="cx" accel="1.1" decel="5" sigma="0.5" length="5"
		 * maxspeed="35" color="0.9,0.9,0.9" /> </vtypes>
		 */
		//
		// These types are used to generate vehicles, equally distributed.
		//
		//
		vtypes = new ArrayList<VType>();
		h = new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, qName, attributes);
				if (qName.equals("vtype")) {
					VType vt = new VType();
					vt.id = attributes.getValue(attributes.getIndex("id"));
					vt.accel = attributes
							.getValue(attributes.getIndex("accel"));
					vt.color = attributes
							.getValue(attributes.getIndex("color"));
					vt.decel = attributes
							.getValue(attributes.getIndex("decel"));
					vt.length = attributes.getValue(attributes
							.getIndex("length"));
					vt.maxspeed = attributes.getValue(attributes
							.getIndex("maxspeed"));
					vt.sigma = attributes
							.getValue(attributes.getIndex("sigma"));
					vtypes.add(vt);
				}
			}

		};
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
			parser.parse(new InputSource(baseFolder + baseName + ".veh.xml"));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
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
					currentFlow.next();
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

		graph = new SingleGraph("", false, true);
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
				parser
						.parse(new InputSource(baseFolder + baseName
								+ ".net.xml"));
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


		// ----------------------- POLYGONES
		File f = new File(baseFolder + baseName + ".shapes.xml");
		if (!f.exists()) {
			try {
				sr = new StreamResult(baseFolder + baseName + ".shapes.xml");
				xmlMain();

				tfh.startElement("", "", "shapes", ai);
				for (Zone z : zones.values()) {
					ai.clear();
					ai.addAttribute("", "", "id", "CDATA", z.id);
					ai.addAttribute("", "", "type", "CDATA", z.type.toString());
					ai.addAttribute("", "", "color", "CDATA", String.format(
							Locale.US, "%.2f,%.2f,%.2f",
							z.color.getRed() / 255.0,
							z.color.getGreen() / 255.0,
							z.color.getBlue() / 255.0));
					ai.addAttribute("", "", "fill", "CDATA", "1");
					ai.addAttribute("", "", "layer", "CDATA", "-1");
					ai.addAttribute("", "", "proba", "CDATA", String.format(
							Locale.US, "%.8f", z.probability));
					ai.addAttribute("", "", "surface", "CDATA", z.surface + "");

					String s = "";
					for (int i = 0; i < z.points.size(); i++) {
						s += String.format(Locale.US, "%.0f,%.0f", z.points
								.get(i).x, z.points.get(i).y);
						if (i != z.points.size() - 1)
							s += " ";
					}
					ai.addAttribute("", "", "shape", "CDATA", s);
					tfh.startElement("", "", "poly", ai);
					tfh.endElement("", "", "poly");
				}
				tfh.endElement("", "", "shapes");
				tfh.endDocument();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// -------------------------------------------------------------------
		// -------------- debuging Graphical User Interface ------------------

		if (gui) {
			ae = new AreasEditor(this);
			ae.run();
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
		
		
		int zone_count = 0;
		ArrayList<Zone> toRemove = new ArrayList<Zone>();


		String zone_info_filename = new String();
		int zcount = 0;
                for (Zone z : zones.values()) {
                            zone_count++;

                            if (z.type == ZoneType.RESIDENTIAL) {
                                    System.out.printf("Shortest path for INNER TRAFFIC (residential zones) %d over %d  %n", zone_count, zones.size());
                                    Path path = null;
                                    Node n = null;
                                    Dijkstra djk = null;

                                    int limit = 0;
                                    do {
                                            if (limit > 5) {
                                                    toRemove.add(z);
                                                    System.out.printf("zone %s should be removed.%n", z.id);
                                                    break;

                                            }
                                            Point2D.Double point = pointInZone(z);
                                            n = getClosestNode(point);
                                            djk = new Dijkstra(Dijkstra.Element.node, "weight", n
                                                            .getId());
                                            djk.init(graph);
                                            djk.compute();
                                            // a reference node that ensures this zone can reach the network.
                                            path = djk.getShortestPath(graph.getNode("9647221"));
                                            limit++;
                                    } while (path.empty());
                                    z.shortestPath = djk.getParentEdgesString();
                                    z.sourceNode = n;
                                    djk = null;

                            }
                    }

                    for (Zone z : toRemove) {
                            zones.remove(z.id);
                    }

                    int count = 0;
                    cal = Calendar.getInstance();
                    System.out.println("starting filling zones reference nodes at "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND) + "...");
                    for (Zone z : zones.values()) {
                        count++;
                        fillZoneNodes(z);
                    }
                    System.out.println("done.");



	}

        //does the flow generation process
        public void datamain() {

            int[] flowCounter = new int[stopHour+1];
            for(Loop nextLoop : loops){
                for (Flow nextFlow: nextLoop.flows) {
                    if(nextFlow.hour>stopHour) continue;
                    String path;
                    int cnt = 0;
                    nextFlow.reset();
                    while ((path=nextFlow.goOne())!=null){
                        cnt++;
                        for(EvaluationPoint ev: evalData.points){
                            ev.updateEstimate(path, nextFlow.hour);
                        }
                        //inside flow
                        if (Math.random() < insideFlowRatio) {
                            path = goInsideFlow();
                            if (path!=null) {
                                for(EvaluationPoint ev: evalData.points){
                                    ev.updateEstimate(path, nextFlow.hour);
                                }
                            }
                        }
                    }
                }
            }

            //showing the results of counting flows on control points:
            for(EvaluationPoint ev: evalData.points){
                System.out.println(ev);
            }

        }


	private String goInsideFlow() {
		// at currentTime

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
			//System.out.printf("zone is NULL in goInsideFlow !!! rand=%f sum=%f%n", rand,sum);
			return(null);
		}
		String p = null;
		int count = 0;
		do {
			if (count > 10) {
				System.out.println("infinite loop on zone " + zone.id);
				return(null);
			}
			p = createRandomPath(zone.shortestPath, zone.sourceNode);
			count++;

		} while (p == null);
                return(p);
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

        public void fillZoneNodes(Zone z){
                for (int i=0;i<5;i++){
                    Point2D.Double point = pointInZone(z);
                    z.near_nodes.add(getClosestNode(point));
                }
        }


        public void evaluate(Solution solution) {
            
            System.out.println("evaluation starts...");
            double fitness = 0;
            Area a = null;

            areas.clear();
            for(Zone z : zones.values()){
                z.area = null;
            }

            Variable[] vars = solution.getDecisionVariables();
            jmetal.base.variable.ArrayInt typeAr = (jmetal.base.variable.ArrayInt)(vars[1]);
            jmetal.base.variable.ArrayInt areaAr = (jmetal.base.variable.ArrayInt)(vars[0]);

            //filling area type probabilites :
            //

            try{
                ZoneType.RESIDENTIAL.probability = (double)typeAr.getValue(0)/100;
                ZoneType.COMMERCIAL.probability  = (double)typeAr.getValue(1)/100;
                ZoneType.INDUSTRIAL.probability  = (double)typeAr.getValue(2)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }


            //filling area parameters
            //
            Area defaultAreaRES = new Area();
            try{
                defaultAreaRES.probability = (double)areaAr.getValue(0)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            defaultAreaRES.type = ZoneType.RESIDENTIAL;
            areas.add(defaultAreaRES);

            a = new Area();
            a.id = "R1";
            a.x = 19900;
            a.y = 14500;
            a.radius = 3400;
            try{
                a.probability = (double)areaAr.getValue(1)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            a.type = ZoneType.RESIDENTIAL;
            a.color = colorCOM_light;
            areas.add(a);


            Area defaultAreaCOM = new Area();
            try{
                defaultAreaCOM.probability = (double)areaAr.getValue(2)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            defaultAreaCOM.type = ZoneType.COMMERCIAL;
            areas.add(defaultAreaCOM);

            a = new Area();
            a.id = "C1";
            a.x = 20418;
            a.y = 14500;
            a.radius = 1500;
            try{
                a.probability = (double)areaAr.getValue(3)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            a.type = ZoneType.COMMERCIAL;
            a.color = colorCOM_light;
            areas.add(a);

            a = new Area();
            a.id = "C2";
            a.x = 22418;
            a.y = 16700;
            a.radius = 1500;
            try{
                a.probability = (double)areaAr.getValue(4)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            a.type = ZoneType.COMMERCIAL;
            a.color = colorCOM_light;
            areas.add(a);

            a = new Area();
            a.id = "C3";
            a.x = 17000;
            a.y = 15200;
            a.radius = 1000;
            try{
                a.probability = (double)areaAr.getValue(5)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            a.type = ZoneType.COMMERCIAL;
            a.color = colorCOM_light;
            areas.add(a);



            Area defaultAreaIND = new Area();
            try{
                defaultAreaIND.probability = (double)areaAr.getValue(6)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            defaultAreaIND.type = ZoneType.INDUSTRIAL;
            areas.add(defaultAreaIND);

            a = new Area();
            a.id = "I1";
            a.x = 20300;
            a.y = 14000;
            a.radius = 6000;
            try{
                a.probability = (double)areaAr.getValue(7)/100;
            }catch(JMException e){
                System.out.println("JME exception");
            }
            a.type = ZoneType.INDUSTRIAL;
            a.color = colorCOM_light;
            areas.add(a);


            insideFlowRatio = (double)((jmetal.base.variable.Int)vars[3]).getValue()/100;

                // -------------------------------------------------------------------
                // ------------- Compute Probabilities for each zone -----------------

            // compute which areas on which zone.
            for (Zone z : zones.values()) {
                for (Area a1 : areas) {
                   if (z.area == null && a1.type == z.type) {
                        for (Point2D.Double p : z.points) {
                            if (a1.radius > euclideanDistance(a1.x, a1.y, p.x, p.y)) {
                                //System.out.println(z.id + " in area" + a.id);
                                z.area = a1;
                                break;
                            }
                        }
                    }

                }
            }

                // compute proba of zones
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

            for (Zone z : zones.values()) {
                    z.probability = (z.surface / z.area.sumSurfaceZones)
                                    * z.type.probability * z.area.probability;
                    //System.out.printf(" surface: %.0f proba: %.8f Zone: %s%n",z.surface, z.probability, z.id);
            }

            evalData.resetPoints();
            datamain();
            evalData.updateFitness();
            fitness = evalData.sumOfDifference;
            //fitness = evalData.meanOfDifferencePercent;
            System.out.println("Sum:"+evalData.sumOfDifference+" F1:"+evalData.sumOfDifferenceF1+" D%:"+evalData.meanOfDifferencePercent);
            System.out.println("Res:"+ZoneType.RESIDENTIAL.probability+" Com:"+ZoneType.COMMERCIAL.probability+" Ind:"+ZoneType.INDUSTRIAL.probability);
            try{
                System.out.println("Res Probs:"+areaAr.getValue(0)+" "+areaAr.getValue(1));
                System.out.println("Com Probs:"+areaAr.getValue(2)+" "+areaAr.getValue(3)+" "+areaAr.getValue(4)+" "+areaAr.getValue(5));
                System.out.println("Ind Probs:"+areaAr.getValue(6)+" "+areaAr.getValue(7));
                System.out.println("Inside flow ratio(percent):"+ ((jmetal.base.variable.Int)vars[3]).getValue());
            }catch(JMException e){

            }
            solution.setObjective(0, fitness);
            solution.setFitness(fitness);
        }

        public int getAreaTypeNum(){
            return areaTypes.size();
        }

        public void addAreaType(String name, int parts){
            areaTypes.add(new AreaType(name,parts));
        }

        public int getAreaParts(int index){
            return areaTypes.get(index).areaParts;
        }

}

