package lu.uni.routegeneration.helpers;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TripHandler extends DefaultHandler {
	
	private int stopHour;
	private double[] durations;
	private double[] routeLenghts;
	private long[] tripNumbers;

	private long totalTripNumber;
	private double totalDuration;
	private double totalRouteLength;
	
	public long getTotalTripNumber() {
		return totalTripNumber;
	}

	public double getTotalDuration() {
		return totalDuration;
	}

	public double getTotalRouteLength() {
		return totalRouteLength;
	}

	public double[] getDurations() {
		return durations;
	}

	public double[] getRouteLenghts() {
		return routeLenghts;
	}

	public long[] getTripNumbers() {
		return tripNumbers;
	}

	public TripHandler(int stopHour) {
		this.stopHour = stopHour;
		durations = new double[stopHour];
		routeLenghts = new double[stopHour];
		tripNumbers = new long[stopHour];
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("tripinfo")) {
			double depart = Double.parseDouble(attributes.getValue("depart"));
			int hour = (int) depart / 3600;
			if (hour < stopHour) {
				tripNumbers[hour]++;
				double duration = Double.parseDouble(attributes.getValue("duration"));
				double routeLength = Double.parseDouble(attributes.getValue("routeLength"));
				durations[hour] += duration;
				routeLenghts[hour] += routeLength; 
				
				totalTripNumber++;
				totalDuration += duration;
				totalRouteLength += routeLength;
			}
		}
	}
	
};