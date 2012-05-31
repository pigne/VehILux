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
import java.util.Locale;

import org.graphstream.algorithm.Dijkstra;
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

	public Zone() {
		points = new ArrayList<Point2D.Double>();
        near_nodes = new ArrayList<Node>();
	}

	public String toString() {
		String s = new String();
		s = String.format(
						Locale.US,
						"Zone %s:%n  -type: %s%n  -surface: %.15f%n  -boundaries: (%f,%f) (%f,%f)%n  -points: [",
						this.id, this.type, this.surface, this.min_x_boundary,
						this.min_y_boundary, this.max_x_boundary,
						this.max_y_boundary);
		for (Point2D.Double p : this.points) {
			s += String.format(Locale.US, "(%.4f,%.4f) ", p.x, p.y);
		}
		s += "]";
		return s;
	}
}
