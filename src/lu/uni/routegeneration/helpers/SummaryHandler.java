package lu.uni.routegeneration.helpers;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SummaryHandler extends DefaultHandler {
	
	private int stopHour;
	private long[] runnings;
	private long[] loadeds;
	private long[] emitteds;
	private long[] waitings;
	private long[] endeds;
	private double[] meanWaitingTimes;
	private double[] meanTravelTimes;
	
	private int currentHour;
	private long[] runnings2;
	private long[] loadeds2;
	private long[] emitteds2;
	private long[] waitings2;
	private long[] endeds2;
	private double[] meanWaitingTimes2;
	private double[] meanTravelTimes2;

	public long[] getRunnings() {
		return runnings;
	}
	
	public long[] getLoadeds() {
		return loadeds;
	}

	public long[] getEmitteds() {
		return emitteds;
	}

	public long[] getWaitings() {
		return waitings;
	}

	public long[] getEndeds() {
		return endeds;
	}

	public double[] getMeanWaitingTimes() {
		return meanWaitingTimes;
	}

	public double[] getMeanTravelTimes() {
		return meanTravelTimes;
	}
	
	public long[] getRunnings2() {
		return runnings2;
	}
	
	public long[] getLoadeds2() {
		return loadeds2;
	}

	public long[] getEmitteds2() {
		return emitteds2;
	}

	public long[] getWaitings2() {
		return waitings2;
	}

	public long[] getEndeds2() {
		return endeds2;
	}

	public double[] getMeanWaitingTimes2() {
		return meanWaitingTimes2;
	}

	public double[] getMeanTravelTimes2() {
		return meanTravelTimes2;
	}
	
	
	public SummaryHandler(int stopHour) {
		this.stopHour = stopHour;
		currentHour = 0;
		loadeds = new long[stopHour];
		emitteds = new long[stopHour];
		runnings = new long[stopHour];
		waitings = new long[stopHour];
		endeds = new long[stopHour];
		meanWaitingTimes = new double[stopHour];
		meanTravelTimes = new double[stopHour];
		loadeds2 = new long[stopHour];
		emitteds2 = new long[stopHour];
		runnings2 = new long[stopHour];
		waitings2 = new long[stopHour];
		endeds2 = new long[stopHour];
		meanWaitingTimes2 = new double[stopHour];
		meanTravelTimes2 = new double[stopHour];
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (qName.equals("step")) {
			double time = (int)Double.parseDouble(attributes.getValue("time"));
			int hour = (int) time / 3600;
			if (hour < stopHour) {
				long loaded = Long.parseLong(attributes.getValue("loaded"));
				long emitted = Long.parseLong(attributes.getValue("emitted"));
				long running = Long.parseLong(attributes.getValue("running"));
				long waiting = Long.parseLong(attributes.getValue("waiting"));
				long ended = Long.parseLong(attributes.getValue("ended"));
				double meanWaitingTime = Double.parseDouble(attributes.getValue("meanWaitingTime"));
				double meanTravelTime = Double.parseDouble(attributes.getValue("meanTravelTime"));
				loadeds[hour] = loaded;
				emitteds[hour] = emitted;
				runnings[hour] = running;
				waitings[hour] = waiting;
				endeds[hour] = ended;
				meanWaitingTimes[hour] = meanWaitingTime;
				meanTravelTimes[hour] = meanTravelTime;
				if (hour != currentHour) {
					// next hour
					if (hour == 1) {
						loadeds2[0] = loadeds[0];
						emitteds2[0] = emitteds[0];
						runnings2[0] = runnings[0];
						waitings2[0] = waitings[0];
						endeds2[0] = endeds[0];
						meanWaitingTimes2[0] = meanWaitingTimes[0];
						meanTravelTimes2[0] = meanTravelTimes[0];
					}
					else {
						loadeds2[currentHour] = loadeds[currentHour] - loadeds[currentHour-1];
						emitteds2[currentHour] = emitteds[currentHour] - emitteds[currentHour-1];
						runnings2[currentHour] = runnings[currentHour] - runnings[currentHour-1];
						waitings2[currentHour] = waitings[currentHour] - waitings[currentHour-1];
						endeds2[currentHour] = endeds[currentHour] - endeds[currentHour-1];
						meanWaitingTimes2[currentHour] = meanWaitingTimes[currentHour] - meanWaitingTimes[currentHour-1];
						meanTravelTimes2[currentHour] = meanTravelTimes[currentHour] - meanTravelTimes[currentHour-1];
					}
					currentHour = hour;
				}
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("summary")) {
			if (currentHour == stopHour - 1) {
				loadeds2[currentHour] = loadeds[currentHour] - loadeds[currentHour-1];
				emitteds2[currentHour] = emitteds[currentHour] - emitteds[currentHour-1];
				runnings2[currentHour] = runnings[currentHour] - runnings[currentHour-1];
				waitings2[currentHour] = waitings[currentHour] - waitings[currentHour-1];
				endeds2[currentHour] = endeds[currentHour] - endeds[currentHour-1];
				meanWaitingTimes2[currentHour] = meanWaitingTimes[currentHour] - meanWaitingTimes[currentHour-1];
				meanTravelTimes2[currentHour] = meanTravelTimes[currentHour] - meanTravelTimes[currentHour-1];
			}
		}
	}
};