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

import javax.swing.JPanel;

import lu.uni.routegeneration.generation.Area;
import lu.uni.routegeneration.generation.Lane;
import lu.uni.routegeneration.generation.Zone;

/**
 * 
 */
public class EditorPanel extends JPanel {

	AreasEditor window;

	public EditorPanel(AreasEditor window) {
		this.window = window;

	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		// Enable antialiasing for shapes
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		double orig_width = getWidth();
		double orig_height = getHeight();
		double pixelRatio = orig_width / orig_height;
		double mapRatio = (window.max_x_boundary - window.min_x_boundary)
				/ (window.max_y_boundary - window.min_y_boundary);
		double usefull_width;
		double usefull_height;
		if (mapRatio > pixelRatio) {
			usefull_width = orig_width;
			usefull_height = orig_width / mapRatio;

		} else {
			usefull_height = orig_height;
			usefull_width = orig_height * mapRatio;

		}

		g2.setColor(Color.gray);
		g2.drawRect((int) (orig_width / 2 - usefull_width / 2),
				(int) (orig_height / 2 - usefull_height / 2),
				(int) usefull_width - 1, (int) usefull_height - 1);

		double ratioX = usefull_width
				/ (window.max_x_boundary - window.min_x_boundary);
		// System.out.println("ratioX="+ratioX);

		double ratioY = usefull_height
				/ (window.max_y_boundary - window.min_y_boundary);
		// System.out.println("ratioY="+window.max_y_boundary);

		for (Zone z : window.rg.zones.values()) {
			int[] xs = new int[z.points.size()];
			int[] ys = new int[z.points.size()];
			for (int i = 0; i < z.points.size(); i++) {
				xs[i] = (int) ((z.points.get(i).x - window.min_x_boundary)
						* ratioX + orig_width / 2 - usefull_width / 2);
				ys[i] = (int) (orig_height - ((z.points.get(i).y - window.min_y_boundary)
						* ratioY + orig_height / 2 - usefull_height / 2));
				// System.out.println(xs[i]+", "+ys[i]);

			}
			g2.setColor(z.color);

			g2.fillPolygon(xs, ys, z.points.size());


		}

		for (Area a : window.rg.areas) {
			if (a.id != null) {
				int x = (int) ((a.x - a.radius - window.min_x_boundary)
						* ratioX + orig_width / 2.0 - usefull_width / 2.0);
				int y = (int) (orig_height - ((a.y + a.radius - window.min_y_boundary)
						* ratioY + orig_height / 2.0 - usefull_height / 2.0));
				int w = (int) (a.radius * 2 * ratioX);
				int h = (int) (a.radius * 2 * ratioY);

				g2.setColor(a.color);
				g2.fillOval(x, y, w, h);
				g2.setColor(a.color.LIGHT_GRAY);
				g2.drawOval(x, y, w, h);
			}
		}

		g2.setColor(Color.gray);
		for (Lane e : window.rg.edges) {
			int[] xs = new int[e.shape.size()];
			int[] ys = new int[e.shape.size()];
			for (int i = 0; i < e.shape.size(); i++) {
				xs[i] = (int) ((e.shape.get(i).x - window.min_x_boundary)
						* ratioX + orig_width / 2 - usefull_width / 2);
				ys[i] = (int) (orig_height - ((e.shape.get(i).y - window.min_y_boundary)
						* ratioY + orig_height / 2 - usefull_height / 2));
			}
			g2.drawPolyline(xs, ys, e.shape.size());
		}

		g2.setColor(Color.red);
		for (Point2D.Double e : window.destinations) {

			g2.drawRect((int) ((e.x - window.min_x_boundary) * ratioX
					+ orig_width / 2 - usefull_width / 2),
					(int) (orig_height - ((e.y - window.min_y_boundary)
							* ratioY + orig_height / 2 - usefull_height / 2)),
					1, 1);
		}

		/*
		g2.setColor(new Color(0, 0, 0, 10));

		for (Zone z : window.rg.zones.values()) {
			int x;
			int y;
			x = (int) ((z.points.get(0).x - window.min_x_boundary) * ratioX
					+ orig_width / 2 - usefull_width / 2);
			y = (int) (orig_height - ((z.points.get(0).y - window.min_y_boundary)
					* ratioY + orig_height / 2 - usefull_height / 2));

			// g2.drawString(String.format("%s / %.0f / %.6f", z.id, z.surface,
			// z.probability), x, y);
			g2.drawString(String.format("%s", z.id), x, y);

		}
		*/
	}
}
