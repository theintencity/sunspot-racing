package edu.usfca;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Random;

/**
 * The map that contains obstructions in the form of maze. The
 * maze has horizontal walls, with some gaps to cross them.
 * The number of walls, the number of gaps are determined by the
 * difficulty level. The positions of gaps are random.
 * The walls are added at a distance 1000 away from finish Y position.
 * 
 * @author mamta
 */
public class MapDataMaze extends MapData {

    /**
     * Add the random walls in the maze depending on the difficulty level.
     * 
     * @param level
     */
    @Override
    protected void addObstructions(String level) {
        if (EASY.equals(level))
            addRandomMaze(1000, 1000, 1000, 4);
        else if (MODERATE.equals(level))
            addRandomMaze(500, 500, 500, 6);
        else if (DIFFICULT.equals(level))
            addRandomMaze(150, 150, 150, 6);
    }

    /**
     * Add random walls and gaps.
     *
     * @param blockHeight The height (Y size) of the wall. Smaller blockHeight
     * and smaller yGap means more number of walls.
     * @param xGap The X width of gap.
     * @param yGap The Y gap between walls.
     * @param maxGaps Maximum number of gaps in a wall. Actual number is
     * randomly generated between 1 and maxGaps.
     */
    private void addRandomMaze(int blockHeight, int xGap, int yGap, int maxGaps) {
        Random r = new Random();

        // y range from start+1000 to finish-1000
        int ystart = start.y + 1000;
        int yend = finish.y - start.y - 2000;

        // walls are generated from y start to end
        for (int y=ystart; (y+blockHeight) <= yend; y += yGap + blockHeight) {
            // number of gaps is random betwen 1 and maxGaps
            int gaps = 1 + r.nextInt(maxGaps);
            int[] xpos = new int[gaps];

            // X position of gaps are random
            for (int i=0; i<xpos.length; ++i) {
                xpos[i] = bounds.x + r.nextInt(bounds.width-xGap);
            }
            Arrays.sort(xpos);
            int x1 = bounds.x+1;
            int x2 = 0;

            // draw all the rectangles for a wall
            for (int i=0; i<xpos.length; ++i) {
                x2 = xpos[i];
                if (x2 > x1) {
                    Rectangle o = new Rectangle(x1, y, x2-x1, blockHeight);
                    obstructions.add(o);
                }
                x1 = x2 + xGap;
            }
            if (x1 <= (bounds.x + bounds.width)) {
                // wall has fixed height.
                Rectangle o = new Rectangle(x1, y, bounds.x+bounds.width-x1-1, blockHeight);
                obstructions.add(o);
            }
        }
    }

}
