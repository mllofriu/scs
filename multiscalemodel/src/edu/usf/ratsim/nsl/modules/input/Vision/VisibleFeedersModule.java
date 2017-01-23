package edu.usf.ratsim.nsl.modules.input.Vision;

import java.util.List;

import edu.usf.experiment.subject.SubjectOld;
import edu.usf.experiment.universe.Feeder;
import edu.usf.micronsl.module.Module;
import edu.usf.micronsl.port.onedimensional.List.Int1dPortList;
import edu.usf.micronsl.port.onedimensional.array.Int1dPortArray;
import edu.usf.micronsl.port.singlevalue.Int0dPort;
import edu.usf.ratsim.experiment.universe.virtual.VirtUniverse;

/**
 * Provides an output port with the identifier of the closes feeder
 * @author Martin Llofriu
 *
 */
public class VisibleFeedersModule extends Module {

	private SubjectOld sub;
	
	private Int1dPortList outPort = new Int1dPortList(this);

	public VisibleFeedersModule(String name, SubjectOld sub) {
		super(name);
		
		this.sub = sub;
		addOutPort("visibleFeeders", outPort);
		
	}

	@Override
	public void run() {
		
		
		outPort.clear();		
		List<Feeder> feeders = sub.getRobot().getVisibleFeeders(new int[] {});
		for(Feeder f : feeders) outPort.add(f.getId());
		
		//System.out.println("Done visible feeders");
		

	}

	@Override
	public boolean usesRandom() {
		return false;
	}

	
}
