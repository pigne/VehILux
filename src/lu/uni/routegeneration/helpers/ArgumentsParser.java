package lu.uni.routegeneration.helpers;

public class ArgumentsParser {
	
	private static String baseFolder  = "./test/Luxembourg/";
	private static String baseName = "Luxembourg";
	private static double insideFlowRatio = 0.695180108613385;;
	private static int stopHour = 11;
	private static double shiftingRatio = 0.39292397212308;
	private static double defaultResidentialAreaProbability = 0.7131550142660672;
	private static double defaultCommercialAreaProbability = 0.11799278717593802;
	private static double defaultIndustrialAreaProbability = 0.9502920163825972;
	private static String referenceNodeId = "77813703#1";
	private static int steps;
	private static int dumpInterval = 3600;
	
	public static String getBaseFolder() {
		return baseFolder;
	}

	public static String getBaseName() {
		return baseName;
	}

	public static double getInsideFlowRatio() {
		return insideFlowRatio;
	}

	public static int getStopHour() {
		return stopHour;
	}

	public static int getSteps() {
		return steps;
	}
	
	public static double getShiftingRatio() {
		return shiftingRatio;
	}

	public static double getDefaultResidentialAreaProbability() {
		return defaultResidentialAreaProbability;
	}

	public static double getDefaultCommercialAreaProbability() {
		return defaultCommercialAreaProbability;
	}

	public static double getDefaultIndustrialAreaProbability() {
		return defaultIndustrialAreaProbability;
	}

	public static String getReferenceNodeId() {
		return referenceNodeId;
	}
	
	public static int getDumpInterval() {
		return dumpInterval;
	}
	
	public static void parse(String[] args) {
		if (args == null) {
			return;
		}
		int i = 0;
		String arg;
		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i];
			i++;
			if (i > args.length) {
				System.err.println("no value for parameter " + arg);
				return;
			}
			if (arg.equals("-baseFolder")) {
				baseFolder = args[i];
			}
			if (arg.equals("-baseName")) {
				baseName = args[i];
			}
			if (arg.equals("-insideFlowRatio")) {
				insideFlowRatio = Double.parseDouble(args[i]);
			}
			if (arg.equals("-stopHour")) {
				stopHour = Integer.parseInt(args[i]);
			}
			if (arg.equals("-shiftingRatio")) {
				shiftingRatio = Double.parseDouble(args[i]);
			}
			if (arg.equals("-referenceNodeId")) {
				referenceNodeId = args[i];
			}
			if (arg.equals("-steps")) {
				steps = Integer.parseInt(args[i]);
			}
			if (arg.equals("-dumpInterval")) {
				dumpInterval = Integer.parseInt(args[i]);
			}
			i++;
		}
	}
}
