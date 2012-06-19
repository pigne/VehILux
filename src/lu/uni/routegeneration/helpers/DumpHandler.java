package lu.uni.routegeneration.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import lu.uni.routegeneration.generation.Loop;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DumpHandler extends DefaultHandler {
	private HashMap<String,Loop> loops =  new HashMap<String, Loop>();
	private HashMap<String, String> detectorIds;
	private int stopHour;
	private int currentHour;
	
	public HashMap<String,Loop> getLoops() {
		return loops;
	}
	public void setLoops(HashMap<String,Loop> loops) {
		this.loops = loops;
	}
	
	public DumpHandler(HashMap<String, String> detectorIds, int stopHour) {
		this.detectorIds = detectorIds;
		for (String edge: detectorIds.keySet()) {
			String id = detectorIds.get(edge);
			Loop loop = new Loop(id, edge);
			loops.put(id, loop);
		}
		this.stopHour = stopHour;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("interval")) {
			int begin = (int)Double.parseDouble(attributes.getValue("begin"));
			int end = (int)Double.parseDouble(attributes.getValue("end"));
			currentHour = begin / 3600;
			
		}
		if (qName.equals("edge")) {
			String id = attributes.getValue("id");
			if (detectorIds.containsKey(id)) {
				Loop loop = loops.get(detectorIds.get(id));
				int sec = (int)Double.parseDouble(attributes.getValue("sampledSeconds"));
				int entered = (int)Double.parseDouble(attributes.getValue("entered"));
				int left = (int)Double.parseDouble(attributes.getValue("left"));
				loop.addSec(sec, currentHour);
				loop.addEntered(entered, currentHour);
				loop.addLeft(left, currentHour);
//				if (id.equals("-88593721#1")) {
//					System.out.println("hour: " + currentHour + ", entered: " + entered + ", left: " + left);
//				}
			}
		}
	}
};