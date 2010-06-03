package edu.usfca;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;
import java.awt.Color;
import java.io.IOException;
import javax.microedition.io.Connector;


/**
 * The main base station application that launches the user interface as well as
 * conenctions to send and receive data from SPOTs.
 *
 * @author mamta
 */
public class Project4BaseStation {
    // the port number to receive data on from SPOT
    private static final int PORT_RECV = 40;

    // the port number to send data to SPOT
    private static final int PORT_SEND = 41;

    // the command code for setting the color of the SPOT LED.
    private static final int SET_COLOR = 80;

    // The main application user interface panel
    private ControlPanel panel;

    // the connection to send data to the SPOTs and recv data from SPOTs.
    private RadiogramConnection connSend;
    private Radiogram dgSend;
    private RadiogramConnection connRecv;
    private Radiogram dgRecv;

    /**
     * The main application method that opens connections, launches the control panel
     * user interface and then handles any incoming input packet from the SPOTs.
     */
    public void run() {
        try {
            // create the send connection
            connSend = (RadiogramConnection) Connector.open("radiogram://broadcast:" + String.valueOf(PORT_SEND));
            connSend.setMaxBroadcastHops(1);
            dgSend = (Radiogram) connSend.newDatagram(16);

            // create the receive connection
            connRecv = (RadiogramConnection) Connector.open("radiogram://:" + String.valueOf(PORT_RECV));
            dgRecv = (Radiogram) connRecv.newDatagram(4);

            // launch the user interface
            panel = new ControlPanel(this, "Project 4");

            while (true) {
                // receive packet
                dgRecv.reset();
                try {
                    connRecv.receive(dgRecv);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.sleep(1000);
                    continue;
                }

                // if this SPOT is not in our player list, add it
                String src = dgRecv.getAddress();
                if (!panel.hasPlayer(src)) {
                    panel.addPlayer(src);
                }

                // get the input readings from the packet, and
                // update the player state based on the input
                if (dgRecv.getLength() == 3) {
                    int xd = dgRecv.readByte();
                    int yd = dgRecv.readByte();
                    int zd = dgRecv.readByte();
                    panel.setAction(src, xd, yd, zd);
                }
                else {
                    System.out.println("empty data: " + dgRecv.getLength());
                }
            }

        } catch (IOException ex) {
            // recturn in case of exception
            return;
        }


    }

    /**
     * Send the color data to the SPOT address using the set color command
     * in the packet, target address and the color RGB values.
     *
     * @param addr
     * @param color
     */
    public synchronized void sendColor(String addr, Color color) {
        try {
            dgSend.reset();
            dgSend.writeByte(SET_COLOR);
            dgSend.writeLong(IEEEAddress.toLong(addr));
            dgSend.writeByte(color.getRed());
            dgSend.writeByte(color.getGreen());
            dgSend.writeByte(color.getBlue());
            connSend.send(dgSend);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Start up the host application.
     *
     * @param args any command line arguments
     */
    public static void main(String[] args) {
        Project4BaseStation app = new Project4BaseStation();
        app.run();
    }
}
