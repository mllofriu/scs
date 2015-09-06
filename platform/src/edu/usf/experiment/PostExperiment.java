package edu.usf.experiment;

import java.util.List;
import java.util.Random;

import edu.usf.experiment.log.Logger;
import edu.usf.experiment.log.LoggerLoader;
import edu.usf.experiment.plot.Plotter;
import edu.usf.experiment.plot.PlotterLoader;
import edu.usf.experiment.subject.Subject;
import edu.usf.experiment.universe.Universe;
import edu.usf.experiment.universe.UniverseLoader;
import edu.usf.experiment.utils.ElementWrapper;
import edu.usf.experiment.utils.RandomSingleton;
import edu.usf.experiment.utils.XMLExperimentParser;

/**
 * This holds a set of trials over a group of individuals. Group parameters, and
 * trial parameters are loaded from an xml file. See helloworld.xml for details.
 * 
 * When executed, this class only executes one subject's trials, loading from
 * the xml only the information needed for this subject.
 * 
 * @author gtejera,mllofriu
 * 
 */
public class PostExperiment extends Experiment implements Runnable {
	private List<Plotter> afterPlotters;
	private List<Logger> afterLoggers;

	/**
	 * Build an object only for experiment purposes
	 * 
	 * @param experimentFile
	 * @param logPath
	 */
	public PostExperiment(String experimentFile, String logPath) {
		logPath = logPath + "/";
		System.out.println("[+] Wrapping up experiment at " + logPath);

		ElementWrapper root = XMLExperimentParser.loadRoot(experimentFile);

		setUniverse(UniverseLoader.getInstance().load(root, logPath));

		afterPlotters = PlotterLoader.getInstance().load(
				root.getChild("afterExperimentPlotters"), logPath);
		afterLoggers = LoggerLoader.getInstance().load(
				root.getChild("afterExperimentLoggers"), logPath);

	}

	/***
	 * Runs the experiment for the especified subject. Just goes over trials and
	 * runs them all. It also executes tasks and plotters.
	 */
	public void run() {
		System.out.println("[+] Running plotters and loggers");
		
		// Log and finalize
		for (Logger logger : afterLoggers) {
			logger.log(this);
			logger.finalizeLog();
		}
		// Plot
		for (Plotter plotter : afterPlotters)
			plotter.plot();

	}

	public static void main(String[] args) {
		if (args.length < 2)
			System.out.println("Usage: java edu.usf.experiment "
					+ "exprimentLayout");

		PostExperiment e = new PostExperiment(args[0], args[1]);
		e.run();

		System.exit(0);
	}

}