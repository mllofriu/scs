package edu.usf.ratsim.nsl.modules.input;

import edu.usf.experiment.robot.LocalizableRobot;
import edu.usf.experiment.robot.Robot;
import edu.usf.experiment.robot.componentInterfaces.LocalizationInterface;
import edu.usf.micronsl.module.Module;
import edu.usf.micronsl.port.onedimensional.vector.Point3fPort;

/**
 * Provides an output port stating whether the subject just ate
 * @author Martin Llofriu
 *
 */
public class Position extends Module {

	public Point3fPort pos;
	private LocalizableRobot robot;
	private LocalizationInterface localization;
	private Runnable option;

	public Position(String name, Robot robot) {
		super(name);
		if(robot instanceof LocalizableRobot){
			this.robot = (LocalizableRobot)robot;
			option = new LocalizableRobotOption();
		} else {
			localization = (LocalizationInterface)robot;
			option = new InterfaceOption();
		}
		pos = new Point3fPort(this);
		addOutPort("position", pos);
	}

	@Override
	public void run() {
		option.run();
	}

	@Override
	public boolean usesRandom() {
		return false;
	}

	class LocalizableRobotOption implements Runnable{

		@Override
		public void run() {
			pos.set(robot.getPosition());
			
		}
		
	}
	
	class InterfaceOption implements Runnable {

		@Override
		public void run() {
			pos.set(localization.getPosition());
			
		}
		
	}
	
	
}
