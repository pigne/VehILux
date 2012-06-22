/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file Area.java
 * @date Nov 6, 2010
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.generation;

import java.awt.Color;
import java.util.ArrayList;

/**
 * 
 */
public class Area {
	
	private String id = null;
	public String getId() {
		return id;
	}
	
	private ZoneType zoneType = null;
	public ZoneType getZoneType() {
		return zoneType;
	}
	
	private double probability;
	public void setProbability(double probability) {
		this.probability = probability;
	}
	public double getProbability() {
		return probability;
	}
	public void addProbability(double probability) {
		this.probability += probability;
	}

	private Color color;
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}

	private double x;
	public double getX() {
		return x;
	}

	private double y;
	public double getY() {
		return y;
	}

	private double radius;
	public double getRadius() {
		return radius;
	}

	private ArrayList<Zone> zones;
	
	public void addZone(Zone zone) {
		zones.add(zone);
	}
	
	public void removeZone(String zoneId) {
		zones.remove(zoneId);
	}
	
	private double surface;
	
	public double getSurface() {
		return surface;
	}
	public void setSurface(double surface) {
		this.surface = surface;
	}
	public void addSurface(double surface) {
		this.surface += surface;
	}
	
	public ArrayList<Zone> getZones() {
		return zones;
	}
	public Area(String id, double x, double y, double radius, double probability, ZoneType zoneType) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.probability = probability;
		this.zoneType = zoneType;
		color = zoneType.getColor();
		Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 40);
		color = transparentColor;
		zones = new ArrayList<Zone>();
		surface = 0.0;
	}

	public Area(double probability, ZoneType zoneType) {
		this.id = "";
		this.x = 0.0;
		this.y = 0.0;
		this.radius = 0.0;
		this.probability = probability;
		this.zoneType = zoneType;
		zones = new ArrayList<Zone>();
	}
	
}
