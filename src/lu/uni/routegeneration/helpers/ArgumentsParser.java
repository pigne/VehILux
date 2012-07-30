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
	
	private String baseFolder  = "./test/Luxembourg/";
	private String baseName = "Luxembourg";
	private double insideFlowRatio = 0.3435561009268159;
	private int stopHour = 11;
	private double shiftingRatio = 0.40568854350331804;
	private double defaultResidentialAreaProbability = 0.8512264551022459;
	private double defaultCommercialAreaProbability = 0.42124639069257036;
	private double defaultIndustrialAreaProbability = 0.11568295615593334;
	private String referenceNodeId = "77813703#1";
	private int steps;
	private int dumpInterval = 3600;
	
	public String getBaseFolder() {
		return baseFolder;
	}

	public String getBaseName() {
		return baseName;
	}

	public double getInsideFlowRatio() {
		return insideFlowRatio;
	}

	public int getStopHour() {
		return stopHour;
	}

	public int getSteps() {
		return steps;
	}
	
	public double getShiftingRatio() {
		return shiftingRatio;
	}

	public double getDefaultResidentialAreaProbability() {
		return defaultResidentialAreaProbability;
	}

	public double getDefaultCommercialAreaProbability() {
		return defaultCommercialAreaProbability;
	}

	public double getDefaultIndustrialAreaProbability() {
		return defaultIndustrialAreaProbability;
	}

	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public void setInsideFlowRatio(double insideFlowRatio) {
		this.insideFlowRatio = insideFlowRatio;
	}

	public void setStopHour(int stopHour) {
		this.stopHour = stopHour;
	}

	public void setShiftingRatio(double shiftingRatio) {
		this.shiftingRatio = shiftingRatio;
	}

	public void setDefaultResidentialAreaProbability(
			double defaultResidentialAreaProbability) {
		this.defaultResidentialAreaProbability = defaultResidentialAreaProbability;
	}

	public void setDefaultCommercialAreaProbability(
			double defaultCommercialAreaProbability) {
		this.defaultCommercialAreaProbability = defaultCommercialAreaProbability;
	}

	public void setDefaultIndustrialAreaProbability(
			double defaultIndustrialAreaProbability) {
		this.defaultIndustrialAreaProbability = defaultIndustrialAreaProbability;
	}

	public void setReferenceNodeId(String referenceNodeId) {
		this.referenceNodeId = referenceNodeId;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public void setDumpInterval(int dumpInterval) {
		this.dumpInterval = dumpInterval;
	}

	public String getReferenceNodeId() {
		return referenceNodeId;
	}
	
	public int getDumpInterval() {
		return dumpInterval;
	}
	
	public void parse(String args) {
		if (args != null) {
			parse(args.split(" "));
		}
	}
	
	public void parse(String[] args) {
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
			if (arg.equals("-defaultResidentialAreaProbability")) {
				defaultResidentialAreaProbability = Double.parseDouble(args[i]);
			}
			if (arg.equals("-defaultCommercialAreaProbability")) {
				defaultCommercialAreaProbability = Double.parseDouble(args[i]);
			}
			if (arg.equals("-defaultIndustrialAreaProbability")) {
				defaultIndustrialAreaProbability = Double.parseDouble(args[i]);
			}
			i++;
		}
	}
	
	public String[] getArgs() {
		String[] args = new String[] {
				"-baseFolder", baseFolder,
				"-baseName", baseName,
				"-insideFlowRatio", ""+insideFlowRatio,
				"-stopHour", ""+stopHour,
				"-shiftingRatio", ""+shiftingRatio,
				"-referenceNodeId", referenceNodeId,
				"-steps", ""+steps,
				"-dumpInterval", ""+dumpInterval,
				"-defaultResidentialAreaProbability", ""+defaultResidentialAreaProbability,
				"-defaultCommercialAreaProbability", ""+defaultCommercialAreaProbability,
				"-defaultIndustrialAreaProbability", ""+defaultIndustrialAreaProbability,
		};
		return args;
	}
}
