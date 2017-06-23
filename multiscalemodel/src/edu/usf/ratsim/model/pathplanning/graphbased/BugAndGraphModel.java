package edu.usf.ratsim.model.pathplanning.graphbased;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.usf.experiment.display.DisplaySingleton;
import edu.usf.experiment.robot.LocalizableRobot;
import edu.usf.experiment.robot.Robot;
import edu.usf.experiment.robot.SonarRobot;
import edu.usf.experiment.subject.Subject;
import edu.usf.experiment.utils.ElementWrapper;
import edu.usf.micronsl.Model;
import edu.usf.ratsim.model.GraphModel;
import edu.usf.ratsim.nsl.modules.input.HeadDirection;
import edu.usf.ratsim.nsl.modules.input.PlatformPosition;
import edu.usf.ratsim.nsl.modules.input.Position;
import edu.usf.ratsim.nsl.modules.input.SonarReadings;
import edu.usf.ratsim.nsl.modules.pathplanning.Edge;
import edu.usf.ratsim.nsl.modules.pathplanning.ExperienceRoadMap;
import edu.usf.ratsim.nsl.modules.pathplanning.PointNode;

/**
 * This model allows to switch between bug algorithms depending on the 'algorithm' parameter
 * @author ludo
 *
 */
public class BugAndGraphModel extends Model implements GraphModel {


	private ExperienceRoadMap erm;

	public BugAndGraphModel() {
	}

	public BugAndGraphModel(ElementWrapper params,
			Robot robot) {
		
		String algorithm = params.getChildText("algorithm");
		
		SonarReadings sReadings = new SonarReadings("Sonar Readings", (SonarRobot) robot);
		addModule(sReadings);
		
		Position rPos = new Position("Position", (LocalizableRobot) robot);
		addModule(rPos);
		
		HeadDirection orientation = new HeadDirection("HeadDirection", (LocalizableRobot) robot);
		addModule(orientation);
		
		PlatformPosition platPos = new PlatformPosition("Plat Pos");
		addModule(platPos);
		
		erm = new ExperienceRoadMap("Experience road map", algorithm, robot);
		erm.addInPort("sonarReadings", sReadings.getOutPort("sonarReadings"));
		erm.addInPort("sonarAngles", sReadings.getOutPort("sonarAngles"));
		erm.addInPort("position", rPos.getOutPort("position"));
		erm.addInPort("orientation", orientation.getOutPort("orientation"));
		erm.addInPort("platformPosition", platPos.getOutPort("platformPosition"));
		addModule(erm);		
		
		DisplaySingleton.getDisplay().addUniverseDrawer(new ExpGraphDrawer(erm), 0);
		DisplaySingleton.getDisplay().addUniverseDrawer(new SonarReadingsDrawer((SonarRobot) robot, (LocalizableRobot) robot), 0);
	}

	public UndirectedGraph<PointNode, Edge> getGraph() {
		return erm.getGraph();
	}

}
