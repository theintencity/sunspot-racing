package edu.usfca;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the main data model that stores the game data such as the map
 * and players.
 *
 * @author mamta
 */
public class GameData {
    // the car colors.
    private static final Color[] colors = {Color.GREEN, Color.YELLOW, Color.CYAN, Color.ORANGE, Color.RED};

    // whether the game is started.
    private boolean started = false;

    // the start time
    private long startTime = System.currentTimeMillis();

    // collection of all the active players
    private Map<String, PlayerData> players;

    // the associated map data
    private MapData map = null;

    // the listener application that receives player removed event when
    // stop method is invoked.
    private ControlPanel listener;

    // the start index the player is cached so that if the player
    // re-joins a game, his color and initial position is intact.
    private Map<String, Integer> startPosition = new Hashtable<String, Integer>();

    /**
     * Construct a new game data
     */
    public GameData(ControlPanel listener) {
        this.listener = listener;
    }
    
    /**
     * Set the map data for this game. Any previous map data is cleaned up.
     *
     * @param listener
     * @param value
     */
    public void setMapData(MapData value) {
        if (map != null)
            map.cleanup();
        map = value;
    }

    /**
     * Get the map data for this game.
     * @return
     */
    public MapData getMapData() {
        return map;
    }

    /**
     * Set the list of players for this game.
     *
     * @param value
     */
    public void setPlayers(Map<String, PlayerData> value) {
        players = value;
    }

    /**
     * Get the list of players for this game.
     *
     * @return
     */
    public Map<String, PlayerData> getPlayers() {
        return players;
    }

    /**
     * Set the game in start state.
     */
    public void start() {
        started = true;
        startTime = System.currentTimeMillis();
    }

    /**
     * Stop the game removing any active players.
     */
    public void stop() {
        started = false;
        
        for (Iterator<PlayerData> it=players.values().iterator(); it.hasNext(); ) {
            PlayerData player = it.next();
            it.remove();
            listener.removed(player);
        }
    }

    /**
     * Has the game started?
     * @return
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Get the duration since the start of the game.
     * @return
     */
    public long getDuration() {
        return (System.currentTimeMillis() - startTime);
    }

    /**
     * Check whether there are any players?
     * @return
     */
    public boolean hasPlayers() {
        return players.size() > 0;
    }

    /**
     * Add a new player to the game data. If the player's index was
     * cached it is used, otherwise a new index is created based on
     * the current players count. The index determines the player's
     * color and start position in the map.
     * @param addr
     * @return
     */
    public PlayerData addPlayer(String addr) {
        int index = players.size();
        if (startPosition.containsKey(addr)) {
            // cache the player's start position, so that the color and x,y are same.
            index = startPosition.get(addr).intValue();
        }
        else {
            startPosition.put(addr, new Integer(index));
        }
        Point pos = map.getStart(index);
        Color color = colors[index % colors.length];
        PlayerData player = new PlayerData(addr, color, pos);
        players.put(addr, player);
        return player;
    }

    /**
     * Check whether a player has collided.
     * It checks using the available map data.
     * @param player
     * @return
     */
    public boolean hasCollided(PlayerData player) {
        Rectangle bounds = player.getCarRectangle();
        return (map != null ? map.hasCollided(bounds) : false);
    }
}
