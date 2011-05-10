/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Lane.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * 
 */
public class Lane {
	public ArrayList<Point2D.Double> shape;

	public Lane() {
		shape = new ArrayList<Point2D.Double>();
	}
}
