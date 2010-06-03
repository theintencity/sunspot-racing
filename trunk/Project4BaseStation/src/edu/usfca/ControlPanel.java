package edu.usfca;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The is the main user interface and controller of the application. It associated
 * the user interface components with the data model and forwards user actions.
 *
 * @author mamta
 */
public class ControlPanel extends JFrame implements ActionListener {
    // various colors in the user interface
    private static final Color bgColor = Color.DARK_GRAY;
    private static final Color fgColor = Color.WHITE;
    private static final Color bgColorCenter = Color.BLACK;
    private static final Color bgColorTop = Color.DARK_GRAY;

    // the main data model
    private GameData data;

    // other user interface components
    private JPanel centerPanel;
    private JLabel statusLabel;
    private JComboBox maptypeCombo;
    private JComboBox maplevelCombo;
    private JButton newmapButton;
    private JButton startgameButton;
    private JButton stopgameButton;

    // individual player views indexed by SPOT address
    private Map<String, PlayerView> playerViews = new Hashtable<String, PlayerView>();

    // the listener application that receives event for setColor
    private Project4BaseStation listener;

    /**
     * Construct the user interface, and generate the data model for the game.
     *
     * @param listener
     * @param title
     */
    public ControlPanel(Project4BaseStation listener, String title) {
        super(title);

        this.listener = listener;

        // create the game data with empty players list
        data = new GameData(this);
        data.setPlayers(new Hashtable<String, PlayerData>());

        // create other user interface components
        createComponents();

        // create the random map
        data.setMapData(MapData.createRandom((String) maptypeCombo.getSelectedItem(),
                                             (String) maplevelCombo.getSelectedItem()));

        statusLabel.setText("Start your SunSPOT(s) to join the game");
        validate();
        setVisible(true);
    }

    /**
     * Create main user interface components. This includes the top level
     * buttons, labels as well the the map view on left. The top level
     * contains two boxes to select the map type and difficulty level,
     * a button to regenerate a new map, and buttons to start and stop
     * the game, as well as a help text.
     */
    private void createComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1024, 768);
        setLocation(100, 100);
        setBackground(bgColor);

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(bgColorTop);
        topPanel.setPreferredSize(new Dimension(1024, 50));

        JComboBox combo1 = new JComboBox();
        for (int i=0; i<MapData.MAP_TYPES.length; ++i) {
            combo1.addItem(MapData.MAP_TYPES[i]);
        }
        maptypeCombo = combo1;

        JComboBox combo2 = new JComboBox();
        for (int i=0; i<MapData.MAP_LEVELS.length; ++i) {
            combo2.addItem(MapData.MAP_LEVELS[i]);
        }
        maplevelCombo = combo2;

        JButton button1 = new JButton("New Map");
        button1.setActionCommand("newmap");
        button1.addActionListener(this);
        newmapButton = button1;

        JButton button2 = new JButton("Start Game");
        button2.setActionCommand("startgame");
        //button2.setEnabled(false);
        button2.addActionListener(this);
        startgameButton = button2;

        JButton button3 = new JButton("Stop Game");
        button3.setActionCommand("stopgame");
        button3.setEnabled(false);
        button3.addActionListener(this);
        stopgameButton = button3;

        JLabel label1 = new JLabel();
        label1.setPreferredSize(new Dimension(350, 40));
        label1.setFont(new Font("Arial", Font.BOLD, 18));
        label1.setForeground(fgColor);
        statusLabel = label1;

        topPanel.add(combo1);
        topPanel.add(combo2);
        topPanel.add(button1);
        topPanel.add(button2);
        topPanel.add(button3);
        topPanel.add(label1);

        add(topPanel, BorderLayout.PAGE_START);

        MapView map = new MapView(this, data);
        map.setPreferredSize(new Dimension(200, 718));
        add(map, BorderLayout.LINE_START);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setPreferredSize(new Dimension(1024, 718));
        centerPanel.setBackground(bgColorCenter);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Get the game data model.
     *
     * @return
     */
    public GameData getData() {
        return data;
    }

    /**
     * Check whether the player for the given address exists in the game?
     *
     * @param addr
     * @return
     */
    public boolean hasPlayer(String addr) {
        return data.getPlayers().containsKey(addr);
    }

    /**
     * Add a new player for the given address. It adds the player in the game
     * data model as well as the view. It also updates the help text.
     * Finally it calls the callback on application to set the new player's
     * color.
     *
     * @param addr
     */
    public void addPlayer(String addr) {
        System.out.println("added " + addr);
        PlayerData player = data.addPlayer(addr);
        PlayerView playerView = new PlayerView(data, player);
        playerViews.put(addr, playerView);
        
        if (!data.isStarted()) {
            startgameButton.setEnabled(true);
            statusLabel.setText("Click on \"Start Game\" to start");
        }
        centerPanel.add(playerView, -1);
        repaint();
        validate();

        if (listener != null) {
            listener.sendColor(addr, player.getCarColor());
        }
    }

    /**
     * When a player is removed from the game, as detected by the MapView on
     * inactivity of player actions, the player view is removed from the
     * user interface also. The help text is updated as needed.
     *
     * @param player
     */
    public void removed(PlayerData player) {
        if (player != null) {
            System.out.println("removed " + player.getAddr());
            PlayerView playerView = playerViews.get(player.getAddr());
            playerViews.remove(player.getAddr());
            
            centerPanel.remove(playerView);
            if (data.getPlayers().isEmpty()) {
                data.stop();
                statusLabel.setText("Start your SunSPOT(s) to join the game");
            }
            repaint();
            validate();
        }
    }

    /**
     * The main application supplies the user action to this object. This object,
     * forwards the action to the player data and makes that player active.
     *
     * @param addr
     * @param xd
     * @param yd
     * @param zd
     */
    public void setAction(String addr, int xd, int yd, int zd) {
        PlayerData player = data.getPlayers().get(addr);
        if (player != null) {
            player.setActivity();
            if (!player.isCompletelyDamaged() && data.isStarted()) {
                player.setAction(xd, yd, zd);
            }
        }
    }

    /**
     * When the user clicks on some user interface button, take appropriate
     * action. For example, newmap button causes creation of a new
     * map data. The startgame button starts the game, and stop game
     * button stops the game. When stopping, if there are players in the
     * game, it prompts the user for confirmation. Depending on user action,
     * the buttons are enabled or disabled. For example, the stop button
     * is disabled if game is not started, whereas the newmap, combo boxes
     * as well as startgame buttons are disabled if game is started.
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        if ("newmap".equals(e.getActionCommand())) {
            data.setMapData(MapData.createRandom((String) maptypeCombo.getSelectedItem(),
                                                 (String) maplevelCombo.getSelectedItem()));
        }
        else if ("startgame".equals(e.getActionCommand())) {
            newmapButton.setEnabled(false);
            maptypeCombo.setEnabled(false);
            maplevelCombo.setEnabled(false);
            startgameButton.setEnabled(false);
            stopgameButton.setEnabled(true);

            data.start();
        }
        else if ("stopgame".equals(e.getActionCommand())) {
            if (!data.getPlayers().isEmpty()) {
                int selection = JOptionPane.showConfirmDialog(this, 
                        "Stop the game and lose this game data?",
                        null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (selection != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            maptypeCombo.setEnabled(true);
            newmapButton.setEnabled(true);
            maplevelCombo.setEnabled(true);
            startgameButton.setEnabled(true);
            stopgameButton.setEnabled(false);

            data.stop();
        }
    }
}
