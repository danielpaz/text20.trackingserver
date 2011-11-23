package de.dfki.km.text20.trackingserver.eyes.adapter.impl.itu;

import static net.jcores.jre.CoreKeeper.$;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import net.jcores.jre.cores.CoreString;
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
import de.dfki.km.text20.trackingserver.common.measurement.util.TimingUtil;
import de.dfki.km.text20.trackingserver.eyes.adapter.AdapterCommand;
import de.dfki.km.text20.trackingserver.eyes.adapter.GazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.impl.simulator.SimulatingGazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;

/**
 * Takes ITU Gaze Tracker events.
 * 
 * @author Ralf Biedert
 */
@Author(name = "Ralf Biedert")
@Version
@PluginImplementation
public class ITUGazeAdapter implements GazeAdapter {
    /** */
    @InjectPlugin
    public Diagnosis diagnosis;

    /** */
    @InjectPlugin
    public PluginConfiguration rawConfiguration;

    /** */
    @InjectPlugin
    public TimingUtil timing;

    /** */
    private TrackingDeviceInformation trackingDeviceInfo;

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

        final int dataport = pcu.getInt(SimulatingGazeAdapter.class, "adapter.data.port", 6666);
        final AtomicLong firstTime = new AtomicLong();

        this.log = this.diagnosis.channel(CommonAdapterTracer.class);
        this.thread = new Thread() {
            @SuppressWarnings("unqualified-field-access")
            @Override
            public void run() {
                log.status("start/thread/run");

                try {
                    // Setup DGRAM socket to receive data events
                    final DatagramSocket serverSocket = new DatagramSocket(dataport);
                    byte[] receiveData = new byte[1024];

                    // And now receive data packets
                    while (true) {
                        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);

                        // Example:
                        // STREAM_DATA 63457562743747 1276.407 263.638 1600094406
                        final String sentence = new String(receivePacket.getData());

                        final CoreString split = $(sentence).split(" ");
                        if (!"STREAM_DATA".equals(split.get(0))) continue;

                        final long time = Long.parseLong(split.get(1));
                        final double x = split.d(2);
                        final double y = split.d(3);

                        // Set firstTime if still 0
                        firstTime.compareAndSet(0, time);

                        final long dt = time - firstTime.get();

                        // Generate tracking event
                        final TrackingEvent trackingEvent = new TrackingEvent();
                        timing.initEvent(trackingEvent);

                        trackingEvent.leftEyePos[0] = 0.40f;
                        trackingEvent.leftEyePos[1] = 0.5f;
                        trackingEvent.leftEyePos[2] = 0.5f;

                        trackingEvent.gazeLeftPos[0] = (float) (x / screenSize.width);
                        trackingEvent.gazeLeftPos[1] = (float) (y / screenSize.height);

                        trackingEvent.gazeRightPos[0] = (float) (x / screenSize.width);
                        trackingEvent.gazeRightPos[1] = (float) (y / screenSize.height);

                        trackingEvent.rightEyePos[0] = 0.60f;
                        trackingEvent.rightEyePos[1] = 0.5f;
                        trackingEvent.rightEyePos[2] = 0.5f;

                        trackingEvent.pupilSizeLeft = 3.0f;
                        trackingEvent.pupilSizeRight = 3.0f;

                        trackingEvent.centerGaze = new Point((int) x, (int) y);
                        trackingEvent.leftGaze = new Point((int) x, (int) y);
                        trackingEvent.rightGaze = new Point((int) x, (int) y);

                        // Dispatch the event
                        ITUGazeAdapter.this.queue.add(trackingEvent);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        this.trackingDeviceInfo.deviceName = "ITU Gaze Tracker 2.1";
        this.trackingDeviceInfo.hardwareID = "itu/v2.1";
        this.trackingDeviceInfo.trackingDeviceManufacturer = "gazegroup.org";

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
        return new String[] { "gazeadapter:itu21" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.km.text20.trackingserver.adapter.GazeAdapter#adapterCommand(de.dfki.km.text20.trackingserver.adapter.
     * AdapterCommand, de.dfki.km.text20.trackingserver.adapter.options.AdapterCommandOption[])
     */
    @Override
    public void adapterCommand(AdapterCommand command, AdapterCommandOption... options) {
        this.log.status("adaptercommand/call", new OptionInfo("command", command.toString()));
    }
}
