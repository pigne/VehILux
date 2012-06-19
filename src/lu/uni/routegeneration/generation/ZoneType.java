package lu.uni.routegeneration.generation;

import java.awt.Color;



public enum ZoneType {
	
//	RESIDENTIAL(0, new Color(75, 170, 0, 90)), 
//	COMMERCIAL(0, new Color(44, 157, 222, 90)),
//	INDUSTRIAL(0, new Color(83, 49, 206, 90));
	
	RESIDENTIAL(0, new Color(75, 170, 0)), 
	COMMERCIAL(0, new Color(255, 127, 0)),
	INDUSTRIAL(0, new Color(83, 49, 206)); 
	
	// grayscale
//	RESIDENTIAL(0, new Color(220, 220, 220)), 
//	COMMERCIAL(0, new Color(150, 150, 150)),
//	INDUSTRIAL(0, new Color(80, 80, 80)); 
	
	private double probability;
	private Color color;
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	ZoneType(double probability, Color color) {
		this.probability = probability;
		this.color = color;
	}

	public double getProbability() {
		return probability;
	}
	
	public void setProbability(double probability) {
		this.probability = probability;
	}
};
