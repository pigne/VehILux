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

import java.util.TreeSet;

/**
 * 
 */
public class Loop  implements Comparable<Loop>  {
	public int nextTime;
	String id;
	String edge;
	TreeSet<Flow> flows = new TreeSet<Flow>();

	String dijkstra = null;

	@Override
	public String toString() {
		String fl = "";
		for (Flow f : flows) {
			fl += "\t" + f.toString() + "\n";
		}
		return String.format("Loop %s on edge %s,  flows:%n%s", id, edge, fl);
	}

	public int compareTo(Loop l) {
		if (l == this)
			return 0;
		else if (this.flows.first().next < l.flows.first().next)
			return -1;
		else
			// if (this.flows.first().next > l.flows.first().next)
			return 1;
		// else
		// return 0;
	}
}
