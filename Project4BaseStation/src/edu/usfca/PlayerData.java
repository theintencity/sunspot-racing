package edu.usfca;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * An individual's player data such as his position in the map, the
 * direction of the car, damage count, finish line status, etc.
 * 
 * @author mamta
 */
public class PlayerData {
    // after how many milliseconds should a player data expire if no activity
    private static final int EXPIRE_AFTER_INACTIVITY = 5000;

    // after how many milisecond after last activity, should player be marked
    // inactive. An inactive player is removed after 5000-1000 of inactivity.
    private static final int RECENT_ACTIVITY_TIMEOUT = 1000;

    // For how long in milliseconds should the damage circle be displayed.
    private static final int RECENT_DAMAGE_DURATION = 200;

    // the number of total damages before marking the car as completely damaged.
    private static final int DAMAGE_LIMIT = 100;

    // width of the car (X) in start position
    private static final int carWidth = 20;

    // length of the car (Y) in start position
    private static final int carLength = 30;

    // maximum speed in forward direction
    public static final int SPEED_FORWARD_MAX = 150;

    // maximum speed in reverse direction
    public static final int SPEED_REVERSE_MAX = 20;

    // what is the absolute speed below which the car is made stationary.
    private static final double SPEED_MIN = 3;

    // how much acceleration to apply to positive speed in positive yd (gas).
    // For negative yd (brake), the acceleration is double
    private static final double SPEED_ACCELERATION_FACTOR = 1/20.0;

    // how much acceleration to apply to negative speed.
    private static final double SPEED_DECELERATION_FACTOR = 1/100.0;

    // how much angle change should be applied for xd (steering control).
    private static final double ANGLE_CHANGE_FACTOR = 1/20.0;

    // how much speed should reduce for inactivity
    private static final double SPEED_DECELERATE_ON_INACTIVITY = 2;

    // what is the speed factor to apply in each interval
    private static final double SPEED_FACTOR = 1/200.0;

    // what is the minimum speed tilt (yd/gas/brake) below which the data is ignored
    private static final int MIN_SPEED_TILT = 10;

    // what is the minimum angle tilt (xd/steering) below which the data is ignored
    private static final int MIN_ANGLE_TILT = 10;

    // the color of the player's car
    private Color color;

    // the current position of the car
    private double x;
    private double y;

    // the bounding rectangle of the car for collision detection
    private Rectangle rect;

    // the speed of the car
    private double speed = 0;

    // the current angle. 0 is straigh. 90 is right. -90 is left.
    private double angle = 0;

    // the address of the player's SPOT
    private String addr;

    // after every activity the expires is reset to some time in future
    private long expires;

    // when was the last activity from this player
    private long lastActivity=0;

    // the damage count of the car
    private double damage = 0;

    // whether the player has finished. Once finished it remains finished.
    private boolean isFinished = false;

    // the finish duration.
    private long duration = -1;

    // the interval used for painting in the view.
    private int paintInterval = 10;

    // the time when this car was last collided.
    private long lastDamage = 0;

    /**
     * Construct a new player data using the start position.
     *
     * @param addr
     * @param color
     * @param start
     */
    public PlayerData(String addr, Color color, Point start) {
        this.addr = addr;
        this.color = color;
        x = start.getX();
        y = start.getY();
        rect = new Rectangle((int)(x-carWidth/2), (int)(y-carLength/2), carWidth, carLength);
        expires = System.currentTimeMillis() + EXPIRE_AFTER_INACTIVITY;
    }

    /**
     * Get the address string.
     * @return
     */
    public String getAddr() {
        return addr;
    }

    /**
     * Get the x position of the car.
     * @return
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y position of the car.
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     * Get the car position.
     * @return
     */
    public Point getCarLocation() {
        return new Point((int) x, (int) y);
    }

    /**
     * Get the car shape as polygon with appropriate angle applied
     * so that the PlayerView can display this car.
     *
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    public Polygon getCarPolygon(int viewWidth, int viewHeight) {
        double radians = Math.toRadians(-angle);
        int w1 = (int) ((carLength/2)*Math.sin(radians));
        int h1 = (int) ((carLength/2)*Math.cos(radians));
        int w2 = (int) ((carWidth/2)*Math.cos(radians));
        int h2 = (int) ((carWidth/2)*Math.sin(radians));

        Polygon p = new Polygon();
        p.addPoint(viewWidth/2-w1-w2, 3*viewHeight/4-h1+h2);
        p.addPoint(viewWidth/2-w1+w2, 3*viewHeight/4-h1-h2);
        p.addPoint(viewWidth/2+w1+w2, 3*viewHeight/4+h1-h2);
        p.addPoint(viewWidth/2+w1-w2, 3*viewHeight/4+h1+h2);
        return p;
    }

    /**
     * Get the car rectangle for collision detection.
     * @return
     */
    public Rectangle getCarRectangle() {
        return rect;
    }

    /**
     * Get the color of the player's car.
     * @return
     */
    public Color getCarColor() {
        return color;
    }

    /**
     * Get the current speed.
     * @return
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Get the current car angle.
     * @return
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Has the player activity expired?
     * @return
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() > expires;
    }

    /**
     * Some activity received from player. Set the last activity and expires.
     */
    public void setActivity() {
        lastActivity = System.currentTimeMillis();
        expires = lastActivity + EXPIRE_AFTER_INACTIVITY;
    }

    /**
     * Set the action by this player. The action is given in xd, yd, zd for
     * tilt of SPOT in three dimensions. The xd controls the angle and yd speed.
     * zd is ignored.
     *
     * Any value of xd or yd below a limit of 10 is ignored. This is treated as
     * an error in holding the SPOT.
     *
     * When the speed is positive, car is moving forward, the positive yd (gas)
     * increases the speed by a factor (acceleration), and negative yd (brake)
     * reduces by double factor. For negative speed (moving backward) factor
     * is five times lower. The speed has a positive and negative maximum
     * above which it is not changed. Also a speed less than a lower absolute threshold
     * is treated as 0, and car is made stationary.
     *
     * @param xd
     * @param yd
     * @param zd
     */
    public void setAction(int xd, int yd, int zd) {
        if (Math.abs(yd) > MIN_SPEED_TILT) {
            if (speed >= 0) {
                if (yd > 0) {
                    speed += yd*SPEED_ACCELERATION_FACTOR;
                }
                else {
                    speed += yd*2*SPEED_ACCELERATION_FACTOR;
                }
            }
            else {
                speed += yd*SPEED_DECELERATION_FACTOR;
            }

            if (speed > (SPEED_FORWARD_MAX - damage))
                speed = (SPEED_FORWARD_MAX - damage);
            else if (speed < -SPEED_REVERSE_MAX)
                speed = -SPEED_REVERSE_MAX;
            if (Math.abs(speed) < SPEED_MIN)
                speed = 0;
        }

        if (Math.abs(xd) > MIN_ANGLE_TILT) {
            angle += xd*ANGLE_CHANGE_FACTOR;
        }
    }

    /**
     * Check whether there was any activity recently.
     *
     * @return
     */
    public boolean hasRecentActivity() {
        return ((System.currentTimeMillis() - lastActivity) < RECENT_ACTIVITY_TIMEOUT);
    }

    /**
     * In every interval (duration) update the position of the car
     * based on the speed and angle. A speed factor is applied, so that 
     * position is not changed drastically in 10 ms. If there has been
     * no recent activity by the user, then start reducing the absolute
     * speed of the user.
     *
     * @param duration
     */
    public void update(int duration) {
        this.paintInterval = duration;
        double radians = Math.toRadians(angle);
        x += speed*Math.sin(radians)*duration*SPEED_FACTOR;
        y += speed*Math.cos(radians)*duration*SPEED_FACTOR;
        rect.setLocation((int) (x - carWidth/2), (int) (y - carLength/2));

        if (!hasRecentActivity()) {
            if (speed > 0) {
                speed -= SPEED_DECELERATE_ON_INACTIVITY*duration*SPEED_FACTOR*2;
                if (speed < 0)
                    speed = 0;
            }
            else if (speed < 0) {
                speed += SPEED_DECELERATE_ON_INACTIVITY*duration*SPEED_FACTOR*2;
                if (speed > 0)
                    speed = 0;
            }
        }
    }

    /**
     * When the car collides, the damaged method is invoked. It increases
     * the damage count, and sets the speed to 0. It also positions the
     * car to just before the collision.
     */
    public void damaged() {
        double radians = Math.toRadians(angle);
        x -= 2*speed*Math.sin(radians)*paintInterval*SPEED_FACTOR;
        y -= 2*speed*Math.cos(radians)*paintInterval*SPEED_FACTOR;
        rect.setLocation((int) (x - carWidth/2), (int) (y - carLength/2));
        damage += Math.abs((speed <= SPEED_FORWARD_MAX/2 ? speed : 4*speed)/10.0);
        speed = 0;
        lastDamage = System.currentTimeMillis();
    }

    /**
     * Get the current damage count.
     * @return
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Whether the damage count has reached the limit?
     * @return
     */
    public boolean isCompletelyDamaged() {
        return (damage >= DAMAGE_LIMIT);
    }

    /**
     * If car recently collided.
     * @return
     */
    public boolean isRecentlyDamaged() {
        return (System.currentTimeMillis() - lastDamage) < RECENT_DAMAGE_DURATION;
    }

    /**
     * Player finished the race. Set the flag and finish duration.
     * The flag is set only once and finish duration recorded the first
     * time the car reaches the finish line.
     *
     * @param duration
     */
    public void finished(long duration) {
        if (!isFinished) {
            isFinished = true;
            this.duration = duration;
        }
    }

    /**
     * Check whether the player has finished the race.
     * @return
     */
    public boolean hasFinished() {
        return isFinished;
    }

    /**
     * Get the finish duration of this player is hasFinished.
     * @return
     */
    public long getFinishDuration() {
        return duration;
    }
}
