package lu.uni.routegeneration.generation;

import java.awt.Color;



public enum ZoneType {
	
	RESIDENTIAL(0, new Color(155, 208, 130)), 
	INDUSTRIAL(0, new Color(74, 43, 138)), 
	COMMERCIAL(0, new Color(04, 170, 220));
	
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
