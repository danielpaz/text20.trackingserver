package de.dfki.km.text20.trackingserver.eyes.adapter.impl.dummy;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import de.dfki.km.text20.trackingserver.eyes.adapter.AdapterCommand;
import de.dfki.km.text20.trackingserver.eyes.adapter.GazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;

/**
 * Connects to a Tobii device.
 * 
 * @author Ralf Biedert
 * 
 */
@Author(name = "Ralf Biedert")
@Version
@PluginImplementation
public class DummyGazeAdapter implements GazeAdapter {

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    @InjectPlugin
    public PluginConfiguration rawConfiguration;

    /** */
    private TrackingDeviceInformation trackingDeviceInfo;

    /** */
    BlockingQueue<TrackingEvent> queue;

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
    public void start() {
        DummyGazeAdapter.this.logger.info("Starting tracking.");

        new Thread() {
            @Override
            public void run() {

                while (true) {
                    final TrackingEvent trackingEvent = new TrackingEvent();

                    final long time = System.currentTimeMillis() % 2000;
                    final double percentageTime = (time / 2000.0) * Math.PI;

                    final double value = Math.sin(percentageTime) * 0.8;
                    //                    final double value = percentageTime * 0.8;

                    trackingEvent.leftEyePos[0] = (float) value;
                    trackingEvent.leftEyePos[1] = (float) value;
                    trackingEvent.leftEyePos[2] = (float) value;

                    trackingEvent.rightEyePos[0] = (float) value + 0.1f;
                    trackingEvent.rightEyePos[1] = (float) value + 0.1f;
                    trackingEvent.rightEyePos[2] = (float) value;

                    trackingEvent.pupilSizeLeft = 1.5f + trackingEvent.rightEyePos[0];
                    trackingEvent.pupilSizeRight = 2.0f;

                    trackingEvent.date = System.currentTimeMillis();

                    DummyGazeAdapter.this.queue.add(trackingEvent);
                    try {
                        Thread.sleep(15);
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
        return new String[] { "gazeadapter:dummy" };
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.adapter.GazeAdapter#adapterCommand(de.dfki.km.text20.trackingserver.adapter.AdapterCommand, de.dfki.km.text20.trackingserver.adapter.options.AdapterCommandOption[])
     */
    public void adapterCommand(AdapterCommand command, AdapterCommandOption... options) {
        //
    }
}
