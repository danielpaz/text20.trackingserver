package de.dfki.km.text20.trackingserver.eyes.adapter.impl.replay;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;
import de.dfki.km.text20.trackingserver.common.adapter.diagnosis.channels.tracing.CommonAdapterTracer;
import de.dfki.km.text20.trackingserver.eyes.adapter.AdapterCommand;
import de.dfki.km.text20.trackingserver.eyes.adapter.GazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;

/**
 * Simulates events based on a previously recorder or programmed replay. The adapter should
 * 
 * <ul>
 * <li>replay gaze from a simple file</li>
 * <li>use behave like the simulating adapter when no replay is being performed</li>
 * <li>thus have the states IDLE and REPLAY</li>
 * <li>be able to loop (indefinitely)</li>
 * <li>support programmatic replay (i.e., JARs with a plugin that control the replay)</li>
 * <li>add a hot-key to switch from IDLE to REPLAY and vice versa</li>
 *  
 * </ul>
 * 
 * @author Ralf Biedert
 * 
 */
@Author(name = "Ralf Biedert")
@Version
@PluginImplementation
public class ReplayGazeAdapter implements GazeAdapter {

    /** */
    @InjectPlugin
    public Diagnosis diagnosis;
    
    /** */
    @InjectPlugin
    public PluginConfiguration rawConfiguration;

    /** */
    private TrackingDeviceInformation trackingDeviceInfo;
    /** */
    final Random r = new Random();

    /** */
    BlockingQueue<TrackingEvent> queue;
    
    /** Used for tracing */
    DiagnosisChannel<String> log;

    private Thread thread;

    /** */
    @Init
    @SuppressWarnings("boxing")
    public void init() {
        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.rawConfiguration);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        this.log = this.diagnosis.channel(CommonAdapterTracer.class);
        this.thread = new Thread() {

            final int baseValue = pcu.getInt(ReplayGazeAdapter.class, "fixation.duration.base", 150);
            final int variableValue = pcu.getInt(ReplayGazeAdapter.class, "fixation.duration.variable", 500);
            final int xoffset = pcu.getInt(ReplayGazeAdapter.class, "fixation.inaccuracy.x", 20);
            final int yoffset = pcu.getInt(ReplayGazeAdapter.class, "fixation.inaccuracy.y", 20);
            final int xnoise = pcu.getInt(ReplayGazeAdapter.class, "device.inaccuracy.x", 5);
            final int ynoise = pcu.getInt(ReplayGazeAdapter.class, "device.inaccuracy.y", 5);

            int currentDuration = this.baseValue;

            int sleepValue = 10;

            Point current = new Point(0, 0);

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void run() {
                log.status("start/thread/run");
                while (true) {

                    // Returns the base location on screen
                    final Point location = MouseInfo.getPointerInfo().getLocation();

                    // Generate new next fixation duration
                    if (this.currentDuration < 0) {
                        this.currentDuration = baseValue + ReplayGazeAdapter.this.r.nextInt(variableValue);

                        // Simluate near mouse 
                        this.current.x = location.x + ReplayGazeAdapter.this.r.nextInt(2 * xoffset) - xoffset;
                        this.current.y = location.y + ReplayGazeAdapter.this.r.nextInt(2 * yoffset) - yoffset;

                    }

                    // Generate noisy point
                    Point toSend = new Point(this.current.x + (ReplayGazeAdapter.this.r.nextInt(2 * xnoise) - xnoise), this.current.y + (ReplayGazeAdapter.this.r.nextInt(2 * ynoise) - ynoise));

                    // Generate tracking event
                    final TrackingEvent trackingEvent = new TrackingEvent();
                    trackingEvent.leftEyePos[0] = 0.40f;
                    trackingEvent.leftEyePos[1] = 0.5f;
                    trackingEvent.leftEyePos[2] = 0.5f;
                    
                    trackingEvent.gazeLeftPos[0] = this.current.x / (float) screenSize.width; 
                    trackingEvent.gazeLeftPos[1] = this.current.y / (float) screenSize.height;
                    
                    trackingEvent.gazeRightPos[0] = this.current.x / (float) screenSize.width; 
                    trackingEvent.gazeRightPos[1] = this.current.y / (float) screenSize.height; 

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
                    ReplayGazeAdapter.this.queue.add(trackingEvent);

                    // Reduce fixation time
                    this.currentDuration -= this.sleepValue;

                    // sleep
                    try {
                        Thread.sleep(this.sleepValue);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        };
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
        this.trackingDeviceInfo.deviceName = "Simulator";
        this.trackingDeviceInfo.hardwareID = "simulator/v0";
        this.trackingDeviceInfo.trackingDeviceManufacturer = "Text 2.0 Project";

        this.log.status("setup/call", new OptionInfo("name", this.trackingDeviceInfo.deviceName));
        
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

        this.thread.start();
        this.log.status("start/end");        
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#stop()
     */
    @Override
    public void stop() {
        this.log.status("stop/call");
        this.thread.interrupt();
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
        return new String[] { "gazeadapter:replay" };
    }
    
    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.adapter.GazeAdapter#adapterCommand(de.dfki.km.text20.trackingserver.adapter.AdapterCommand, de.dfki.km.text20.trackingserver.adapter.options.AdapterCommandOption[])
     */
    @Override
    public void adapterCommand(AdapterCommand command, AdapterCommandOption... options) {
        this.log.status("adaptercommand/call", new OptionInfo("command", command.toString()));
    }
}
