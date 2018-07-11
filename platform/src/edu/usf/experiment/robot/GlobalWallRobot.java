package edu.usf.experiment.robot;

import java.util.Set;

import edu.usf.experiment.universe.wall.Wall;

/**
 * A robot that can known about the existence of all the walls in the universe
 * @author martin
 *
 */
public interface GlobalWallRobot {
	
	/**
	 * Returns all the walls in the Universe
	 * @return
	 */
	public Set<Wall> getWalls();

}