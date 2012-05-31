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

import org.graphstream.graph.Path;


/**
 * 
 */
public class Flow  implements Comparable<Flow> {
	
	private static final int CAR = 0;
	private static final int TRUCK = 1;
	
	private int hour;
	public int getHour() {
		return hour;
	}

	private int cars;
	private int trucks;
	private int vehicles;
	
	public int getVehicles() {
		return vehicles;
	}

	private double time; // time of a next vehicle departure in sec
	public double getTime() {
		return time;
	}

	private double carTime; // time of a next car departure in sec
	private double truckTime; // time of a next truck departure in sec
	private double carStepTime; // number of seconds between cars departure 1/cars*3600
	private double truckStepTime; // number of seconds between trucks departure 1/trucks*3600
	
	private int nextVehicle;
	
	private int stopTime;

	public Flow(int hour, int cars, int trucks, int stopTime) {
		this.hour = hour;
		this.cars = cars;
		this.trucks = trucks;
		this.vehicles = cars + trucks;
		this.stopTime = stopTime;
		
		time = carTime = truckTime = (hour - 1) * 3600;
		carStepTime = 1.0 / (double) cars * 3600;
		truckStepTime = 1.0 / (double) trucks * 3600;
		if (cars > trucks) {
			nextVehicle = CAR;
			carTime += carStepTime;
			time = carTime;
		}
		else {
			nextVehicle = TRUCK;
			truckTime += truckStepTime;
			time = truckTime;
		}
	}
	
	@Override
	public String toString() {
		return String.format("hour: %d, cars: %d, trucks: %d, carTime: %.2f, truckTime: %.2f, time: %.2f", hour, cars, trucks, carTime, truckTime, time);
	}

	boolean next() {
		if (carTime + carStepTime < truckTime + truckStepTime) {
			nextVehicle = CAR;
			carTime += carStepTime;
			time = carTime;
		}
		else {
			nextVehicle = TRUCK;
			truckTime += truckStepTime;
			time = truckTime;
		}
	
		if (time > (hour * 3600) || time > stopTime) {
			return false;
		}

		return true;

	}

	public int compareTo(Flow f) {
		if (this == f)
			return 0;

		else if (this.time < f.time) {
			return -1;
		}
		else {
			return 1;
		}
	}
	
}
