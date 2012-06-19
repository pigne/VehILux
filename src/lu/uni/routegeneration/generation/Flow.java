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

/**
 * 
 */
public class Flow  implements Comparable<Flow> {
	
	public final int CAR = 0;
	public final int TRUCK = 1;

	private int cars;
	private int trucks;
	private int vehicles;
	private int hour;
	private double time; // time of a next vehicle departure in sec
	private double carTime; // time of a next car departure in sec
	private double truckTime; // time of a next truck departure in sec
	private double carStepTime; // number of seconds between cars departure 1/cars*3600
	private double truckStepTime; // number of seconds between trucks departure 1/trucks*3600
	private int nextVehicle;
	private int entered;
	private int left;
	
	public int getEntered() {
		return entered;
	}

	public void addEntered(int entered) {
		this.entered += entered;
	}

	public int getLeft() {
		return left;
	}

	public void addLeft(int left) {
		this.left += left;
	}

	public int getHour() {
		return hour;
	}

	public int getVehicles() {
		return vehicles;
	}
	
	public void addVehicles(int vehicles) {
		this.vehicles += vehicles;
	}

	public double getTime() {
		return time;
	}
	
	public int getNextVehicle() {
		return nextVehicle;
	}

	private int stopTime;

	public Flow(int hour, int cars, int trucks, int stopTime) {
		this.hour = hour;
		this.cars = cars;
		this.trucks = trucks;
		this.vehicles = cars + trucks;
		this.stopTime = stopTime;
		this.entered = 0;
		this.left = 0;
		
		time = carTime = truckTime = (hour - 1) * 3600;
		carStepTime = (cars != 0) ? (1.0 / (double) cars * 3600) : Double.MAX_VALUE;
		truckStepTime = (trucks != 0) ? (1.0 / (double) trucks * 3600) : Double.MAX_VALUE;
	}
	
	@Override
	public String toString() {
		return String.format("hour: %d, cars: %d, trucks: %d, carTime: %.2f, truckTime: %.2f, time: %.2f", hour, cars, trucks, carTime, truckTime, time);
	}

	boolean next() {
		boolean hasNext = false;
		if (time < (hour * 3600) && time < stopTime) {
			hasNext = true;
		}
		
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
		
		return hasNext;
	}

	public void resetTime() {
		time = carTime = truckTime = (hour - 1) * 3600;
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
