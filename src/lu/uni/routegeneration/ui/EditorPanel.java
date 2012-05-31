/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file EditorPanel.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import org.graphstream.graph.Node;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;

import lu.uni.routegeneration.generation.Area;
import lu.uni.routegeneration.ui.Lane;
import lu.uni.routegeneration.generation.Zone;

/**
 * 
 */
public class EditorPanel extends JPanel {

	double min_x_boundary = Double.MAX_VALUE;
	double min_y_boundary = Double.MAX_VALUE;
	double max_x_boundary = Double.MIN_VALUE;
	double max_y_boundary = Double.MIN_VALUE;
	Graphics2D g2;
	double ratioX;
	double ratioY;
	double orig_width;
	double orig_height;
	double usefull_width;
	double usefull_height;
	double mapRatio;
	
	// data to display
	private HashMap<String, Zone> zones = new HashMap<String, Zone>();
	private ArrayList<Lane> edges = new ArrayList<Lane>();
	private ArrayList<Area> areas = new ArrayList<Area>();
	private ArrayList<Point2D.Double> sources = new ArrayList<Point2D.Double>();
	private ArrayList<Point2D.Double> destinations = new ArrayList<Point2D.Double>();
	private int step = 0;
	private ArrayList<Color> colors = new ArrayList<Color>();
	
	public ArrayList<Point2D.Double> getSources() {
		return sources;
	}

	private ArrayList<Point2D.Double> nodesToPoints(ArrayList<Node> nodes) {
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for (Node node : nodes) {
			if (node != null) {
				double x = node.getAttribute("x");
				double y = node.getAttribute("y");
				points.add(new Point2D.Double(x, y));
			}
		}
		return points;
	}
	
	public void setSources(ArrayList<Node> sources) {
		this.sources = nodesToPoints(sources);
	}

	public ArrayList<Point2D.Double> getDestinations() {
		return destinations;
	}

	public void setDestinations(ArrayList<Point2D.Double> destinations) {
		this.destinations = destinations;
	}

	public ArrayList<Area> getAreas() {
		return areas;
	}

	public void setAreas(ArrayList<Area> areas) {
		this.areas = areas;
	}

	public HashMap<String, Zone> getZones() {
		return zones;
	}

	public void setZones(HashMap<String, Zone> zones) {
		this.zones = zones;
	}

	public ArrayList<Lane> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<Lane> edges) {
		this.edges = edges;
	}


	//graph.addAttribute("ui.stylesheet", styleSheet);
	// ------ For a graphical output of the graph (very slow...)
	// graph.addAttribute("ui.antialias");
	// graph.display(false);
	
	
	private String styleSheet = "graph { padding: 60px; fill-color:#eeeeee;}"
			+ "node { z-index:3; size: 1px; fill-color: #777777; }"
			+ "node.internal{ fill-color: #BB4444; }"
			+ "edge  { fill-color: #404040; size: 1px;}"
			+ "sprite {text-style:bold; text-color: #555555;  fill-color:#eeeeee; }"
			+ "edge.path {fill-color: #ff4040;}";
	

	public EditorPanel(double min_x_boundary, double min_y_boundary, double max_x_boundary, double max_y_boundary) {
		this.min_x_boundary = min_x_boundary;
		this.min_y_boundary = min_y_boundary;
		this.max_x_boundary = max_x_boundary;
		this.max_y_boundary = max_y_boundary;
		mapRatio = (max_x_boundary - min_x_boundary) / (max_y_boundary - min_y_boundary);
		colors.add(new Color(255,20,11));
		colors.add(Color.red);
		colors.add(Color.magenta);
		colors.add(Color.yellow);
		colors.add(Color.orange);
	}

	@Override
	public void paintComponent(Graphics g) {
		g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		orig_width = getWidth();
		orig_height = getHeight();
		double pixelRatio = orig_width / orig_height;
		if (mapRatio > pixelRatio) {
			usefull_width = orig_width;
			usefull_height = orig_width / mapRatio;
		} 
		else {
			usefull_height = orig_height;
			usefull_width = orig_height * mapRatio;
		}

		g2.setColor(Color.gray);
		g2.drawRect((int) (orig_width / 2 - usefull_width / 2), (int) (orig_height / 2 - usefull_height / 2), (int) usefull_width - 1, (int) usefull_height - 1);

		ratioX = usefull_width / (max_x_boundary - min_x_boundary);
		ratioY = usefull_height / (max_y_boundary - min_y_boundary);
		
		drawZones();
		//drawAreas();

		drawPoints(sources, colors.get(step));
	}
	
	public void drawPoint(Point2D.Double point, Color color) {
		g2.setColor(color);
		g2.fillOval(
				(int) ((point.x - min_x_boundary) * ratioX + orig_width / 2 - usefull_width / 2),
				(int) (orig_height - ((point.y - min_y_boundary) * ratioY + orig_height / 2 - usefull_height / 2)),
				5, 5);
	}
	
	public void generateSnapshot(String path) {
		PDFGraphics2D g = new PDFGraphics2D(0.0, 0.0, getWidth(), getHeight());
		g.setFontRendering(PDFGraphics2D.FontRendering.VECTORS);
		paint(g);
		step = (++step)%colors.size();
		try {
			FileOutputStream ff = new FileOutputStream(path);
			try {
				ff.write(g.getBytes());
			} finally {
				ff.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void drawZones() {
		for (Zone z : zones.values()) {
			int[] xs = new int[z.points.size()];
			int[] ys = new int[z.points.size()];
			for (int i = 0; i < z.points.size(); i++) {
				xs[i] = (int) ((z.points.get(i).x - min_x_boundary) * ratioX + orig_width / 2 - usefull_width / 2);
				ys[i] = (int) (orig_height - ((z.points.get(i).y - min_y_boundary) * ratioY + orig_height / 2 - usefull_height / 2));
			}
			g2.setColor(z.color);
			g2.fillPolygon(xs, ys, z.points.size());
		}
	}
	
	public void drawZones2() {
		g2.setColor(new Color(0, 0, 0, 10));
		for (Zone z : zones.values()) {
			int x;
			int y;
			x = (int) ((z.points.get(0).x - min_x_boundary) * ratioX + orig_width / 2 - usefull_width / 2);
			y = (int) (orig_height - ((z.points.get(0).y - min_y_boundary) * ratioY + orig_height / 2 - usefull_height / 2));
			g2.drawString(String.format("%s", z.id), x, y);
		}
	}
	
	public void drawAreas() {
		for (Area area : areas) {
			if (area.getId() != null) {
				int x = (int) ((area.getX() - area.getRadius() - min_x_boundary) * ratioX + orig_width / 2.0 - usefull_width / 2.0);
				int y = (int) (orig_height - ((area.getY() + area.getRadius() - min_y_boundary) * ratioY + orig_height / 2.0 - usefull_height / 2.0));
				int w = (int) (area.getRadius() * 2 * ratioX);
				int h = (int) (area.getRadius() * 2 * ratioY);
				g2.setColor(area.getColor());
				g2.fillOval(x, y, w, h);
				g2.setColor(area.getColor());
				g2.drawOval(x, y, w, h);
			}
		}
	}
	
	public void drawPoints(ArrayList<Point2D.Double> points, Color color) {
		for (Point2D.Double point : points) {
			drawPoint(point, color);
		}
	}
	
	public void drawEdges() {
		g2.setColor(new Color(150, 150, 150, 140));
		for (Lane edge : edges) {
			int[] xs = new int[edge.shape.size()];
			int[] ys = new int[edge.shape.size()];
			for (int i = 0; i < edge.shape.size(); i++) {
				xs[i] = (int) ((edge.shape.get(i).x - min_x_boundary) * ratioX + orig_width / 2 - usefull_width / 2);
				ys[i] = (int) (orig_height - ((edge.shape.get(i).y - min_y_boundary) * ratioY + orig_height / 2 - usefull_height / 2));
			}
			g2.drawPolyline(xs, ys, edge.shape.size());
		}
	}
}
