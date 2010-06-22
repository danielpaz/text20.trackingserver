package de.dfki.km.text20.trackingserver.eyes.adapter.impl.simulator;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import de.dfki.km.text20.trackingserver.eyes.adapter.AdapterCommand;
import de.dfki.km.text20.trackingserver.eyes.adapter.GazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;
 
/**
 * Simulates events
 * 
 * @author Ralf Biedert
 * 
 */
@Author(name = "Ralf Biedert")
@Version
@PluginImplementation
public class SimulatingGazeAdapter implements GazeAdapter {

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    @InjectPlugin
    public PluginConfiguration rawConfiguration;

    /** */
    private TrackingDeviceInformation trackingDeviceInfo;

    /** */
    BlockingQueue<TrackingEvent> queue;

    /** */
    final Random r = new Random();

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#setup(java
     * .util.concurrent.BlockingQueue)
     */
    public void setup(BlockingQueue<TrackingEvent> eventQueue) {
        this.trackingDeviceInfo = new TrackingDeviceInformation();
        this.queue = eventQueue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#start()
     */
    @SuppressWarnings("boxing")
    public void start() {
        SimulatingGazeAdapter.this.logger.info("Starting tracking.");

        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.rawConfiguration);

        new Thread() {

            final int baseValue = pcu.getInt(SimulatingGazeAdapter.class, "fixation.duration.base", 150);
            final int variableValue = pcu.getInt(SimulatingGazeAdapter.class, "fixation.duration.variable", 500);
            final int xoffset = pcu.getInt(SimulatingGazeAdapter.class, "fixation.inaccuracy.x", 20);
            final int yoffset = pcu.getInt(SimulatingGazeAdapter.class, "fixation.inaccuracy.y", 20);
            final int xnoise = pcu.getInt(SimulatingGazeAdapter.class, "device.inaccuracy.x", 5);
            final int ynoise = pcu.getInt(SimulatingGazeAdapter.class, "device.inaccuracy.y", 5);

            
            int currentDuration = this.baseValue;

            int sleepValue = 10;

            Point current = new Point(0, 0);

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void run() {

                while (true) {

                    // Returns the base location on screen
                    final Point location = MouseInfo.getPointerInfo().getLocation();

                    // Generate new next fixation duration
                    if (this.currentDuration < 0) {
                        this.currentDuration = baseValue + SimulatingGazeAdapter.this.r.nextInt(variableValue);

                        // Simluate near mouse 
                        this.current.x = location.x + SimulatingGazeAdapter.this.r.nextInt(2 * xoffset) - xoffset;
                        this.current.y = location.y + SimulatingGazeAdapter.this.r.nextInt(2 * yoffset) - yoffset;

                    }

                    // Generate noisy point
                    Point toSend = new Point(this.current.x + (SimulatingGazeAdapter.this.r.nextInt(2*xnoise) - xnoise), this.current.y + (SimulatingGazeAdapter.this.r.nextInt(2*ynoise) - ynoise));

                    // Generate tracking event
                    final TrackingEvent trackingEvent = new TrackingEvent();
                    trackingEvent.leftEyePos[0] = 0.40f;
                    trackingEvent.leftEyePos[1] = 0.5f;
                    trackingEvent.leftEyePos[2] = 0.5f;

                    trackingEvent.rightEyePos[0] = 0.60f;
                    trackingEvent.rightEyePos[1] = 0.5f;
                    trackingEvent.rightEyePos[2] = 0.5f;

                    trackingEvent.pupilSizeLeft = 3.0f;
                    trackingEvent.pupilSizeRight = 3.0f;

                    trackingEvent.centerGaze = toSend;
                    trackingEvent.leftGaze = toSend;
                    trackingEvent.rightGaze = toSend;

                    trackingEvent._centerX = toSend.x;
                    trackingEvent._centerY = toSend.y;
                    trackingEvent._centerValidity = true;

                    trackingEvent.date = System.currentTimeMillis();

                    // Dispatch the event
                    SimulatingGazeAdapter.this.queue.add(trackingEvent);

                    // Reduce fixation time
                    this.currentDuration -= this.sleepValue;

                    // sleep
                    try {
                        Thread.sleep(this.sleepValue);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#stop()
     */
    public void stop() {
        // TODO
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#
     * getDeviceInformation()
     */
    public TrackingDeviceInformation getDeviceInformation() {
        return this.trackingDeviceInfo;
    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "gazeadapter:simulator" };
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.adapter.GazeAdapter#adapterCommand(de.dfki.km.text20.trackingserver.adapter.AdapterCommand, de.dfki.km.text20.trackingserver.adapter.options.AdapterCommandOption[])
     */
    public void adapterCommand(AdapterCommand command, AdapterCommandOption... options) {
        //
    }
}
