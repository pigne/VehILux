package lu.uni.routegeneration.helpers;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import lu.uni.routegeneration.evaluation.Detector;
import lu.uni.routegeneration.generation.Trip;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class RouteHandler extends DefaultHandler {
	
	private long counter;
	private ArrayList<Trip> trips;
	private Trip trip;
	
	public ArrayList<Trip> getTrips() {
		return trips;
	}

	public RouteHandler() {
		counter = 0;
		trips = new ArrayList<Trip>();
		trip = null;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("vehicle")) {
			String id = attributes.getValue("id");
			String type = attributes.getValue("type");
			double depart = Double.parseDouble(attributes.getValue("depart"));
			int hour = (int) depart / 3600;
			trip = new Trip("h"+hour + "_" + counter);
			trip.setDepartTime(depart);
		}
		if (qName.equals("route")) {
			String route = attributes.getValue("edges");
			if (route != null && trip != null) {
				trip.setRoute(route);
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("vehicle")) {
			trips.add(trip);
		} 
	}
};