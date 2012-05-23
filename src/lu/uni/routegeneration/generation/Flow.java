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
import org.graphstream.graph.Path;
import org.xml.sax.SAXException;

/**
 * 
 */
public class Flow   implements Comparable<Flow> {
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
		return String.format("hour: %d, car: %d, truck: %d, carT: %.2f, truckT: %.2f, next: %.2f", hour, car, truck, carT,truckT,next);
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

		if (next > ((hour) * 3600) || next > rg.getStopTime()) {
			//System.out
			//		.println("__done with flow " + loop.id + " (" + loop.edge
			//				+ ") h:" + hour );
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
		if (next > rg.getStopTime()) {
			return false;
		}

		if (loop.dijkstra == null) {
			System.out.println("!!!!!!!!!!!!!!!!!! -> Computing dijkstra for edge "+loop.edge);
			Dijkstra djk = new Dijkstra(Dijkstra.Element.NODE, loop.edge,"weight");
			djk.init(rg.getGraph());
			djk.setSource(rg.getGraph().getNode(loop.edge));
			djk.compute();
			loop.dijkstra = loop.edge;
		}
		
		// System.out.println("Go on flow "+loop.id+"_"+hour+".  next="+next);
		try {
			if (nextVehicle == CAR) {
				rg.incrementVehicleCounter();
				rg.getAttributeImp().clear();
				rg.getAttributeImp().addAttribute("", "", "id", "CDATA", "l" + loop.id + "_h" + hour + "_c" + C);
				rg.getAttributeImp().addAttribute("", "", "type", "CDATA", rg.getVTypes().get((int)(org.util.Random.next() * rg.getVTypes().size())).id);
				rg.getAttributeImp().addAttribute("", "", "depart", "CDATA", "" + (int) next);
				rg.getTransformerHandler().startElement("", "", "vehicle", rg.getAttributeImp());
				rg.getAttributeImp().clear();
				Path p = null;
				do{
					p=rg.createRandomPath(loop.dijkstra,rg.getGraph().getNode(loop.edge));
				} while(p==null);
				
				rg.getAttributeImp().addAttribute("", "", "edges", "CDATA", RouteGeneration.pathToString(p));
				rg.getTransformerHandler().startElement("", "", "route", rg.getAttributeImp());
				rg.getTransformerHandler().endElement("", "", "route");
				
				rg.getTransformerHandler().endElement("", "", "vehicle");
				C++;
			}

			else {
				rg.incrementVehicleCounter();
				rg.getAttributeImp().clear();
				rg.getAttributeImp().addAttribute("", "", "id", "CDATA", "l" + loop.id + "_h" + hour + "_t" + T);
				rg.getAttributeImp().addAttribute("", "", "type", "CDATA", "truck");
				rg.getAttributeImp().addAttribute("", "", "depart", "CDATA", Integer.toString((int) next));
				rg.getTransformerHandler().startElement("", "", "vehicle", rg.getAttributeImp());
				rg.getAttributeImp().clear();

				Path p=null;
				do {
					p=rg.createRandomPath(loop.dijkstra,rg.getGraph().getNode(loop.edge));
				} while(p==null);
				rg.getAttributeImp().addAttribute("", "", "edges", "CDATA", RouteGeneration.pathToString(p));
				rg.getTransformerHandler().startElement("", "", "route", rg.getAttributeImp());
				rg.getTransformerHandler().endElement("", "", "route");

				rg.getTransformerHandler().endElement("", "", "vehicle");
				T++;
			}

		} catch (SAXException e) {
			e.printStackTrace();
		}
		return next();
	}
	
	boolean go2() {
		if (next > rg.getStopTime()) {
			return false;
		}
		if (nextVehicle == CAR) {
			C++;
		}
		else {
			T++;
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
		
	private Path path;
	
	public Path GetPath() {
		return this.path;
	}
	
//	boolean goAndSetPath() {
//		if (next > rg.getStopTime()) {
//			return false;
//		}
//		try {
//			rg.incrementVehicleCounter();
//			do{
//				this.path=rg.createRandomPath(loop.dijkstra,rg.getGraph().getNode(loop.edge));
//			}
//			while(this.path==null);
//			if (nextVehicle == CAR) {
//				C++;
//			}
//			else {
//				T++;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return next();
//	}   
	boolean goNext() {
		if (next > rg.getStopTime()) {
			return false;
		}
		try {
			if (nextVehicle == CAR) {
				C++;
			}
			else {
				T++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return next();
	}   
	
}
