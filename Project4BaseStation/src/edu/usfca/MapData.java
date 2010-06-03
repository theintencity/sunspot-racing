package edu.usfca;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The map data contains the map dimensions, start and finish positions as well
 * as list of obstructions (i.e., size and position of each obstruction).
 *
 * Use the createRandom class method to create a new map. It invokes the
 * appropriate sub-class depending on the map type and difficulty level.
 * To add a new map type, you can create a sub-class and install it in the
 * createRandom method.
 *
 * @author mamta
 */
public class MapData {
    // the map types
    public static final String RANDOM_BLOCKS = "Random Blocks";
    public static final String RANDOM_MAZE   = "Random Maze";
    public static final String MOVING_BLOCKS = "Moving Blocks";
    public static final String OPEN_FIELD    = "Open Field";
    public static final String[] MAP_TYPES = { RANDOM_BLOCKS, RANDOM_MAZE, MOVING_BLOCKS, OPEN_FIELD};

    // the map difficulty level
    public static final String EASY          = "Easy";
    public static final String MODERATE      = "Moderate";
    public static final String DIFFICULT     = "Difficult";
    public static final String[] MAP_LEVELS = { EASY, MODERATE, DIFFICULT };

    // default map Rectangle(-4000, -1000, 8000, 30000);
    private static final int DEFAULT_X = -4000;
    private static final int DEFAULT_Y = -1000;
    private static final int DEFAULT_WIDTH = 8000;
    private static final int DEFAULT_HEIGHT = 30000;

    // start Y position is 0, and goes up to 1000 from Y end of map.
    private static final int DEFAULT_START_Y = 0;
    private static final int DEFAULT_FINISH_DISTANCE = 1000;

    // default gap between initial car positions
    private static final int DEFAULT_CAR_GAP = 1000;

    // the map properties
    protected Rectangle bounds;
    protected Point finish;
    protected Point start;
    protected int initialDistance;

    // list of obstructions, typically defined by sub-classes
    protected List<Rectangle> obstructions = new LinkedList<Rectangle>();

    /**
     * This method should be used to create a random map.
     *
     * @param type The map type.
     * @param level The difficulty level of the map.
     * @return
     */
    public static MapData createRandom(String type, String level) {
        MapData map;
        if (RANDOM_BLOCKS.equals(type))
            map = new MapDataBlocks();
        else if (MOVING_BLOCKS.equals(type))
            map = new MapDataMovingBlocks();
        else if (RANDOM_MAZE.equals(type))
            map = new MapDataMaze();
        else
            map = new MapData();

        map.createDefault();
        map.addObstructions(level);
        return map;
    }

    /**
     * The destructor for the map data.
     */
    public void cleanup() {
        removeObstructions();
    }

    /**
     * Create the default dimension, and start/finish positions.
     */
    protected void createDefault() {
        bounds = new Rectangle(DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        start = new Point(0, DEFAULT_START_Y);
        finish = new Point(0, bounds.height + bounds.y - DEFAULT_FINISH_DISTANCE);
        initialDistance = DEFAULT_CAR_GAP / 2;
    }

    /**
     * The sub-class should add obstructions depending on the level of
     * difficulty and map type. The base does not have any obstructions.
     *
     * @param level
     */
    protected void addObstructions(String level) {
        // nothing.
    }

    /**
     * Remove all the obstructions in this map. The sub-class may override
     * this to do any additional cleanup if needed.
     */
    protected void removeObstructions() {
        while (obstructions.size() > 0)
            obstructions.remove(0);
    }

    /**
     * Get the bounds rectangle.
     *
     * @return
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Get the start position.
     *
     * @return
     */
    public Point getStart() {
        return start;
    }

    /**
     * Get the finish position.
     *
     * @return
     */
    public Point getFinish() {
        return finish;
    }

    /**
     * Get the list of obstructions.
     *
     * @return
     */
    public List getObstructions() {
        return obstructions;
    }

    /**
     * Get a new car's start position based on its index.
     * The cars are positioned starting from the center, alternating between
     * left and right. Hence for increasing index, the cars are places at
     * -X, +X, -2X, +2X, -3X, +3X... where X is initial Distance (500) and
     * up to the bounds. After the bounds the cars are places at the boundary
     * of the map.
     *
     * @param index
     * @return
     */
    public Point getStart(int index) {
        int x =  index % 2 == 0 ? -((index + 1) * initialDistance) : (index * initialDistance);
        if (x < bounds.x)
            x = bounds.x + 10;
        else if (x > bounds.x + bounds.width)
            x = bounds.x + bounds.width - 10;
        return new Point(x, 0);
    }

    /**
     * Check whether the supplied bounds collides with any of the obstructions
     * of this map or with the boundary of this map.
     *
     * @param rect1
     * @return Return true if collides, else false.
     */
    public boolean hasCollided(Rectangle rect1) {
        if (rect1.x < bounds.x || rect1.x + rect1.width > bounds.x + bounds.width
         || rect1.y < bounds.y || rect1.y + rect1.width > bounds.y + bounds.height) {
            return true;
        }
        
        for (Iterator<Rectangle> it=obstructions.iterator(); it.hasNext(); ) {
            Rectangle rect2 = it.next();
            if (rect1.intersects(rect2))
                return true;
        }
        return false;
    }

}
