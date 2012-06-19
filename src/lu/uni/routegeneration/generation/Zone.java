/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Zone.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import lu.uni.routegeneration.helpers.MathHelper;

import org.graphstream.graph.Node;

/**
 * 
 */
public class Zone {
	public String id;
	public ZoneType type = null;
	public Color color;
	public double min_x_boundary;
	public double min_y_boundary;
	public double max_x_boundary;
	public double max_y_boundary;
	public ArrayList<Point2D.Double> points;
	public ArrayList<Node> near_nodes;
    public double surface;
	public double probability;
	public Area area = null;
	public String shortestPath;
	public Node sourceNode;
	private Random random;

	public Zone() {
		random = new Random(System.currentTimeMillis());
		points = new ArrayList<Point2D.Double>();
        near_nodes = new ArrayList<Node>();
	}

	public String toString() {
		String s = new String();
		s = String.format(
						Locale.US,
						"Zone %s:%n  -type: %s%n  -surface: %.15f%n   -probability: %.5f%n  -boundaries: (%f,%f) (%f,%f)%n  -points: [",
						this.id, this.type, this.surface, this.probability,
						this.min_x_boundary, this.min_y_boundary, this.max_x_boundary,
						this.max_y_boundary);
		for (Point2D.Double p : this.points) {
			s += String.format(Locale.US, "(%.4f,%.4f) ", p.x, p.y);
		}
		s += "]";
		return s;
	}
	
	public Node getDestinationNode() {
		if (near_nodes == null || near_nodes.size() == 0) {
			return null;
		}
		int randNode = (int) (random.nextDouble() * near_nodes.size());
		return near_nodes.get(randNode);
	}
	
	/**
	 * @param zone
	 * @return a random node by a random point(x,y) in the zone
	 */
	public Node getRandomNode(Iterator<? extends Node> nodes) {
		Point2D.Double point = getRandomPoint();
		return getClosestNode(point, nodes);
	}
	
	private Point2D.Double getRandomPoint() {
		Point2D.Double point = new Point2D.Double();
		do {
			point.x = random.nextDouble()
					* (max_x_boundary - min_x_boundary)
					+ min_x_boundary;
			point.y = random.nextDouble()
					* (max_y_boundary - min_y_boundary)
					+ min_y_boundary;
		} while (!contains(point));
		return point;
	}
	
	private boolean contains(Point2D.Double point) {
		Point2D.Double other = new Point2D.Double(max_x_boundary, point.y);
		int n = 0;
		for (int i = 0; i < points.size() - 1; i++) {
			if (MathHelper.intersect(point, other, points.get(i), points.get(i + 1))) {
				n++;
			}
		}
		return n % 2 == 1;
	}
	
	private static Node getClosestNode(Point2D.Double p, Iterator<? extends Node> nodes) {
		Node closestNode = nodes.next();
		double closestX = (Double) closestNode.getAttribute("x");
		double closestY = (Double) closestNode.getAttribute("y");
		double closestDist = Math.sqrt(Math.pow(closestX - p.x, 2.0) + Math.pow(closestY - p.y, 2.0));
		while (nodes.hasNext()) {
			Node currentNode = nodes.next();
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
}
