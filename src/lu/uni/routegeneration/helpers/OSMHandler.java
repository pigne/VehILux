package lu.uni.routegeneration.helpers;

import java.awt.geom.Point2D;
import java.util.HashMap;

import lu.uni.routegeneration.generation.Zone;
import lu.uni.routegeneration.generation.ZoneType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.jhlabs.map.proj.Projection;

public class OSMHandler extends DefaultHandler {
	
	private Zone zone = null;
	private Projection proj;
	private Point2D.Double netOffset;
	private HashMap<String, Point2D.Double> nodes;
	
	private HashMap<String, Zone> zones;
	public HashMap<String, Zone> getZones() {
		return zones;
	}

	private double sumResidentialSurface;
	private double sumCommercialSurface;
	private double sumIndustrialSurface;
	
	
	public double getSumResidentialSurface() {
		return sumResidentialSurface;
	}

	public double getSumCommercialSurface() {
		return sumCommercialSurface;
	}

	public double getSumIndustrialSurface() {
		return sumIndustrialSurface;
	}
	
	public OSMHandler(Projection projection, Point2D.Double netOffset) {
		this.proj = projection;
		this.netOffset = netOffset;
		nodes = new HashMap<String, Point2D.Double>();
		zones = new HashMap<String, Zone>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (qName.equals("node")) {
			double x = Double.parseDouble(attributes.getValue(attributes.getIndex("lon")));
			double y = Double.parseDouble(attributes.getValue(attributes.getIndex("lat")));
			Point2D.Double dest = new Point2D.Double();
			proj.transform(x, y, dest);
			dest.x = dest.x + netOffset.x;
			dest.y = dest.y + netOffset.y;
			nodes.put(attributes.getValue("id"), dest);
		} 
		else if (qName.equals("way")) {
			if (zones.get(attributes.getValue("id")) == null) {
				zone = new Zone();
				zone.id = attributes.getValue("id");
			}
		} 
		else if (qName.equals("nd") && zone != null) {
			zone.points.add(nodes.get(attributes.getValue("ref")));
		} 
		else if (qName.equals("tag") && zone != null) {
			if (attributes.getValue("k").equals("landuse")) {
				String landuse = attributes.getValue("v");
				if (landuse.equals("residential")) {
					zone.type = ZoneType.RESIDENTIAL;
				} 
				else if (landuse.equals("industrial")) {
					zone.type = ZoneType.INDUSTRIAL;
				} 
				else if (landuse.equals("commercial") || landuse.equals("retail")) {
					zone.type = ZoneType.COMMERCIAL;
				}
			} 
			else if (attributes.getValue("k").equals("shop") || attributes.getValue("k").equals("amenity")) {
				zone.type = ZoneType.COMMERCIAL;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("way") && zone != null) {
			if (zone.type != null) {
				zone.color = zone.type.getColor();
				// compute area of the zone
				zone.surface = 0.0;
				for (int i = 0; i < zone.points.size() - 1; i++) {
					zone.surface += zone.points.get(i).x * zone.points.get(i + 1).y - zone.points.get(i + 1).x * zone.points.get(i).y; // x0*y1 - x1*y0
					
				}
				zone.surface = Math.abs(zone.surface / 2.0);
				if (zone.type == ZoneType.RESIDENTIAL) {
					sumResidentialSurface += zone.surface;
				}
				if (zone.type == ZoneType.COMMERCIAL) {
					sumCommercialSurface += zone.surface;
				}
				if (zone.type == ZoneType.INDUSTRIAL) {
					sumIndustrialSurface += zone.surface;
				}
				// compute boundaries of the zone
				zone.min_x_boundary = Double.MAX_VALUE;
				zone.min_y_boundary = Double.MAX_VALUE;
				zone.max_x_boundary = Double.MIN_VALUE;
				zone.max_y_boundary = Double.MIN_VALUE;
				for (Point2D.Double p : zone.points) {
					if (p.x < zone.min_x_boundary) {
						zone.min_x_boundary = p.x;
					}
					if (p.x > zone.max_x_boundary) {
						zone.max_x_boundary = p.x;
					}
					if (p.y < zone.min_y_boundary) {
						zone.min_y_boundary = p.y;
					}
					if (p.y > zone.max_y_boundary) {
						zone.max_y_boundary = p.y;
					}
				}
				zones.put(zone.id, zone);
			}
			zone = null;
		}
	}
};
