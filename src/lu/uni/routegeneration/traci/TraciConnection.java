package lu.uni.routegeneration.traci;

import it.polito.appeal.traci.SumoTraciConnection;

import java.util.Set;

import lu.uni.routegeneration.generation.RouteGeneration;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class TraciConnection {
	
	private String configPath;
	
	// Define a static logger variable so that it references the Logger instance named "RouteGeneration".
	static Logger logger = Logger.getLogger(RouteGeneration.class);
			
	public TraciConnection(String configPath) {
		
		// Set up a simple configuration that logs on the console.
	    BasicConfigurator.configure();
	    
		
		// configPath = "./test/Luxembourg/Luxembourg.sumocfg"
		this.configPath = configPath;
	}

	public void ReadTravelTime() {
		
		SumoTraciConnection conn = new SumoTraciConnection(
				configPath,  // config file
				12346,                                 // random seed
				false                                  // look for geolocalization info in the map
				);
		try {
			conn.runServer();
			
			// the first two steps of this simulation have no vehicles.
//				conn.nextSimStep();
//				conn.nextSimStep();
			
			for (int i = 0; i < 100; i++) {
				int time = conn.getCurrentSimStep();
				Set<String> vehicles = conn.getActiveVehicles();
				
				//logger.info("At time step " + time + ", there are "+ vehicles.size() + " vehicles: " + vehicles);
				if (vehicles.size()>0) {

					//String aVehicleID = vehicles.iterator().next();
					
//						Vehicle aVehicle = conn.getVehicle(aVehicleID);
//						logger.info("Vehicle " + aVehicleID + " will traverse these edges: " + aVehicle.getCurrentRoute());
					
//						String edgeID = "94661965#0";
//						double travelTime = conn.getEdgeTravelTime(edgeID);
//						logger.info("travelTime of " + edgeID + ": " + travelTime);
				}
				
				conn.nextSimStep();
			}
			String edgeID;
			double travelTime;
			
			edgeID = "50118056#1";
			travelTime = conn.getEdgeTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			edgeID = "56640729#2";
			travelTime = conn.getCurrentTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			edgeID = "50118056#1";
			travelTime = conn.getCurrentTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			edgeID = "56640729#2";
			travelTime = conn.getCurrentTravelTime(edgeID);
			logger.info("travelTime of " + edgeID + ": " + travelTime);
			
			conn.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
}
