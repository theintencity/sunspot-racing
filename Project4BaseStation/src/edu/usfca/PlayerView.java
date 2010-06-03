package edu.usfca;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Iterator;
import javax.swing.JPanel;

/**
 * The individual player view displayed on the right. It automatically adjust the
 * size. The pixels have one-to-one mapping with x, y values in game data, except
 * that the directio of y is reversed in the view: top-to-bottom, instead of bottom-to
 * -top in game data.
 * The player view displays the player's car and correct direction at the origin
 * position, and all others (grid and obstructions) relative to this.
 * The grid is displayed so that the view appears moving.
 * 
 * @author mamta
 */
public class PlayerView extends JPanel implements Runnable {
    // how often in milliseconds to repaint the view
    private static final int PAINT_INTERVAL = 20;

    // various colors
    private static final Color bgColor = Color.BLACK;
    private static final Color fgColor = Color.LIGHT_GRAY;
    private static final Color fgColorNormal = new Color(128, 128, 255);
    private static final Color fgColorWarning = Color.RED;
    private static final Color fgColorFinish = Color.GREEN;

    // when collision happens, a circle is this radius is displayed
    // temporarily on the car.
    private static final int BANG_RADIUS = 30;

    // the x,y spacing of the grid that is displayed.
    private static final int gridSpace = 150;
    private static final Font textFont = new Font("Arial", Font.PLAIN, 14);

    // this player's data
    private PlayerData data;

    // the global game data needed for drawing obstructions
    private GameData gameData;

    /**
     * Start the player thread.
     * 
     * @param gameData
     * @param data
     */
    public PlayerView(GameData gameData, PlayerData data) {
        this.gameData = gameData;
        this.data = data;
        setDoubleBuffered(true);

        Thread th = new Thread(this);
        th.start();
    }

    /**
     * The paint method draws the player view:
     * background, grid, player's car, obstructions, control view.
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawGrid(g);
        drawObstructions(g);
        drawCar(g);
        drawControl(g);
    }

    /**
     * Draw the Grid. It assumes the car position at (1/2)*width and (3/4)*height.
     * It draws grid relative to the car position in this view, assuming the
     * grid-spacing uniformly from data position of (0,0).
     */
    private void drawGrid(Graphics g) {
        // view size
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // player position
        double x = data.getX();
        double y = data.getY();

        // draw background
        g.setColor(bgColor);
        g.fillRect(1, 1, viewWidth-2, viewHeight-2);

        g.setColor(fgColor);

        // draw vertical lines on the left of player
        for (int x1= -(((int) x) % gridSpace) + viewWidth/2; x1 >= 0; x1 -= gridSpace) {
            g.drawLine(x1, 0, x1, viewHeight-1);
        }

        // draw vertical lines on the right of player
        for (int x1= -(((int) x) % gridSpace) + viewWidth/2 + gridSpace; x1 < viewWidth; x1 += gridSpace) {
            g.drawLine(x1, 0, x1, viewHeight-1);
        }

        // draw horizontal lines below of player
        for (int y1= (((int) y) % gridSpace) + 3*viewHeight/4; y1 < viewHeight; y1 += gridSpace) {
            g.drawLine(0, y1, viewWidth-1, y1);
        }

        // draw horizontal lines above of player
        for (int y1= (((int) y) % gridSpace) + 3*viewHeight/4 - gridSpace; y1 >= 0; y1 -= gridSpace) {
            g.drawLine(0, y1, viewWidth-1, y1);
        }
    }

    /**
     * Draw the obstructions in the map data.
     * @param g
     */
    private void drawObstructions(Graphics g) {
        // view size and player position
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        double x = data.getX();
        double y = data.getY();

        MapData map = gameData.getMapData();
        g.setColor(Color.LIGHT_GRAY);

        // the view rectangle with respect to data co-ordinates
        Rectangle rect1 = new Rectangle((int)(x-viewWidth/2), (int)(y-viewHeight/4), viewWidth, viewHeight);

        // draw all the obstructions
        for (Iterator<Rectangle> it = map.getObstructions().iterator(); it.hasNext(); ) {
            Rectangle rect2 = it.next();
            if (rect1.intersects(rect2)) {
                Rectangle rect3 = data2view(rect2, rect1);
                g.fillRect(rect3.x, rect3.y, rect3.width, rect3.height);
            }
        }

        // now draw the map boundaries, relative to the view
        Rectangle bounds = map.getBounds();
        if (rect1.x < bounds.x) {
            g.fillRect(1, 1, (int) (bounds.x - rect1.x), viewHeight-1);
        }
        if ((rect1.x + rect1.width) > (bounds.x + bounds.width)) {
            int x1 = (int) ((rect1.x + rect1.width) - (bounds.x + bounds.width));
            g.fillRect(viewWidth-x1, 1, x1, viewHeight);
        }
        if ((rect1.y + rect1.height) > (bounds.y + bounds.height)) {
            g.fillRect(1, 1, viewWidth, (rect1.y + rect1.height) - (bounds.y + bounds.height));
        }
        if (rect1.y < bounds.y) {
            g.fillRect(1, viewHeight - (bounds.y-rect1.y), viewWidth, bounds.y-rect1.y);
        }
    }

    /**
     * Draw the car at the fixed location in the view (1/2)*width, (3/4)*height,
     * but with the correct car direction.
     * @param g
     */
    private void drawCar(Graphics g) {
        // view size
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // if car was recently collided draw the red circle.
        if (data.isRecentlyDamaged()) {
            g.setColor(fgColorWarning);
            g.fillOval(viewWidth/2-BANG_RADIUS, 3*viewHeight/4-BANG_RADIUS, 2*BANG_RADIUS, 2*BANG_RADIUS);
        }

        // different players have different car colors
        g.setColor(data.getCarColor());

        // get the car polygone based on the direction, relative to the view
        Polygon p = data.getCarPolygon(viewWidth, viewHeight);
        g.fillPolygon(p);

        // also draw the direction on the car
        g.setColor(bgColor);
        g.drawLine((p.xpoints[0]+p.xpoints[3])/2, (p.ypoints[0]+p.ypoints[3])/2,
                   (p.xpoints[0]+p.xpoints[1])/2, (p.ypoints[0]+p.ypoints[1])/2);
        g.drawLine((p.xpoints[1]+p.xpoints[2])/2, (p.ypoints[1]+p.ypoints[2])/2,
                   (p.xpoints[1]+p.xpoints[0])/2, (p.ypoints[1]+p.ypoints[0])/2);

    }

    /**
     * Draw the control view for the speed and damage information for each
     * player view. For finished player, it just displays the finish time.
     * Otherwise for completely damaged car, it displays a lost message.
     * Otherwise for a active player, it displays the control for the
     * speed as dial from 0-150. It also displays a red-dot if the
     * player is inactive so that to warn him to bring the SPOT closer to the
     * base station to continue the game. The speed control is displayed
     * in different color depending on the speed.
     *
     * @param g
     */
    private void drawControl(Graphics g) {
        // the control is placed near the height of the view.
        int viewHeight = getHeight();

        int yText = viewHeight - 30;
        g.setFont(textFont);
        if (data.hasFinished()) {
            // if player has crossed the finish line
            g.setColor(fgColorFinish);
            g.drawString("Finished in " + data.getFinishDuration() / 1000.0 + " seconds", 20, yText);
        }
        else if (data.isCompletelyDamaged()) {
            // if player's car is completely damaged.
            g.setColor(fgColorWarning);
            g.drawString("You Lost", 20, yText);
        }
        else {
            // draw the speed view as a dial. Angle depends on speed.
            // absolute value of speed is used.
            double value = Math.toRadians(Math.abs(data.getSpeed())*180.0/PlayerData.SPEED_FORWARD_MAX);
            g.setColor(bgColor);
            g.fillRect(10, viewHeight-110, 140, 105);
            g.setColor(fgColor);
            g.drawRect(10, viewHeight-110, 140, 105);
            int xd = (int) (50*Math.cos(value));
            int yd = (int) (50*Math.sin(value));
            // color also depends on speed
            if (data.getSpeed() < 50)
                g.setColor(Color.GREEN);
            else if (data.getSpeed() < 90)
                g.setColor(Color.ORANGE);
            else
                g.setColor(Color.RED);

            int x0 = 20+50;
            int y0 = yText-20;
            g.fillOval(x0-3, y0-3, 6, 6);
            g.drawLine(x0, y0, x0-xd, y0-yd);

            // write the speed as text as well
            g.setColor(fgColor);
            g.drawString(String.valueOf((int) data.getSpeed()), x0, y0+20);

            // display the damage bar, as well as count.
            int damage = (int) data.getDamage();
            g.setColor(fgColorWarning);
            g.drawString(String.valueOf((int) data.getDamage()), 20 + 100 + 10, yText+10);
            g.fillRect(20 + 100 - damage, yText+5, damage, 5);
            g.setColor(fgColorFinish);
            g.fillRect(20, yText+5, 100-damage, 5);
        }

        // if there was no recent activity from this player,
        // indicate a small dot so that player can move the SPOT closer to
        // the base station, assuming he wants to continue to play.
        if (gameData.isStarted() && !data.hasRecentActivity()) {
            g.setColor(fgColorWarning);
            g.fillOval(2, yText-10, 10, 10);
        }
    }

    /**
     * Utility method to map a rectangle in game data to the local view.
     * The x,y position is translated based on the bounds' position and
     * size.
     * 
     * @param rect The rectangle to map
     * @param bounds The bounds of the local view.
     * @return The new rectangle with co-ordinates in the local view.
     */
    private Rectangle data2view(Rectangle rect, Rectangle bounds) {
        int x1 = rect.x - bounds.x;
        int y1 = (bounds.y + bounds.height) - (rect.y + rect.height);
        return new Rectangle(x1, y1, rect.width, rect.height);
    }

    /**
     * The read method to periodically repaint, and update the player
     * position.
     */
    public void run() {
        while (true) {
            try {
                data.update(PAINT_INTERVAL);
                this.repaint();
                Thread.sleep(PAINT_INTERVAL);
            }
            catch (InterruptedException ex) {
                break;
            }
        }
    }
}
