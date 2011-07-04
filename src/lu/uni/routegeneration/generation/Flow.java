/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Flow.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import org.graphstream.algorithm.Dijkstra;
import org.xml.sax.SAXException;

/**
 * 
 */
public class Flow   {
	private static final int CAR = 0;
	private static final int TRUCK = 1;
	int hour;
	int car;
	int truck;
	Loop loop;
	double next = -1;
	private int nextVehicle;
	private int T = 0;
	private int C = 0;
	RouteGeneration rg;
	double truckT = 0;
	double carT = 0;

	public Flow(RouteGeneration rg) {
		this.rg = rg;

	}

	@Override
	public String toString() {
		return String.format("hour: %d, car: %d, truck: %d", hour, car, truck);
	}

       
	
		
       
	
}
