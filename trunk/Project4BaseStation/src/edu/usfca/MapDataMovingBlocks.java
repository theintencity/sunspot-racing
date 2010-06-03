package edu.usfca;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * The map containing random moving blocks. This extends the MapdataBlocks,
 * to create a thread that randomly moves the blocks. The direction of the movement
 * is determined randomly, and is periodically reversed.
 *
 * @see MapDataBlocks.
 * @author mamta
 */
public class MapDataMovingBlocks extends MapDataBlocks {

    // whether the thread is running or not?
    private volatile boolean running = false;
    private Thread thread = null;

    /**
     * Add the obstructions using base class method, and start a thread to
     * move those obstructions.
     *
     * @param level
     */
    @Override
    public void addObstructions(String level) {
        super.addObstructions(level);
        startMoveThread(0.5, 50, 5);
    }

    /**
     * Remove the obstructions and terminate the thread.
     */
    @Override
    public void removeObstructions() {
        super.removeObstructions();
        if (thread != null && running) {
            running = false;
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * Start the thread to move the obstructions.
     *
     * @param fraction What fraction of obstructions should be moved.
     * @param interval How often to move.
     * @param speed In each interval, whats the change in position.
     */
    private void startMoveThread(double fraction, final int interval, final double speed) {
        Random r = new Random();

        // the movement direction of a rectangle
        final Map<Rectangle, Double> direction = new HashMap<Rectangle, Double>();

        // make fraction of rectangles with random move direction
        for (Iterator<Rectangle> it=obstructions.iterator(); it.hasNext(); ) {
            Rectangle obs = it.next();
            if (r.nextDouble() < fraction) {
                direction.put(obs, new Double(r.nextInt(360)));
            }
        }

        running = true;

        thread = new Thread() {
            @Override
            public void run() {
                long duration = 0;
                while (running) {
                    duration += interval;

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ex) {
                        continue; // that it will break if running is false
                    }

                    for (Iterator<Rectangle> it=obstructions.iterator(); it.hasNext(); ) {
                        Rectangle obs = it.next();
                        if (direction.containsKey(obs)) {
                            // calculate the new position based on speed and direction
                            double angle = direction.remove(obs).doubleValue();
                            double radians = Math.toRadians(angle);
                            double xd = speed*Math.sin(radians);
                            double yd = speed*Math.cos(radians);
                            obs.setLocation((int) (obs.x + xd), (int) (obs.y + yd));

                            // after some time reverse the move direction
                            if (duration % 30000 == 0)
                                angle += 180;

                            // update in the direction map
                            direction.put(obs, new Double(angle));
                        }
                    }
                }
            }
        };

        thread.start();
    }
}
