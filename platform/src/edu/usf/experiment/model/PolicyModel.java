package edu.usf.experiment.model;

import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;

public interface PolicyModel {

	/**
	 * Returns the policy to follow (which affordance) in each point in space
	 * @return The map from points to the affordance to execute in a greedy policy
	 */
	Map<Coordinate, Integer> getPolicyPoints();

}