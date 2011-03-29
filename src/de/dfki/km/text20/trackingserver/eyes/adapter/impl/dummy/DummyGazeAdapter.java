package de.dfki.km.text20.trackingserver.eyes.adapter.impl.dummy;

import java.awt.Point;
import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import de.dfki.km.text20.trackingserver.common.adapter.diagnosis.channels.tracing.CommonAdapterTracer;
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
    @InjectPlugin
    public Diagnosis diagnosis;
    
    /** */
    @InjectPlugin
    public PluginConfiguration rawConfiguration;

    /** */
    private TrackingDeviceInformation trackingDeviceInfo;

    /** */
    BlockingQueue<TrackingEvent> queue;

    /** Used for tracing */
    DiagnosisChannel<String> log;

    /** */
    @Init
    public void init() {
        this.log = this.diagnosis.channel(CommonAdapterTracer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#setup(java
     * .util.concurrent.BlockingQueue)
     */
    @Override
    public void setup(BlockingQueue<TrackingEvent> eventQueue) {
        this.trackingDeviceInfo = new TrackingDeviceInformation();
        this.queue = eventQueue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#start()
     */
    @Override
    public void start() {
        this.log.status("start/start");

        new Thread() {
            @Override
            public void run() {

                while (true) {
                    final TrackingEvent trackingEvent = new TrackingEvent();

                    final long time = System.currentTimeMillis() % 2000;
                    final double percentageTime = (time / 2000.0) * Math.PI;

                    final double value = Math.sin(percentageTime) * 0.8;
                    trackingEvent._centerValidity = true;

                    trackingEvent.centerGaze = new Point(1000, 1000);
                    trackingEvent.leftGaze = new Point(1000, 1000);
                    trackingEvent.rightGaze = new Point(1000, 1000);

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
                        //Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        
        this.log.status("start/end");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated catch block
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#
     * getDeviceInformation()
     */
    @Override
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
    @Override
    public void adapterCommand(AdapterCommand command, AdapterCommandOption... options) {
        //
    }
}
