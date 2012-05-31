/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file VType.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

/**
 * 
 */
public class VType {

	private String id;
	public String getId() {
		return id;
	}

	public String getAccel() {
		return accel;
	}

	public String getDecel() {
		return decel;
	}

	public String getSigma() {
		return sigma;
	}

	public String getLength() {
		return length;
	}

	public String getMinGap() {
		return minGap;
	}

	public String getMaxSpeed() {
		return maxSpeed;
	}

	public String getColor() {
		return color;
	}

	private String accel;
	private String decel;
	private String sigma;
	private String length;
	private String minGap;
	private String maxSpeed;
	private String color;
	
	public VType(String id, String accel, String color, String decel, String length, String minGap, String maxSpeed, String sigma) {
		this.id = id;
		this.accel = accel;
		this.decel = decel;
		this.sigma = sigma;
		this.length = length;
		this.minGap = minGap;
		this.maxSpeed = maxSpeed;
		this.color = color;
	}
	
}
