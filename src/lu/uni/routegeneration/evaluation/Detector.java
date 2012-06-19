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
	double shiftingRatio = 0;
    double shiftingRatio2 = 0;

	public Detector(int stopHour) {
		vehicles = new int[stopHour];
		for (int i=0; i < vehicles.length; i++){
			vehicles[i] = 0;
		}
	}
	
	public Detector(int stopHour, String id) {
		this.id = id;
		vehicles = new int[stopHour];
		for (int i=0; i < vehicles.length; i++){
			vehicles[i] = 0;
		}
	}
	
	public Detector(int stopHour, String id, String edge) {
		this.id = id;
		this.edge = edge;
		vehicles = new int[stopHour];
		for (int i=0; i < vehicles.length; i++){
			vehicles[i] = 0;
		}
	}
	
	public void reset(){
		for (int i=0; i < vehicles.length; i++){
			vehicles[i] = 0;
		}
	}
	
	public void shift(){
		int[] temp = new int[vehicles.length];
        temp[0] = (int)(vehicles[0]*(1-shiftingRatio - shiftingRatio2));
        temp[1] = (int)(vehicles[1]*(1-shiftingRatio - shiftingRatio2) + vehicles[0] * shiftingRatio);
        for(int i=2; i<vehicles.length; i++){
            temp[i] = (int)(vehicles[i]*(1-shiftingRatio - shiftingRatio2) + vehicles[i-1] * shiftingRatio + vehicles[i-2] * shiftingRatio2);
        }

        for(int i=0; i<vehicles.length; i++){
        	vehicles[i] = temp[i];
        }
    }
	public double getShiftingRatio() {
		return shiftingRatio;
	}
	public void setShiftingRatio(double shiftingRatio) {
		this.shiftingRatio = shiftingRatio;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String toString(){
        String str = new String();
        str = id + " : ";
        for(int i=0;i<vehicles.length;i++){
            str=str + vehicles[i] +" ";
        }
        //str = str + " f:" + sumOfDifference;
        return(str);
    }
	
	
}