package edu.usfca;

import java.awt.Rectangle;
import java.util.Random;

/**
 * This map adds random obstructions.
 * It adds randomly generated rectangles as obstructions.
 * The size range and number of the rectangles depend on difficulty level.
 * The size and position of the rectangle are also random.
 * The rectangles are added at a distance of 1000 away from start and
 * finish Y positions.
 *
 * @author mamta
 */
public class MapDataBlocks extends MapData {

    /**
     * Depending on the diffuculty level add random blocks as
     * obstructions.
     *
     * @param level
     */
    @Override
    protected void addObstructions(String level) {
        if (EASY.equals(level))
            addRandomBlocks(30, 500, 3000, 1000);
        else if (MODERATE.equals(level))
            addRandomBlocks(100, 200, 2000, 1000);
        else if (DIFFICULT.equals(level))
            addRandomBlocks(200, 100, 1500, 1000);

    }

    /**
     * Add the obstructions using randomly generated rectangles.
     *
     * @param blocks The number of blocks to add
     * @param minSize The minimum width/height of the block.
     * @param maxWidth The maximum width of the block.
     * @param maxHeight The maximum height of the block.
     */
    private void addRandomBlocks(int blocks, int minSize, int maxWidth, int maxHeight) {
        Random r = new Random();

        // x ranges throughout the map
        int xrange = bounds.width;

        // y ranges from start+1000 to finish-1000.
        int yrange = finish.y - start.y - 2000;

        for (int i=0; i<blocks; ++i) {
            // each obstruction has random size
            int w = minSize + r.nextInt(maxWidth);
            int h = minSize + r.nextInt(maxHeight);

            // the position is random in x/y range
            int x = r.nextInt(xrange) - w/2 - xrange/2;
            int y = r.nextInt(yrange-h) + start.y + 1000;

            Rectangle o = new Rectangle(x, y, w, h);
            obstructions.add(o);
        }
    }

}
