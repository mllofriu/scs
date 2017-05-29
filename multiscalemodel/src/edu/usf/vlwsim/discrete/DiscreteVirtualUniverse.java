package edu.usf.vlwsim.discrete;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import edu.usf.experiment.display.DisplaySingleton;
import edu.usf.experiment.robot.Robot;
import edu.usf.experiment.universe.GlobalCameraUniverse;
import edu.usf.experiment.universe.GridUniverse;
import edu.usf.experiment.universe.MovableRobotUniverse;
import edu.usf.experiment.universe.platform.Platform;
import edu.usf.experiment.universe.platform.PlatformUniverse;
import edu.usf.experiment.universe.wall.Wall;
import edu.usf.experiment.universe.wall.WallUniverse;
import edu.usf.experiment.utils.ElementWrapper;
import edu.usf.experiment.utils.GeomUtils;
import edu.usf.vlwsim.display.swing.VirtualUniversePanel;

public class DiscreteVirtualUniverse
		implements PlatformUniverse, WallUniverse, GlobalCameraUniverse, MovableRobotUniverse, GridUniverse {

	private static final float HALF_CELL = .5f;
	private Robot robot;
	private Float robotPos;
	private float robotAngle;
	private int gridWidth;
	private int gridHeight;
	private List<Wall> walls;
	private List<Wall> wallsToRevert;
	private List<Platform> platforms;
	private int robotDx;
	private int robotDy;

	public DiscreteVirtualUniverse(ElementWrapper params, String logPath) {
		robotPos = new Float();
		robotAngle = 0;

		gridWidth = params.getChildInt("width");
		gridHeight = params.getChildInt("height");

		walls = new LinkedList<Wall>();
		wallsToRevert = new LinkedList<Wall>();

		platforms = new LinkedList<Platform>();

		robotDx = 0;
		robotDy = 0;

		DisplaySingleton.getDisplay().addPanel(new DiscreteVirtualUniversePanel(this), 1, 0, 1, 1);
	}

	@Override
	public void setRobot(Robot robot) {
		this.robot = robot;
	}

	@Override
	public void setRobotPosition(Float newPos) {
		robotPos = newPos;
	}

	@Override
	public void setRobotOrientation(float degrees) {
		robotAngle = (float) ((robotAngle + degrees + Math.PI * 2) % (2 * Math.PI));
	}

	@Override
	public Point3f getRobotPosition() {
		return new Point3f(robotPos.x, robotPos.y, 0);
	}

	@Override
	public Quat4f getRobotOrientation() {
		return GeomUtils.angleToRot(robotAngle);
	}

	@Override
	public float getRobotOrientationAngle() {
		return robotAngle;
	}

	@Override
	public void addWall(LineSegment segment) {
		Wall w = new Wall(segment);
		walls.add(w);
		wallsToRevert.add(w);
	}

	@Override
	public void addWall(float x, float y, float x2, float y2) {
		Wall w = new Wall(x, y, x2, y2);
		walls.add(w);
		wallsToRevert.add(w);
	}

	@Override
	public List<Wall> getWalls() {
		return walls;
	}

	@Override
	public void setRevertWallPoint() {
		wallsToRevert.clear();
	}

	@Override
	public void revertWalls() {
		for (Wall w : wallsToRevert)
			walls.remove(w);

		wallsToRevert.clear();
	}

	@Override
	public void clearWalls() {
		walls.clear();
		wallsToRevert.clear();
	}

	@Override
	public List<Platform> getPlatforms() {
		return platforms;
	}

	@Override
	public void clearPlatforms() {
		platforms.clear();
	}

	@Override
	public void addPlatform(Point3f pos, float radius) {
		platforms.add(new Platform(pos, radius));
	}

	@Override
	public int getGridWidth() {
		return gridWidth;
	}

	@Override
	public int getGridHeight() {
		return gridHeight;
	}

	/****************************************
	 * Simulation function
	 **************************************/
	public void moveRobot(int dx, int dy) {
		robotPos = new Point2D.Float(Math.round(robotPos.x + dx), Math.round(robotPos.y + dy));
	}

	@Override
	public void step() {
		if (canRobotMove(robotDx, robotDy))
			moveRobot(robotDx, robotDy);
	}

	public void setMotion(int dx, int dy) {
		robotDx = dx;
		robotDy = dy;
	}

	public boolean canRobotMove(int dx, int dy) {
		Coordinate pos = new Coordinate(robotPos.x + HALF_CELL, robotPos.y + HALF_CELL);
		Coordinate newPos = new Coordinate(robotPos.x + HALF_CELL + dx, robotPos.y + HALF_CELL + dy);
		LineSegment path = new LineSegment(pos, newPos);

		boolean intersects = false;
		for (Wall w : walls)
			intersects |= w.intersects(path);

		return !intersects;
	}

}
