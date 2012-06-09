package lu.uni.routegeneration.helpers;

import java.util.TreeSet;

import lu.uni.routegeneration.generation.Flow;
import lu.uni.routegeneration.generation.Loop;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LoopHandler extends DefaultHandler {
	
	private Flow currentFlow = null;
	private Loop currentLoop = null;
	private TreeSet<Loop> loops;
	public TreeSet<Loop> getLoops() {
		return loops;
	}

	private int stopHour;
	
	public LoopHandler(int stopHour) {
		this.stopHour = stopHour;
		loops = new TreeSet<Loop>(); 
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("loop")) {
			String id = attributes.getValue(attributes.getIndex("id"));
			String edge = attributes.getValue(attributes.getIndex("edge"));
			currentLoop = new Loop(id, edge);
		} 
		else if (qName.equals("flow")) {
			int hour = (int) Double.parseDouble(attributes.getValue(attributes.getIndex("hour")));
			if (hour <= stopHour) {	
				int cars = (int) Double.parseDouble(attributes.getValue("cars"));
				int trucks = (int) Double.parseDouble(attributes.getValue("trucks"));
				currentFlow = new Flow(hour, cars, trucks, stopHour * 3600);
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("loop")) {
			loops.add(currentLoop);
			//System.out.println("cars: " + currentLoop.)
		} 
		else if (qName.equals("flow")) {
			if(currentFlow !=null){
				currentLoop.addFlow(currentFlow);
			}
			currentFlow=null;
		}
	}
}
