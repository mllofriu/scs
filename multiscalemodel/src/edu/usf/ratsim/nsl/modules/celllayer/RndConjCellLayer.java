package edu.usf.ratsim.nsl.modules.celllayer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;

import edu.usf.experiment.robot.LocalizableRobot;
import edu.usf.experiment.robot.Robot;
import edu.usf.experiment.robot.WallRobot;
import edu.usf.experiment.utils.RandomSingleton;
import edu.usf.micronsl.module.Module;
import edu.usf.micronsl.port.onedimensional.Float1dPort;
import edu.usf.micronsl.port.onedimensional.sparse.Float1dSparsePortMap;
import edu.usf.ratsim.nsl.modules.cell.ConjCell;
import edu.usf.ratsim.nsl.modules.cell.ExponentialConjCell;
import edu.usf.ratsim.nsl.modules.cell.ExponentialPlaceIntentionCell;
import edu.usf.ratsim.nsl.modules.cell.ExponentialWallConjCell;

public class RndConjCellLayer extends Module {

	private static final float SEARCH_WIDTH = 0.3f;

	/**
	 * The layer's cells current activation
	 */
	public float[] activation;

	/**
	 * The list of place cells
	 */
	private List<ConjCell> cells;

	/**
	 * A pointer to the robot. This is used to get the robot's current location
	 */
	private LocalizableRobot lRobot;
	
	/**
	 * A pointer to the wall robot interface
	 */
	private WallRobot wRobot;

	/**
	 * A copy of the random number generator
	 */
	private Random random;

	/**
	 * The bounding rectangle coordinates
	 */
	private float ymax;
	private float ymin;
	private float xmax;
	private float xmin;

	/**
	 * The physical length of the layer, used when simulating the effects of
	 * inactivation
	 */
	private float layerLength;

	/**
	 * The output port. A sparse port is used for efficiency sake.
	 */
	private Float1dSparsePortMap activationPort;

	// private Quadtree qtree;

	/**
	 * Create all cells in the layer.
	 * 
	 * @param name
	 *            The module's name.
	 * @param robot
	 *            A robot able to provide localization information.
	 * @param placeRadius
	 *            The radius of the place cells.
	 * @param minDirectionRadius
	 *            The minimum radius for head direction modulation
	 * @param maxDirectionRadius
	 *            The maximum radius for head direction modulation
	 * @param numIntentions
	 *            The number of intention
	 * @param numCells
	 *            The number of cells in the layer
	 * @param placeCellType
	 *            The type of place cell. ExponentialConjCell and
	 *            WallExponentialConjCell are supported.
	 * @param xmin
	 *            The minimum x value of the box in which place cells are
	 *            located
	 * @param ymin
	 *            The minimum y value of the box in which place cells are
	 *            located
	 * @param xmax
	 *            The maximum x value of the box in which place cells are
	 *            located
	 * @param ymax
	 *            The maximum y value of the box in which place cells are
	 *            located
	 * @param goals
	 *            The list of possible goals. This is used to locate place cells
	 *            near goals.
	 * @param nearGoalProb
	 *            The probability of a place cell being place near a goal
	 *            instead of a generic place
	 * @param layer
	 *            Length The physical length of the layer (mm), used when
	 *            simulating the effects of inactivation.
	 * @param wallInhibition
	 *            A parameter passed to wall modulated cells
	 *            (WallExponentialConjCell).
	 * @param wallParamB
	 * @param wallParamA
	 */
	public RndConjCellLayer(String name, Robot robot, float placeRadius, float minDirectionRadius,
			float maxDirectionRadius, int numIntentions, int numCells, String placeCellType, float xmin, float ymin,
			float xmax, float ymax, float layerLength, float wallParamA, float wallParamB) {
		super(name);

		if (!(placeCellType.equals("ExponentialPlaceIntentionCell") || placeCellType.equals("ExponentialConjCell")
				|| placeCellType.equals("ExponentialWallConjCell"))) {
			System.err.println("Place cell type not implemented");
			System.exit(1);
		}

		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.layerLength = layerLength;

		cells = new LinkedList<ConjCell>();
		// qtree = new Quadtree();
		random = RandomSingleton.getInstance();
		int i = 0;
		do {
			Coordinate prefLocation;
			float preferredDirection;
			float directionRadius;
			int preferredIntention;

			// All cells have a preferred location
			prefLocation = createrPreferredLocation(xmin, xmax, ymin, ymax);
			preferredDirection = (float) (random.nextFloat() * Math.PI * 2);
			// Using Inverse transform sampling to sample from k/x between
			// min and max
			// https://en.wikipedia.org/wiki/Inverse_transform_sampling. k =
			// 1/(ln (max) - ln(min)) due to normalization
			float k = (float) (1 / (Math.log(maxDirectionRadius) - Math.log(minDirectionRadius)));
			float s = random.nextFloat();
			directionRadius = (float) Math.exp(s / k + Math.log(minDirectionRadius));

			preferredIntention = random.nextInt(numIntentions);

			ConjCell cell = null;
			if (placeCellType.equals("ExponentialConjCell")) {
				cell = new ExponentialConjCell(prefLocation, preferredDirection, placeRadius, directionRadius,
						preferredIntention);
			} else if (placeCellType.equals("ExponentialPlaceIntentionCell")) {
				cell = new ExponentialPlaceIntentionCell(prefLocation, placeRadius, preferredIntention);
			} else if (placeCellType.equals("ExponentialWallConjCell")) {
				cell = new ExponentialWallConjCell(prefLocation, preferredDirection, placeRadius, directionRadius,
						preferredIntention, random, wallParamA, wallParamB);
			}

			cells.add(cell);
			// qtree.insert(new Envelope(new
			// Coordinate(cell.getPreferredLocation().x,cell.getPreferredLocation().y)),
			// cell);

			i++;
		} while (i < numCells);

		activationPort = new Float1dSparsePortMap(this, cells.size(), 1f / 4000);
		addOutPort("activation", activationPort);

		this.lRobot = (LocalizableRobot) robot;
		this.wRobot = (WallRobot) robot;
	}

	/**
	 * Generates a uniformly distributed location for a place cell.
	 * 
	 * A nearGoalProb proportion of cells are placed near goal positions
	 * instead.
	 * 
	 * @param xmin
	 *            The minimum x value of the box in which place cells are
	 *            located
	 * @param ymin
	 *            The minimum y value of the box in which place cells are
	 *            located
	 * @param xmax
	 *            The maximum x value of the box in which place cells are
	 *            located
	 * @param ymax
	 *            The maximum y value of the box in which place cells are
	 *            located
	 * @return The location of the new cell.
	 */
	private Coordinate createrPreferredLocation(float xmin, float xmax, float ymin, float ymax) {
		float x, y;
		x = random.nextFloat() * (xmax - xmin) + xmin;
		y = random.nextFloat() * (ymax - ymin) + ymin;
		return new Coordinate(x, y);
	}

	/**
	 * Computes the current activation of all cells
	 */
	public void run() {
		// Find the intention
		Float1dPort intention = (Float1dPort) getInPort("intention");
		int intentionNum = -1;
		int i = 0;
		while (intentionNum == -1 && i < intention.getSize()) {
			if (intention.get(i) == 1)
				intentionNum = i;
			i++;
		}

		run(lRobot.getPosition(), lRobot.getOrientationAngle(), intentionNum, wRobot.getDistanceToClosestWall());
	}

	/**
	 * Computes the current activation of all cells given the current
	 * parameters.
	 * 
	 * @param point
	 *            The current location of the animat
	 * @param angle
	 *            The current heading of the animat
	 * @param inte
	 *            The current intention of the animat
	 * @param distToWall
	 *            The distance to the closest wall
	 */
	public void run(Coordinate point, float angle, int inte, float distToWall) {
		int i = 0;
		float total = 0;
		Map<Integer, Float> nonZero = activationPort.getNonZero();
		nonZero.clear();

		// List<Object> activeCells = qtree.query(new Envelope(point.x -
		// SEARCH_WIDTH, point.x + SEARCH_WIDTH, point.y - SEARCH_WIDTH, point.y
		// + SEARCH_WIDTH));

		// System.out.println(activeCells.size());
		// for (Object pCellObj : activeCells) {
		for (ConjCell pCell : cells) {
			// ConjCell pCell = (ConjCell) pCellObj;
			float val = pCell.getActivation(point, angle, inte, distToWall);
			if (val != 0)
				nonZero.put(i, val);
			total += val;
			i++;
		}

		if (Float.isNaN(total))
			System.out.println("Numeric error");
	}

	public List<ConjCell> getCells() {
		return cells;
	}

	/**
	 * Set the modulation parameter of each cell based on a randomize distance
	 * to the center of inactivation. This inactivation supposes a inactivation
	 * efficiency inversely proportional to the cube of the distance to the
	 * point of injection (based on the volume of the sphere)
	 * 
	 * @param constant
	 *            A constant multiplying the modulation
	 */
	public void anesthtizeRadial(float constant) {
		// active = false;
		for (ConjCell cell : cells) {
			float distanceFromInj = random.nextFloat() * layerLength / 2;
			float deact;
			float volume = (float) (3. / 4 * Math.PI * Math.pow(distanceFromInj, 3));
			if (volume != 0)
				deact = (float) Math.min(1, constant / volume);
			else
				deact = 0;
			cell.setBupiModulation(1 - deact);
		}

	}

	/**
	 * A proportion of the cell are fully deactivated
	 * 
	 * @param proportion
	 *            The proportion of cells to be deactivated
	 */
	public void anesthtizeProportion(float proportion) {
		// active = false;
		for (ConjCell cell : cells) {
			if (random.nextFloat() < proportion)
				cell.setBupiModulation(0);
		}

	}

	@Override
	public boolean usesRandom() {
		return false;
	}

	public void clear() {
		((Float1dSparsePortMap) getOutPort("activation")).clear();
	}

	/**
	 * Remaps the layer, relocating all place cells
	 */
	public void remap() {
		for (ConjCell cell : cells) {
			Coordinate prefLocation = createrPreferredLocation(xmin, xmax, ymin, ymax);
			float preferredDirection = (float) (random.nextFloat() * Math.PI * 2);
			cell.setPreferredLocation(prefLocation);
			cell.setPreferredDirection(preferredDirection);
		}
	}

	/**
	 * Disable the effect of inactivation
	 */
	public void reactivate() {
		for (ConjCell cell : cells) {
			cell.setBupiModulation(1);
		}
	}

}
