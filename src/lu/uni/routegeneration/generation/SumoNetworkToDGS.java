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

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *  Utility class for the SUMO Route Generator.
 * 
 * Takes a .net.xml Sumo network file as a parameter and converts it of a DGS
 * file.
 * 
 */
public class SumoNetworkToDGS extends DefaultHandler {
	Graph g;
	ConnectedComponents cc;
	FileSink fs;
	private Node currentNode = null;

	String currentLane = null;
	private String styleSheet = "graph { padding: 60px; fill-color:#eeeeee;}"
			+ "node { z-index:3; size: 1px; fill-color: #777777; }"
			+ "node.internal{ fill-color: #BB4444; }"
			+ "edge  { fill-color: #404040; size: 1px;}"
			+ "sprite {text-style:bold; text-color: #555555;  fill-color:#eeeeee; }"
			+ "edge.path {fill-color: #ff4040;}";

	private String baseName;
	private String baseFolder;

	/**
	 * Main class only for testing purposes.
	 */
	public static void main(String[] args){
		System.out.print("Generating the DGS file...");
		SumoNetworkToDGS netParser = new SumoNetworkToDGS(args[0],
				args[1]);
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(netParser);
			parser
					.parse(new InputSource(args[0] +"/"+args[1]
							+ ".net.xml"));
			System.out.println("OK");
		} catch (Exception ex) {
			System.out.println("ERROR");
			ex.printStackTrace(System.err);
		}

	}
	
	public SumoNetworkToDGS(String folderName, String baseName) {
		this.baseFolder = folderName;
		this.baseName = baseName;
	}

	@Override
	public void startDocument() throws SAXException {
		g = new SingleGraph("Dual", false, true);
		g.addAttribute("copyright", "(c) 2010-2011 University of Luxembourg");
		g.addAttribute("author", "Yoann Pigné");
		g.addAttribute("information", "http://yoann.pigne.org");

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("edge")) {
			if (!attributes.getValue("function").equals("internal")) {
				String id = attributes.getValue("id");
				currentNode = g.getNode(id);
				if (currentNode == null) {
					currentNode = g.addNode(id);
					currentNode.addAttribute("label", id);
				} else {
					System.out.println("Problem");
				}
			}
		} else if (qName.equals("lane")) {
			if (currentNode != null) {

				String maxspeed = attributes.getValue(attributes
						.getIndex("maxspeed"));
				String length = attributes.getValue(attributes
						.getIndex("length"));
				String shape = attributes
						.getValue(attributes.getIndex("shape"));
				String firstPoint = shape.split(" ")[0];
				currentNode.addAttribute("x", Double.parseDouble(firstPoint
						.split(",")[0]));
				currentNode.addAttribute("y", Double.parseDouble(firstPoint
						.split(",")[1]));
				if (maxspeed != null && length != null) {
					double weight = Double.parseDouble(length)
							/ Double.parseDouble(maxspeed);
					currentNode.addAttribute("weight", weight);
					currentNode = null;
				}
			}
		} else if (qName.equals("succ")) {
			currentLane = attributes.getValue("lane");
			String id = attributes.getValue("edge");
			currentNode = g.getNode(id);
			if (currentNode == null) {
				currentNode = g.addNode(id);
				currentNode.addAttribute("label", id);
			}

		} else if (qName.equals("succlane")) {
			String lane = attributes.getValue("lane");
			String otherNodeId = lane.split("_")[0];
			if (!otherNodeId.equals("SUMO")) {
				Node otherNode = g.getNode(otherNodeId);
				if (otherNode == null) {
					otherNode = g.addNode(otherNodeId);
					otherNode.addAttribute("label", otherNodeId);
				}
				Edge link = currentNode.getEdgeToward(otherNodeId);
				if (link == null) {
					g.addEdge(currentNode.getId() + "_" + otherNode.getId(),
							currentNode.getId(), otherNode.getId(), true);
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("edge") || qName.equals("succ"))
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
		for(Node n : g.getEachNode()){
			if(! nodes.contains(n)){
				toRemove.add(n);
			}
			Object o = n.getAttribute("weight");
			for(Edge e :n.getEachLeavingEdge()){
				e.addAttribute("weight", o);
			}
		}
		for(Node n : toRemove){
			g.removeNode(n.getId());
		}
		
		String f = baseFolder + baseName + ".dgs";
		try {
			g.write(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
