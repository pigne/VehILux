package lu.uni.routegeneration.helpers;

import org.apache.log4j.Logger;

import lu.uni.routegeneration.generation.Area;
import lu.uni.routegeneration.generation.Loop;
import lu.uni.routegeneration.generation.RouteGeneration;
import lu.uni.routegeneration.generation.Trip;
import lu.uni.routegeneration.generation.ZoneType;

public class VehlLuxLog {
	
	
	public static void printLoopsInfo(Logger logger, RouteGeneration rg) {
		logger.info("read " + rg.getLoops().size() + " induction loops");	
		double readOuterFlow = 0;
		logger.info("loop \t edge \t flow \n");
		for (Loop loop : rg.getLoops()) {
			logger.info(loop.getId() + "\t " + loop.getEdge() + "\t " + loop.getTotalFlow() + "\n");
			readOuterFlow += loop.getTotalFlow();
		}
		logger.info("total traffic on loops: " + readOuterFlow +"\n");
	}
	
	public static void printAreasInfo(Logger logger, RouteGeneration rg) {
		System.out.println("area type \t zones count \t surface \t probability");
		for (Area area : rg.getAreas()) {
			if (area.getZones() != null) {
				System.out.println(area.getZoneType().name() + " \t " + area.getZones().size() + " \t " + area.getSurface() + " \t " + area.getProbability());
			}	
		}
		System.out.println(rg.getDefaultResidentialArea().getZoneType().name() + " \t " + rg.getDefaultResidentialArea().getZones().size() + " \t " + rg.getDefaultResidentialArea().getSurface() + " \t " + rg.getDefaultResidentialArea().getProbability());
		System.out.println(rg.getDefaultCommercialArea().getZoneType().name() + " \t " + rg.getDefaultCommercialArea().getZones().size() + " \t " + rg.getDefaultCommercialArea().getSurface() + " \t " + rg.getDefaultCommercialArea().getProbability());
		System.out.println(rg.getDefaultIndustrialArea().getZoneType().name() + " \t " + rg.getDefaultIndustrialArea().getZones().size() + " \t " + rg.getDefaultIndustrialArea().getSurface() + " \t " + rg.getDefaultIndustrialArea().getProbability());
	}
	
	public static void printTripInfo(Logger logger, RouteGeneration rg) {
		logger.info("insideFlowRatio: " + rg.getInsideFlowRatio());
		logger.info("generated flows:\n");
		long[] res = new long[rg.getStopHour()];
		long[] com = new long[rg.getStopHour()];
		long[] ind = new long[rg.getStopHour()];
		long sumres=0;
		long sumcom=0;
		long sumind=0;
		long inner = 0;
		long outer = 0;
		for (Trip trip : rg.getTrips()) {
			int hour = (int)trip.getDepartTime()/3600;
			if (trip.getDestinationZoneType().equals(ZoneType.RESIDENTIAL)) {
				res[hour]++;
				sumres++;
			}
			else if (trip.getDestinationZoneType().equals(ZoneType.COMMERCIAL)) {
				com[hour]++;
				sumcom++;
			}
			else if (trip.getDestinationZoneType().equals(ZoneType.INDUSTRIAL)) {
				ind[hour]++;
				sumind++;
			}
		}
		for (int i = 0; i < rg.inner.length; ++i) {
			inner += rg.inner[i];
			outer += rg.outer[i];
		}
		System.out.println("hour \t inner \t outer \t residential \t commercial \t industrial \t sum");
		for (int i = 0; i < rg.getStopHour(); ++i) {
			System.out.println(i + " \t " + rg.inner[i] + " \t " + rg.outer[i] + " \t " + res[i] + " \t " + com[i] + " \t " + ind[i]);
		}	
		System.out.println("sum\t " + inner + " \t " + outer + " \t " + sumres + " \t " + sumcom + " \t " + sumind);
	}
}
