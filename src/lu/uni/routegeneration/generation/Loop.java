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
	private int sumEntered;
	private int sumLeft;
	private double sumSec;
	private int stopHour;
	
	public void setStopHour(int stopHour) {
		this.stopHour = stopHour;
	}

	public int getSumEntered() {
		return sumEntered;
	}
	
	public void addEntered(int entered, int hour) {
		this.sumEntered += entered;
		Flow currentFlow = getFlow(hour);
		currentFlow.addEntered(entered);
	}

	public Flow getFlow(int hour) {
		Flow currentFlow = null;
		for (Flow flow : flows) {
			if (flow.getHour() == hour) {
				currentFlow = flow;
			}
		}
		if (currentFlow == null) {
			currentFlow = new Flow(hour,0,0,stopHour);
			flows.add(currentFlow);
		}
		return currentFlow;
	}
	
	public int getSumLeft() {
		return sumLeft;
	}

	public void addLeft(int left, int hour) {
		this.sumLeft += left;
		Flow currentFlow = getFlow(hour);
		currentFlow.addLeft(left);
	}

	public double getSumSec() {
		return sumSec;
	}

	public void addSec(double sec, int hour) {
		this.sumSec += sec;
	}
	
	public double getTotalFlow() {
		double total = 0;
		for (Flow flow : flows) {
			total += flow.getVehicles();
		}
		return total;
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
