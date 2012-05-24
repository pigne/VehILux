package lu.uni.routegeneration.jCell;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.util.InvalidArgumentException;

import com.sun.org.apache.xpath.internal.operations.Bool;

import jcell.EvolutionaryAlg;
import jcell.GenerationListener;
import jcell.Individual;
import jcell.Operator;
import jcell.Population;
import jcell.Problem;
import jcell.SimpleStats;
import jcell.Statistic;

/**
 * @author Sune
 *
 * Generic Co-evolutionary implementation supporting arbitrary child algorithms run in parallel 
 */
/**
 * @author Sune
 *
 */
public class CoevEA/*<T extends EvolutionaryAlg>*/ extends EvolutionaryAlg implements GenerationListener {

	public static final int PARAM_ALG			= 2000;
	public static final int PARAM_BEST_IDV		= 2001;
	public static final int PARAM_BEST_ISL_IDV	= 2002;
	
	// island threads
	private Thread[] t;

	// island algorithms
	private EvolutionaryAlg[] algorithms;
	private int[] operationCount;
	private int islandCount;
	
	private Boolean synchronised = false;
	private Boolean elitism = false;
	private Boolean sequential = false;
	
	private int[] islandMask;
	private Individual bestIndividual;
			
	private Individual[] bestIslandIndividual;
	private int bestIslandIndex;
	
	private final Object lockObj = new Object();
	
	private long startTimeMillis = 0;
	
	/**
	 * Initializes a new instance of the co-evolutionary evolutionary algorithm parent class
	 * @param r
	 * @param islandCount number of islands
	 * @param islandMask assignment of alleles to islands
	 * @param synchronised indicates whether the co-evolutionary parallel process should sync after each generation
	 */
	public CoevEA(Random r, int islandCount, int[] islandMask, Boolean synchronised, Boolean elitism, Boolean sequential)
	{
		super(r);
		this.islandCount = islandCount;

		this.algorithms = new EvolutionaryAlg[this.islandCount];
		this.operationCount = new int[this.islandCount];
		
		this.islandMask = islandMask;
		this.synchronised = synchronised;
		this.elitism = elitism;
		this.sequential = sequential;
		
		if (!this.synchronised && this.elitism)
		{
			throw new InvalidArgumentException("Elitism requires synchronisation");
		}
		else if (this.sequential && this.synchronised)
		{
			throw new InvalidArgumentException("Sequential doesn't work with synchronisation");
		}
			
		this.bestIndividual = null;
		this.bestIslandIndividual = new Individual[this.islandCount];
	}

	/**
	 * Initializes a new instance of the co-evolutionary evolutionary algorithm parent class
	 * @param r
	 * @param islandCount number of islands
	 * @param islandMask assignment of alleles to islands
	 * @param typeName used to initialize the algorithms (only works for some types)
	 * @param synchronised indicates whether the co-evolutionary parallel process should sync after each generation
	 */
	public CoevEA(Random r, int islandCount, int[] islandMask, String typeName/*Class<EvolutionaryAlg> instance*//*, Type AlgorithmType*/, Boolean synchronised, Boolean elitism, Boolean sequential)
	{		
		this(r, islandCount, islandMask, synchronised, elitism, sequential);
	
		// TODO implement reflection or remove this constructor
		try
		{
			Class<?> c = Class.forName(typeName);
			for(int i = 0; i < this.islandCount; i++)
			{
				this.algorithms[i] = (EvolutionaryAlg)c.getConstructor(Random.class).newInstance(r); //instance.newInstance();
				this.algorithms[i].setParam(EvolutionaryAlg.PARAM_LISTENER, this);				
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param island island index to set parameter on
	 * @param keyValue key value
	 * @param param parameter value
	 */
	public void setParam(int island, int keyValue, Object param)
	{
		if(keyValue == PARAM_ALG)
		{
			this.algorithms[island] = (EvolutionaryAlg)param;
			this.algorithms[island].setParam(EvolutionaryAlg.PARAM_LISTENER, this);
		}
		else this.algorithms[island].setParam(keyValue, param);
	}
		
	/**
	 * @param island island index to set parameter on
	 * @param keyName key name
	 * @param param parameter value
	 */
	public void setParam(int island, String keyName, Object param)
	{
		this.algorithms[island].setParam(keyName, param);
	}
	
	/**
	 * @param island island index to get parameter from
	 * @param keyValue key value
	 * @return parameter value
	 */
	public Object getParam(int island, int keyValue)
	{		
		return this.algorithms[island].getParam(keyValue);
	}
	
	@Override
	public Object getParam(int keyValue)
	{
		if (keyValue == EvolutionaryAlg.PARAM_LISTENER)
		{
			// this should stay the global coevolutionary algorithms listener
			return this.listener;
		}
		else if (keyValue == CoevEA.PARAM_BEST_IDV)
		{
			return this.bestIndividual;
		}
		else if (keyValue == CoevEA.PARAM_BEST_ISL_IDV)
		{
			int bestIndex = 0;
			double bestFitness = (Double)this.bestIslandIndividual[0].getFitness();
			for(int i = 1; i < this.islandCount; i++)
			{			
				if ((Double)this.bestIslandIndividual[i].getFitness() < bestFitness)
				{
					bestIndex = i;
					bestFitness = (Double)this.bestIslandIndividual[i].getFitness();
				}
			}
			System.out.println("Best:" + bestIndex);
			return this.bestIslandIndividual[bestIndex];
		}
		else
		{
			// TODO for most parameters, this is ok		
			return this.algorithms[0].getParam(keyValue);
		}
	}
	
	@Override
	public Object getParam(String keyName)
	{
		// TODO for most parameters, this is ok
		return this.algorithms[0].getParam(keyName);
	}
	
	@Override
	public void setParam(int keyValue, Object param)
	{
		if (keyValue == EvolutionaryAlg.PARAM_LISTENER)
		{
			// this should stay the global coevolutionary algorithms listener
			this.listener = (GenerationListener) param;
		}
		else
		{
			for(int i = 0; i < this.islandCount; i++)
			{			
				this.algorithms[i].setParam(keyValue, param);
			}
		}
	}
	
	@Override
	public void setParam(String keyName, Object param)
	{
		if (keyName == "crossover" || keyName == "mutation")
		{
			// intercept operators to replace with a masked operator
			for(int i = 0; i < this.islandCount; i++)
			{
				Boolean[] mask = new Boolean[islandMask.length];
				
				for (int j = 0; j < islandMask.length; j++)
				{	
					// generate the locus mask for the current island
					mask[j] = (islandMask[j] == i);
				}
				
				CoevMaskOperator operator = new CoevMaskOperator((Operator)param, mask);
				
				// set new operator
				this.algorithms[i].setParam(keyName, operator);
			}
		}
		else
		{
			// set parameter on all islands
			for(int i = 0; i < this.islandCount; i++)
			{			
				this.algorithms[i].setParam(keyName, param);
			}
		}
	}
		
	int index;
	@Override
	public void experiment() {
		t = new Thread[this.islandCount];
		final EvolutionaryAlg parentAlg = this;
		
		// start a threads for each island
		for(int i = 0; i < this.islandCount; i++)
		{
			index = i;
			
			t[i] = new Thread( new Runnable()
				{
	                int island=index;
	                GenerationListener listener = (GenerationListener) parentAlg;
	
	                public void run ()
	                {                    
	                	// run experiment for island
	            		algorithms[island].experiment();

	            		// make sure the last best result is reported to others if algorithm quits prematurely
	            		synchronized(lockObj)
	    				{
	    					lockObj.notifyAll();
	    				}
	                } 
	            }
	        );
			
			UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {				
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					int island=index;
					System.out.println("Uncaught exception in island " + island + ":\r\n" +e.getMessage());
					e.printStackTrace();
				}
			};
			t[i].setUncaughtExceptionHandler(eh);
			
		}
		
		this.startTimeMillis = System.currentTimeMillis();
		
		// start all island threads
		for(int i = 0; i < this.islandCount; i++)
		{
			t[i].start();			
		}
		
		// wait for all to finish
		for(int i = 0; i < this.islandCount; i++)
		{
			try {
				t[i].join();
				
				synchronized(lockObj)
				{
					lockObj.notifyAll();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String stats = "CoevEA done with ";
		for(int i = 0; i < this.islandCount; i++)
		{
			Problem problem = (Problem)this.algorithms[i].getParam(PARAM_PROBLEM);
			int evaluations = problem.getNEvals();
			int generations = (Integer)this.algorithms[i].getParam(PARAM_GENERATION_NUMBER);
			stats += generations + "(" + evaluations + ") ";
		}
		stats += "generations(evaluations)";
		System.out.println(stats);
	}
	
	/**
	 * Updates the best individual common to all islands
	 * @param bestIslandIndividual the best individual from one island
	 * @param islandMask the bit mask of the island used to update only the part evolved by the calling island
	 */
	public void updateBest(Individual bestIslandIndividual, Boolean[] islandMask)
	{
		// update the current best individual 
		if (bestIndividual == null)
		{
			bestIndividual = (Individual)bestIslandIndividual.clone();			
		}
		else
		{
			// only update best individual at loci corresponding to this islands mask
			for(int locus = 0; locus < bestIndividual.getLength(); locus++)
			{
				if (islandMask == null || islandMask[locus])
				{
					bestIndividual.setAllele(locus, bestIslandIndividual.getAllele(locus));
				}
			}
		}
	}
	
	/**
	 * Updates the population by setting the parts of the solution which are outside the mask
	 * @param population population to update
	 * @param islandMask mask corresponding to the island
	 */
	public void updatePopulation(Population population, Boolean[] islandMask)
	{
		synchronized(this)
		{	
			// overwrite all individuals in this islands population with the best global individual, only at loci outside of the islands mask
			for (int i=0; i<population.getPopSize(); i++)
			{
				Individual individual = population.getIndividual(i);
	
				for(int locus = 0; locus < islandMask.length; locus++)
				{
					if (!islandMask[locus])
					{
						individual.setAllele(locus, bestIndividual.getAllele(locus));
					}
				}
			}
		}
	}
	
	/* Gets called by each island at the end of each generation. The existing jCell mechanism is used to broadcast the best individuals alleles to the other islands 
	 * @see jcell.GenerationListener#generation(jcell.EvolutionaryAlg)
	 */
	@Override
	public void generation(EvolutionaryAlg EA) {
		int island = IslandIndex(EA);
		
		// extract the mask of this island from its mutation operator
		CoevMaskOperator operator = (CoevMaskOperator)EA.getParam("mutation");			
		Boolean[] islandMask = operator.getMask();
		
        // Get the population and best individual            
        Population population = (Population)EA.getParam(EvolutionaryAlg.PARAM_POPULATION);
        Statistic stats = (Statistic)EA.getParam(EvolutionaryAlg.PARAM_STATISTIC);
        int bestPos = (Integer)stats.getStat(SimpleStats.MIN_FIT_POS);

    	this.bestIslandIndividual[island] = population.getIndividual(bestPos);

		if (elitism) //works only if synchronous
		{				
			waitForOthers(island);				
						
			if (island == 0) // wait for all island threads to update their best individual, use the thread of island 0 to determine the best
			{
				double bestFitness = 1.7976931348623157E308; //new Double(0).MAX_VALUE;
				
				int j = this.r.nextInt(this.islandCount); // start from random island index to prevent same island contributing if fitness are equal
				for(int i = 0; i< this.islandCount; i++)
				{					
					if ((Double)this.bestIslandIndividual[j].getFitness() < bestFitness)
					{
						bestFitness = (Double)this.bestIslandIndividual[j].getFitness();
						bestIslandIndex = j;
					}
					if(++j == this.islandCount) j=0;
				}					
			}
			
			waitForOthers(island);
			
			synchronized(lockObj)
			{
				boolean islandIsElite = bestIndividual == null || (Double)bestIslandIndividual[island].getFitness() < (Double)bestIndividual.getFitness();
				// update the current best individual of best island only, or if this is just after the first generation 
				if (island == bestIslandIndex && islandIsElite || operationCount[island] <= 2)
				{
		            //updateBest(bestIslandIndividual[island], islandMask);
					updateBest(bestIslandIndividual[island], null);
					bestIndividual.setFitness(bestIslandIndividual[island].getFitness());
				
		            System.out.println("updated " + island + " : " + bestIslandIndividual[island].getFitness()); //for debug only!!
				}
			}
		}
		else
		{
			synchronized(lockObj)
			{
				// update the current best individual
	            updateBest(bestIslandIndividual[island], islandMask);
			}
		}
		
		if (synchronised)
		{
			waitForOthers(island);
		}
						
		synchronized(this) //for debug only!!
		{	
			String out = "";
			for(int i = 0; i< this.islandCount; i++)
			{
				int generation =  (Integer)this.algorithms[i].getParam(PARAM_GENERATION_NUMBER);
				if (i == island)					
				{
					out += "[" + generation + "] ";
				}
				else
				{
					out += " " + generation + "  ";
				}
			}
			int offest = out.length();
			
			out += "[";
			for(int locus = 0; locus < islandMask.length; locus++)
			{	
				out += (islandMask[locus]?">":" ") + String.format("%08f", bestIslandIndividual[island].getAllele(locus)) + (islandMask[locus]?"<":" ");					
				
				if (locus < islandMask.length - 1)
					out += ", ";
			}
			out += "] Fitness=" + bestIslandIndividual[island].getFitness();		
			out += ((island == bestIslandIndex && elitism)?"<-best":"");
			
			if (island == bestIslandIndex || this.sequential)
			{
				out += "\r\n" + new String(new char[offest]).replace('\0', ' ');
				out += "[";
				for(int locus = 0; locus < islandMask.length; locus++)
				{	
					out += "_" + String.format("%08f", bestIndividual.getAllele(locus)) + "_";					
					
					if (locus < islandMask.length - 1)
						out += ", ";
				}
				out += "] Fitness=" + bestIndividual.getFitness();
			}
			
			System.out.println(out);
		}
		
		if (sequential)
		{
			if (operationCount[island] == 0)
			{
				waitForOthers(island);
				updatePopulation(population, islandMask);
				System.out.println("upd " + island);
			}
			
			if (island == islandCount - 1)
			{
				// this is to signal to the listener that the parent co-evolutionary algorithm has completed a generation (when all islands have finished their generation)
				listener.generation(this);	
			}
			
			sequenceAfterOthers(island);
            			
			try {
				// ugly way to make sure the previous island has time to update the best solution (broadcast) before this island uses it to update its population.
				// the generation counter is incremented before update happens and a waiting island can in theory resume with the old value. 
				Thread.yield();
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			updatePopulation(population, islandMask);
		}
				
		if (synchronised)
		{
			updatePopulation(population, islandMask);
			waitForOthers(island);
			
			if (island == 0)
			{
				// this is to signal to the listener that the parent co-evolutionary algorithm has completed a generation (when all islands have finished their generation)
				listener.generation(this);	
			}
		}		
		
		listener.generation(EA);
	}
	
	/** determines the island index of the algorithm 
	 * @param EA
	 * @return -1 if not found
	 */
	public int IslandIndex(EvolutionaryAlg EA)
	{
		int island = -1;
		for(int i = 0; i< this.islandCount; i++)
		{
			if (this.algorithms[i] == EA)
			{
				island = i;
				break;
			}
		}
		return island;
	}
	
	/** Makes the current island thread wait if any islands are behind in terms of operation counts
	 * @param island the island index calling this method
	 */
	private void waitForOthers(int island)
	{
		Boolean someIslandBehind = false;
		
		synchronized(lockObj)
		{
			this.operationCount[island]++;
			// System.out.println("w"+island+":"+this.operationCount[island]);
		
			for( int i=0 ; i<this.islandCount ; ++i)
			{
				if (this.operationCount[i] < this.operationCount[island] && t[i].isAlive())
				{
					someIslandBehind = true;
				}
			}
		
			if (someIslandBehind)
			{
				try {
					lockObj.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				// signal all other threads to proceed
				lockObj.notifyAll();
			}
		}
	}
	
	/** Makes the current island thread wait until it is it's turn to continue
	 * @param island the island index calling this method
	 */
	private void sequenceAfterOthers(int island)
	{	
		synchronized(lockObj)
		{
			int islandToWaitFor = island - 1;
			if (islandToWaitFor < 0)
				islandToWaitFor = this.islandCount - 1;
						
			int targetGeneration = (Integer)this.algorithms[islandToWaitFor].getParam(PARAM_GENERATION_NUMBER);
			int thisGeneration = (Integer)this.algorithms[island].getParam(PARAM_GENERATION_NUMBER);
			
			System.out.println("seq("+island+")"+thisGeneration+"/"+targetGeneration + ":" + (System.currentTimeMillis() - startTimeMillis));
			
			// The first island waits until last reaches its generation number, other islands wait until their predecessor reaches their own generation + 1
			// Any island will stop waiting if the island it is waiting for is not alive
			while (!(thisGeneration + 1 == targetGeneration || (island == 0 && thisGeneration == targetGeneration)) && (t[islandToWaitFor].isAlive() || (targetGeneration == 0 && island == 0)))						
			{
				// signal other thread to proceed
				lockObj.notifyAll();
				
				try {
					lockObj.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				targetGeneration = (Integer)this.algorithms[islandToWaitFor].getParam(PARAM_GENERATION_NUMBER);
			}
			
			System.out.println("ext("+island+")"+thisGeneration+"/"+targetGeneration + ":" + (System.currentTimeMillis() - startTimeMillis));
		}
	}	
}
