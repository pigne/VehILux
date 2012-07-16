package lu.uni.routegeneration.helpers;

/**
 * # Best Solution:
#
# 19490.0 
#
# Alleles of best individual:
# 32.564780040204326 [zone res]
# 64.28010447398898 [zone com]
# 3.155115485806686 [zone ind]
# 11.320371012521585 [area com]
# 32.97112872065881 [area com]
# 13.58386119756256 [area com]
# 42.124639069257036 [default area com]
# 88.43170438440667 [area ind]
# 11.568295615593334 [default area ind]
# 14.877354489775419 [area res]
# 85.12264551022459 [default area res]
# 34.35561009268159 [inside flow ratio]
# 40.568854350331804 [shifting ratio]

 */

public class ArgumentsParser {
	
	private static String baseFolder  = "./test/Luxembourg/";
	private static String baseName = "Luxembourg";
	private static double insideFlowRatio = 0.3435561009268159;
	private static int stopHour = 11;
	private static double shiftingRatio = 0.40568854350331804;
	private static double defaultResidentialAreaProbability = 0.8512264551022459;
	private static double defaultCommercialAreaProbability = 0.42124639069257036;
	private static double defaultIndustrialAreaProbability = 0.11568295615593334;
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
