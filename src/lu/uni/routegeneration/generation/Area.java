/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Area.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import java.awt.Color;
import java.util.ArrayList;

/**
 * 
 */
public class Area {
	public Area() {
		zones = new ArrayList<Zone>();
	}

	public String id = null;
	public ZoneType type = null;
	public Color color;
	public double x;
	public double y;
	public double radius;
	double probability;
	public ArrayList<Zone> zones;
	public double sumSurfaceZones = 0.0;
}
