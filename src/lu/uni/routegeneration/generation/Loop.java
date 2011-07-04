/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Loop.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import java.util.HashSet;
import java.util.TreeSet;

import org.graphstream.algorithm.Dijkstra;

/**
 * 
 */
public class Loop  {
	public int nextTime;
	String id;
	String edge;
	HashSet<Flow> flows = new HashSet<Flow>();

	String dijkstra = null;

	@Override
	public String toString() {
		String fl = "";
		for (Flow f : flows) {
			fl += "\t" + f.toString() + "\n";
		}
		return String.format("Loop %s on edge %s,  flows:%n%s", id, edge, fl);
	}

	
}
