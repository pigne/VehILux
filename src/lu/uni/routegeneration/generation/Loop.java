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
	
	private String id;
	private String edge;
	private TreeSet<Flow> flows;
	private String dijkstra = null;
	private double totalFlow;
	
	public double getTotalFlow() {
		return totalFlow;
	}

	public String getDijkstra() {
		return dijkstra;
	}

	public void setDijkstra(String dijkstra) {
		this.dijkstra = dijkstra;
	}

	public String getId() {
		return id;
	}

	public String getEdge() {
		return edge;
	}

	public TreeSet<Flow> getFlows() {
		return flows;
	}


	public Loop(String id, String edge) {
		this.id = id;
		this.edge = edge;
		this.flows = new TreeSet<Flow>();
	}
	
	public void addFlow(Flow flow) {
		flows.add(flow);
		totalFlow += flow.getVehicles();
	}
	
	public Flow getAndRemoveNextFlow() {
		return flows.pollFirst();
	}
	
	public void removeFlow(Flow flow) {
		flows.remove(flow);
	}
	public boolean hasFlow() {
		return flows.size() > 0;
	}
	
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
		else if (this.flows.first().getTime() < l.flows.first().getTime()) {
			return -1;
		}
		else {
			return 1;
		}
	}
}
