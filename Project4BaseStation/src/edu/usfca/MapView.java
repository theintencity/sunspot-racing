package edu.usfca;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import javax.swing.JPanel;

/**
 * The user interface that displays the global view on the left.
 * It displays the start and finish lines, as well as all the obstructions in
 * event paint interval. This object also scans all players state such as
 * activity, collision and finish line crossing and informs the listener
 * application if needed.
 *
 * @author mamta
 */
public class MapView extends JPanel implements Runnable {
    // how often in milliseconds to refresh the view
    private static final int PAINT_INTERVAL = 20;

    // the colors used
    private static final Color bgColor = Color.BLACK;
    private static final Color fgColor = Color.LIGHT_GRAY;
    private static final Color lineColor = Color.LIGHT_GRAY;

    // length of the player view direction
    private static final int lineLength = 10;

    // radius of the player view circle.
    private static final int carRadius = 4;

    // the listener object that received events when a player is removed
    // due to inactivity, or when a player has reached the finish line.
    private ControlPanel listener;

    // the game data model
    private GameData data;

    /**
     * Construct a new MapView, including the thread to periodically
     * repaint.
     * 
     * @param listener
     * @param data
     */
    public MapView(ControlPanel listener, GameData data) {
        this.listener = listener;
        this.data = data;

        setDoubleBuffered(true);
        Thread th = new Thread(this);
        th.start();
    }

    /**
     * The main method to draw the map and players.
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data != null) {
            drawMap(g);
            drawPlayers(g);
        }
    }


    /**
     * Draw the map: background, start-finish lines, obstructions.
     *
     * @param g
     */
    public void drawMap(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        // background
        g.setColor(bgColor);
        g.fillRect(1, 1, width-2, height-2);

        // start-finish lines
        MapData map = data.getMapData();
        Point start = data2view(map.getStart());
        Point finish = data2view(map.getFinish());

        g.setColor(lineColor);
        g.drawLine(1, start.y, width-2, start.y);
        g.drawLine(start.x, start.y-4, start.x, start.y+4);
        g.drawLine(1, finish.y, width-2, finish.y);

        // obstructions
        g.setColor(fgColor);
        for (Iterator<Rectangle> it = map.getObstructions().iterator(); it.hasNext(); ) {
            Rectangle rect = data2view(it.next());
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    /**
     * Draw the players. It also dispatches event to listener in case of player state
     * changes such as inactivity, collision or finish line crossing.
     * @param g
     */
    private void drawPlayers(Graphics g) {
        for (Iterator<PlayerData> it = data.getPlayers().values().iterator(); it.hasNext(); ) {
            PlayerData player = it.next();

            // if player is inactive, remove him
            if (player.hasExpired()) {
                it.remove();
                listener.removed(player);
            }
            else {
                MapData map = data.getMapData();
                if (map != null) {
                    // if player has collided
                    if (data.hasCollided(player)) {
                        player.damaged();
                    }

                    // if player has finished the finish line
                    if (player.getY() >= map.getFinish().getY()) {
                        player.finished(data.getDuration());
                    }
                }

                // draw a individual player
                drawPlayer(g, player);
            }
        }
    }

    /**
     * Draw a player circle and direction on the map based on the player
     * data.
     *
     * @param g
     * @param player
     */
    private void drawPlayer(Graphics g, PlayerData player) {
        Color color = player.getCarColor();
        Point pos = player.getCarLocation();
        double angle = player.getAngle();
        
        pos = data2view(pos.x, pos.y);
        g.setColor(color);
        g.fillOval(pos.x - carRadius, pos.y - carRadius, 2*carRadius, 2*carRadius);

        double radians = Math.toRadians(angle);
        double xd = lineLength*Math.sin(radians);
        double yd = lineLength*Math.cos(radians);
        g.drawLine(pos.x, pos.y, (int) (pos.x + xd), (int) (pos.y - yd));
    }

    /**
     * Utility method to convert a point from game data to map view.
     *
     * @param p
     * @return
     */
    private Point data2view(Point p) {
        return data2view(p.x, p.y);
    }


    /**
     * Utility method to convert a point from game data to map view.
     * Note that the width maps from bounds.width to view.width, and height
     * form bounds.height to view.height. The x-view on map is centered at x=0,
     * whereas the y-view is not. The direction of y in data is lower-to-upper
     * whereas in map view is upper-to-lower.
     * 
     * @param x
     * @param y
     * @return
     */
    private Point data2view(int x, int y) {
        Rectangle bounds = data.getMapData().getBounds();
        return new Point((int) ((0.5 + x/bounds.getWidth()) * getWidth()),
                         (int) ((1.0 - (y-bounds.getY())/bounds.getHeight()) * getHeight()));
    }

    /**
     * Utility method to convert a rectangle from game data to map view.
     * Note that the rectangle position in game data is given with respect
     * to bottom-left corner point, whereas in map view is with respect to
     * top-left corner point. I calculate the position of the top-left
     * and right-bottom corner points in map-view, and then calculate the
     * rectangle from that.
     * 
     * @param rect
     * @return
     */
    private Rectangle data2view(Rectangle rect) {
        Point pos1 = data2view(rect.x, rect.y+rect.height);
        Point pos2 = data2view(rect.x+rect.width, rect.y);
        return new Rectangle(pos1.x, pos1.y, pos2.x-pos1.x, pos2.y-pos1.y);
    }

    /**
     * The main thread to periodically repaint the view.
     */
    public void run() {
        while (true) {
            this.repaint();
            try {
                Thread.sleep(PAINT_INTERVAL);
            }
            catch (InterruptedException ex) {
                break;
            }
        }
    }
}
