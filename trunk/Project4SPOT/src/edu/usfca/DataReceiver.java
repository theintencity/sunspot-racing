package edu.usfca;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * The data receiver thread that opens a receiving connection and handles
 * any commands from the base station. Currently only one command is defined
 * to SET_COLOR where the basestation assigns a color to the SPOT and the
 * SPOT displays the color on the first LED.
 *
 * @author mamta
 */
public class DataReceiver implements Runnable {
    // The listening PORT number
    private static final int PORT = 41;

    // The code for command to set color
    private static final int SET_COLOR = 80;

    private static EDemoBoard demoBoard = EDemoBoard.getInstance();
    private static ITriColorLED leds[] = demoBoard.getLEDs();
    
    private RadiogramConnection conn = null;
    private Radiogram dg = null;
    private long myaddress;

    /**
     * Create a new receiving connection.
     */
    public DataReceiver() {
        myaddress = Spot.getInstance().getRadioPolicyManager().getIEEEAddress();
        
        try {
            conn = (RadiogramConnection) Connector.open("radiogram://:" + String.valueOf(PORT));
            dg = (Radiogram) conn.newDatagram(16);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The thread method listens for packet, and for valid command of set color,
     * it checks whether the target address is this SPOT's address. It then
     * extracts the RGB of the color from the received packet and assigns it to
     * the first LED.
     */
    public void run() {
        while (true) {
            try {
                dg.reset();
                conn.receive(dg);
                if (dg.getLength() <= 0)
                    continue;

                byte command = dg.readByte();
                if (command == SET_COLOR) {
                    long addr = dg.readLong();
                    if (addr == myaddress) {
                        int r = dg.readByte();
                        int g = dg.readByte();
                        int b = dg.readByte();
                        if ((r == 0) && (g == 0) && (b == 0)) {
                            leds[0].setOff();
                        }
                        else {
                            LEDColor color = new LEDColor(r & 0x0ff, g & 0x0ff, b &0x0ff);
                            leds[0].setOn();
                            leds[0].setColor(color);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
