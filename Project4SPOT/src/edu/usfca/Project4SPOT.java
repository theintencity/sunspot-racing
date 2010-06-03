package edu.usfca;

import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.peripheral.radio.IRadioPolicyManager;
import com.sun.spot.util.*;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The main SPOT application that starts two threads: one to send data and one to
 * receive data from the base station.
 *
 * @author mamta
 */
public class Project4SPOT extends MIDlet {
    private static final int POWER_LEVEL = -20;
    
    private IRadioPolicyManager policyManager = RadioFactory.getRadioPolicyManager();

    protected void startApp() throws MIDletStateChangeException {
        //listens to commands over USB
        new BootloaderListener().start();

        // change the output power to low initially.
        policyManager.setOutputPower(POWER_LEVEL);

        new Thread(new DataSender()).start();
        new Thread(new DataReceiver()).start();
    }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }

}
