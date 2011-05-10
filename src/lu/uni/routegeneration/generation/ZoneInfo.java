package lu.uni.routegeneration.generation;
import java.io.*;
import org.graphstream.graph.Node;


public class ZoneInfo implements Serializable {
	public String id;
	public String shortestPath;
	public String sourceNode;

	public ZoneInfo () {
		id = new String();
		shortestPath = new String();
		sourceNode = new String();
	}
	
	public String toString() {
		return id;
	}

}
