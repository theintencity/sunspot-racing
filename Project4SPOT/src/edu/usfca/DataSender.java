package edu.usfca;

import com.sun.spot.io.j2me.radiogram.Radiogram;
import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.LIS3L02AQAccelerometer;
import com.sun.spot.util.Utils;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * The thread to send periodic accelerometer readings to the base station.
 * This allows the SPOT to act as a input device. Only the tilt reading in
 * each dimension is sent as a three byte packet.
 *
 * @author mamta
 */
public class DataSender implements Runnable {
    // Send data to base station every interval (in milliseconds)
    private static final int INTERVAL = 100;

    // port number to send data to base station
    private static final int PORT = 40;

    private LIS3L02AQAccelerometer acc;
    private RadiogramConnection conn = null;
    private Radiogram dg = null;

    /**
     * Get a reference to the accelerometer and create the send connection.
     */
    public DataSender() {
        acc = (LIS3L02AQAccelerometer)EDemoBoard.getInstance().getAccelerometer();
        acc.setScale(LIS3L02AQAccelerometer.SCALE_2G);        // start using 2G scale

        try {
            conn = (RadiogramConnection) Connector.open("radiogram://broadcast:" + String.valueOf(PORT));
            conn.setMaxBroadcastHops(1);
            dg = (Radiogram) conn.newDatagram(3);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Thread method to periodically send the accelerometer tilt information
     * to the base station.
     */
    public void run() {
        while (true) {
            Utils.sleep(INTERVAL);

            try {
                int x = (int) Math.toDegrees(acc.getTiltX());
                int y = (int) Math.toDegrees(acc.getTiltY());
                int z = (int) Math.toDegrees(acc.getTiltZ());

                dg.reset();
                dg.writeByte(x);
                dg.writeByte(y);
                dg.writeByte(z);
                conn.send(dg);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
    }
}


