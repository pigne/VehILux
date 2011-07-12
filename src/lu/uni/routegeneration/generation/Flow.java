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
public class Flow implements Comparable<Flow> {
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

	boolean next() {

		if (next == -1) {
			// System.out.print("-");

			truckT = carT = next = (hour - 1) * 3600;
			if (1.0 / (double) car < 1.0 / (double) truck) {
				next += (1.0 / (double) car * 3600);
				nextVehicle = Flow.CAR;
				carT = next;
			} else {
				next += (1.0 / (double) truck * 3600);
				nextVehicle = Flow.TRUCK;
				truckT = next;
			}
		}

		else {

			if (truckT + (1.0 / (double) truck * 3600.0) > carT
					+ (1.0 / (double) car * 3600.0)) {
				carT += 1.0 / (double) car * 3600.0;
				next = carT;
				nextVehicle = CAR;
			} else {
				truckT += (1.0 / (double) truck) * 3600.0;
				next = truckT;
				nextVehicle = TRUCK;
			}

		}
		// System.out.println("Next on flow "+loop.id+"_"+hour+".  next="+next);

		if (next > ((hour) * 3600) || next > rg.stopTime) {
			System.out
					.println("Done with flow " + loop.id + "(" + loop.edge
							+ ")_" + hour + ". Removing. " + next + ". \n-" + C
							+ " cars over " + car + "\n-" + T + " trucks over "
							+ truck);
			loop.flows.remove(this);
			// nextFlow = null;
			return false;
			/*
			 * if (loop.flows.size()<0) {
			 * System.out.println("Done with loop "+loop.id+". Removing.");
			 * loops.remove(loop);
			 * 
			 * }
			 */
		}

		return true;

	}

	boolean go() {

		if (next > rg.stopTime) {
			return false;
		}

		if (loop.dijkstra == null) {
			System.out.println("Computing dijkstra for edge "+loop.edge);
			Dijkstra djk  = new Dijkstra(Dijkstra.Element.node, "weight",
					loop.edge);
			djk.init(rg.graph);
			djk.compute();
			loop.dijkstra = djk.getParentEdgesString();
		}
		// System.out.println("Go on flow "+loop.id+"_"+hour+".  next="+next);
		try {
			if (nextVehicle == CAR) {
				rg.val++;
				rg.ai.clear();
				rg.ai.addAttribute("", "", "id", "CDATA", "l" + loop.id + "_h"
						+ hour + "_c" + C);
				rg.ai.addAttribute("", "", "type", "CDATA",
						rg.vtypes.get((int) (org.util.Random.next() * rg.vtypes
								.size())).id);
				rg.ai.addAttribute("", "", "depart", "CDATA", "" + (int) next);

				rg.tfh.startElement("", "", "vehicle", rg.ai);

				rg.ai.clear();

				String p=null;
				do{
					p=rg.createRandomPath(loop.dijkstra,rg.graph.getNode(loop.edge));
				}while(p==null);
				rg.ai.addAttribute("", "", "edges", "CDATA", p);
				rg.tfh.startElement("", "", "route", rg.ai);
				rg.tfh.endElement("", "", "route");

				rg.tfh.endElement("", "", "vehicle");
				C++;
			}

			else {
				rg.val++;
				rg.ai.clear();
				rg.ai.addAttribute("", "", "id", "CDATA", "l" + loop.id + "_h"
						+ hour + "_t" + T);
				rg.ai.addAttribute("", "", "type", "CDATA", "truck");
				rg.ai.addAttribute("", "", "depart", "CDATA", Integer
						.toString((int) next));
				rg.tfh.startElement("", "", "vehicle", rg.ai);
				rg.ai.clear();

				String p=null;
				do{
					p=rg.createRandomPath(loop.dijkstra,rg.graph.getNode(loop.edge));
				}while(p==null);
				rg.ai.addAttribute("", "", "edges", "CDATA", p);
				rg.tfh.startElement("", "", "route", rg.ai);
				rg.tfh.endElement("", "", "route");

				rg.tfh.endElement("", "", "vehicle");
				T++;
			}

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return next();
	}

	public int compareTo(Flow f) {
		if (this == f)
			return 0;

		else if (this.next < f.next)
			return -1;
		else
			// if (this.next > f.next)
			return 1;
		// else
		// return 0;
	}

}
