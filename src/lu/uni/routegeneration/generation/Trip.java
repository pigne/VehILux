package lu.uni.routegeneration.generation;

import java.util.List;

import lu.uni.routegeneration.evaluation.Detector;

import org.apache.log4j.Level;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

public class Trip {

	private String id;
	private String sourceId;
	private String destinationId;
	private Path path;
	private String route;
	//private String[] edges;
	private double departTime;
	private String vehicleId;
	private double weight;
	private ZoneType destinationZoneType;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public double getDepartTime() {
		return departTime;
	}

	public void setDepartTime(double departTime) {
		this.departTime = departTime;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public ZoneType getDestinationZoneType() {
		return destinationZoneType;
	}
	
	public void setDestinationZoneType(ZoneType destinationZoneType) {
		this.destinationZoneType = destinationZoneType;
	}
	
	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
		splitRoute(route);
	}
	
	public Trip(String id) {
		this.id = id;	
	}
	
	public Trip(String id, String sourceId, String destinationId) {
		this.id = id;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
	}
	
	public Trip(Path path) {
		setRoute(path);
		List<Node> nodes = path.getNodePath();
		if (nodes.size() > 0) {
			this.sourceId = nodes.get(0).getId();
			this.destinationId = nodes.get(nodes.size()-1).getId();
		}
	}
	
	public void setRoute(Path path) {
		this.path = path;
		StringBuilder sb = new StringBuilder();
		List<Node> nodes = path.getNodePath();
		for (int i = 0; i < nodes.size() ; i++) {
			nodes.get(i).addAttribute("ui.class", "path");
			sb.append(nodes.get(i).getId());
			if (i < nodes.size()-1) {
				sb.append(" ");
			}
		}
		route = sb.toString();
	}
	
	public double computeWeight() {
		weight = 0;
		if (path != null) {
			for (Node node : path.getNodePath()) {
				weight += (Double)node.getAttribute("weight");
				System.out.println(node.getId() + "\t" + (Double)node.getAttribute("weight"));
			}
		}
		return weight;
	}
	
	private void splitRoute(String route) {
		if (route != null && !route.isEmpty()) {
			String[] edges = route.split(" ");
			if (edges.length > 0) {
				destinationId = edges[0];
				sourceId = edges[edges.length -1];
			}
		}
	}
}
