/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Detector.java
 * @date Jun 15, 2011
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.evaluation;

/**
 * 
 */
public class Detector {
	public String id;
	public String edge;
	public int[] vehicles;

	public Detector(int stopHour) {
		vehicles = new int[stopHour];
		for (int i=0; i< vehicles.length; i++){
			vehicles[i]=0;
		}
	}
	public void reset(){
		for (int i=0; i< vehicles.length; i++){
			vehicles[i]=0;
		}
	}

}