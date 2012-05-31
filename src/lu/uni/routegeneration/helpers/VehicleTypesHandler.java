package lu.uni.routegeneration.helpers;

import java.util.ArrayList;

import lu.uni.routegeneration.generation.VType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class VehicleTypesHandler extends DefaultHandler {

	private ArrayList<VType> vtypes = new ArrayList<VType>();
	
	public ArrayList<VType> getVtypes() {
		return vtypes;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("vType")) {
			String id = attributes.getValue(attributes.getIndex("id"));
			String accel = attributes.getValue(attributes.getIndex("accel"));
			String color = attributes.getValue(attributes.getIndex("color"));
			String decel = attributes.getValue(attributes.getIndex("decel"));
			String length = attributes.getValue(attributes.getIndex("length"));					
			String minGap = attributes.getValue(attributes.getIndex("minGap"));
			String maxSpeed = attributes.getValue(attributes.getIndex("maxSpeed"));
			String sigma = attributes.getValue(attributes.getIndex("sigma"));
			VType vt = new VType(id, accel, color, decel, length, minGap, maxSpeed, sigma);
			vtypes.add(vt);
		}
	}
}