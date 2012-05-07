/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file SumoNetworkToDGS.java
 * @date Nov 5, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.util.SingletonException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Utility class for the SUMO Route Generator.
 * 
 * Takes a .net.xml Sumo network file as a parameter and converts it of a DGS
 * file.
 * 
 */
public class SumoNetworkToDGS extends DefaultHandler {
	Graph g;
	ConnectedComponents cc;
	FileSink fs;
	TreeSet<String> junctions;
	TreeSet<String> tls;
	private Node currentNode = null;
	boolean firstPass = true;
	String currentLane = null;
	private String styleSheet = "graph { padding: 60px; fill-color:#eeeeee;}"
			+ "node { z-index:3; size: 1px; fill-color: #777777; }"
			+ "node.internal{ fill-color: #BB4444; }"
			+ "edge  { fill-color: #404040; size: 1px;}"
			+ "sprite {text-style:bold; text-color: #555555;  fill-color:#eeeeee; }"
			+ "edge.path {fill-color: #ff4040;}";

	private String baseName;
	private String folderName;
	private double JUNCTION_COST = 0;
	private double TLS_COST = 0;

	public SumoNetworkToDGS(String folderName, String baseName) {
		this.folderName = folderName;
		this.baseName = baseName;
	}

	@Override
	public void startDocument() throws SAXException {
		g = new MultiGraph("Dual", false, true);
		g.addAttribute("copyright", "(c) 2010-2011 University of Luxembourg");
		g.addAttribute("author", "Yoann Pigné");
		g.addAttribute("information", "http://yoann.pigne.org");
		
		junctions = new TreeSet<String>();
		tls = new TreeSet<String>();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (qName.equals("edge")) {
			if (attributes.getValue("function") == null || !attributes.getValue("function").equals("internal")) {
				String id = attributes.getValue("id");
				currentNode = g.getNode(id);
				if (currentNode == null) {
					currentNode = g.addNode(id);
					currentNode.addAttribute("label", id);
				} 
				else {
					System.out.println("Problem");
				}
			}
		} else if (qName.equals("lane")) {
			if (currentNode != null) {
				String maxspeed = attributes.getValue(attributes.getIndex("speed"));
				String length = attributes.getValue(attributes.getIndex("length"));
				String shape = attributes.getValue(attributes.getIndex("shape"));
				String firstPoint = shape.split(" ")[0];
				currentNode.addAttribute("x", Double.parseDouble(firstPoint.split(",")[0]));
				currentNode.addAttribute("y", Double.parseDouble(firstPoint.split(",")[1]));
				if (maxspeed != null && length != null) {
					double weight = Double.parseDouble(length) / Double.parseDouble(maxspeed);
					currentNode.addAttribute("weight", weight);
					currentNode = null;
				}
			}
		} 
//		else if (qName.equals("succ")) {
//			currentLane = attributes.getValue("lane");
//			String id = attributes.getValue("edge");
//			String junction = attributes.getValue("junction");
//			currentNode = g.getNode(id);
//			if (currentNode == null) {
//				currentNode = g.addNode(id);
//				currentNode.addAttribute("label", id);
//			}
//			currentNode.addAttribute("junction", junction);
//		} 
//		else if (qName.equals("succlane")) {
//			String lane = attributes.getValue("lane");
//			String otherNodeId = lane.split("_")[0];
//			if (!otherNodeId.equals("SUMO")) {
//				Node otherNode = g.getNode(otherNodeId);
//				if (otherNode == null) {
//					otherNode = g.addNode(otherNodeId);
//					otherNode.addAttribute("label", otherNodeId);
//				}
//				Edge link = currentNode.getEdgeToward(otherNodeId);
//				if (link == null) {
//					Edge e2 = otherNode.getEdgeToward(currentNode.getId());
//					if (e2 != null) {
//						System.out.printf(" !!! opposite-direction edge already exists between %s and %s !!!%n",currentNode.getId(), otherNodeId);
//					}
//					g.addEdge(currentNode.getId() + "_" + otherNode.getId(),
//							currentNode.getId(), otherNode.getId(), true);
//
//				}
//			}
//		} 
		else if (qName.equals("connection")) {
			String id = attributes.getValue("from");
			currentNode = g.getNode(id);
			if (currentNode == null) {
				currentNode = g.addNode(id);
				currentNode.addAttribute("label", id);
			}
			String otherNodeId = attributes.getValue("to");
			Node otherNode = g.getNode(otherNodeId);
			if (otherNode == null) {
				otherNode = g.addNode(otherNodeId);
				otherNode.addAttribute("label", otherNodeId);
			}
			Edge link = currentNode.getEdgeToward(otherNodeId);
			if (link == null) {
				Edge e2 = otherNode.getEdgeToward(currentNode.getId());
				if (e2 != null) {
					System.out.printf(" !!! opposite-direction edge already exists between %s and %s !!!%n", currentNode.getId(), otherNodeId);
				}
				g.addEdge(currentNode.getId() + "_" + otherNode.getId(), currentNode.getId(), otherNode.getId(), true);
			}
		} 
		else if (qName.equals("junction")) {
			String id = attributes.getValue("id");
			String incLanes = attributes.getValue("incLanes");
			TreeSet<String> lanes = new TreeSet<String>();
			for (String s : incLanes.split(" ")) {
				lanes.add(s.split("_")[0]);
			}
			if (lanes.size() > 1) {
				junctions.add(id);
			}
		} 
		else if (qName.equals("tLogic")) {
			String id = attributes.getValue("id");
			tls.add(id);
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("edge") || qName.equals("connection"))
			currentNode = null;
	}

	@Override
	public void endDocument() throws SAXException {

		// remove unsusable connected components
		ConnectedComponents cc = new ConnectedComponents(g);
		cc.compute();
		List<Node> nodes = cc.getGiantComponent();
		g.removeSink(cc);
		ArrayList<Node> toRemove = new ArrayList<Node>();
		for (Node n : g.getEachNode()) {
			if (!nodes.contains(n)) {
				toRemove.add(n);
			}

		}
		for (Node n : toRemove) {
			g.removeNode(n.getId());
		}

		// penalty for junctions
		for (Node n : g) {
			String id = n.getAttribute("junction");
			
			if (id != null && junctions.contains(id)) {
				n.setAttribute("weight", n.getNumber("weight") + JUNCTION_COST);
			}
			if (id != null && tls.contains(id)) {
				n.setAttribute("weight", n.getNumber("weight") + TLS_COST);
			}
			Object o = n.getAttribute("weight");
			for (Edge e : n.getEachLeavingEdge()) {
				e.addAttribute("weight", o);
			}
		}

		String f = folderName + baseName + ".dgs";
		try {
			g.write(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
