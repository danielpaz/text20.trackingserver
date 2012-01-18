package de.dfki.km.text20.trackingserver.eyes.adapter.impl.tobiiAPI3;

import static net.jcores.jre.CoreKeeper.$;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jcores.jre.interfaces.functions.F0R;
import net.jcores.jre.interfaces.functions.F1;
import net.text20.devices.tobii.Browser;
import net.text20.devices.tobii.BrowserListener;
import net.text20.devices.tobii.Device;
import net.text20.devices.tobii.DeviceInfo;
import net.text20.devices.tobii.GazeEvent;
import net.text20.devices.tobii.GazeListener;
import net.text20.devices.tobii.SDK;
import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
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
 * Connects to a Tobii device.
 * 
 * @author Ralf Biedert
 * 
 */
@Author(name = "Ralf Biedert")
@Version
@PluginImplementation
public class TobiiGazeAdapterV3 implements GazeAdapter, GazeListener {

    /** Needed to obtain Tobii specific configuration */
    @InjectPlugin
    public PluginConfiguration rawConfiguration;

    /** Reflects infos on the tracking device */
    private TrackingDeviceInformation trackingDeviceInfo;

    /** Master queue for submitted events */
    protected BlockingQueue<TrackingEvent> dequeue;

    /** */
    final Lock callbacksLock = new ReentrantLock();

    /** */
    protected SDK sdk;

    /** */
    protected Browser browser;

    /** */
    protected String deviceName;

    private Dimension screenSize;

    /** */
    @Init
    public void init() {
        final PluginConfigurationUtil util = new PluginConfigurationUtil(this.rawConfiguration);
        this.deviceName = util.getString(getClass(), "device.name");
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.km.text20.trackingserver.adapter.GazeAdapter#adapterCommand(de
     * .dfki.km.text20.trackingserver.adapter.AdapterCommand,
     * de.dfki.km.text20.trackingserver.adapter.options.AdapterCommandOption[])
     */
    @Override
    public void adapterCommand(final AdapterCommand command,
                               final AdapterCommandOption... options) {

        switch (command) {
        case CALIBRATE:
            // calibrateAdapter(options);
            break;
        // case CALIBRATE_PRINT: calibratorV2.printout(); break;
        default:
            break;
        }

    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "gazeadapter:tobii:sdk3" };
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#setup(java
     * .util.concurrent.BlockingQueue)
     */
    @Override
    public void setup(final BlockingQueue<TrackingEvent> eventQueue) {
        this.dequeue = eventQueue;

        $.async(new F0R<Void>() {
            @Override
            public Void f() {
                TobiiGazeAdapterV3.this.sdk = SDK.get();
                TobiiGazeAdapterV3.this.browser = TobiiGazeAdapterV3.this.sdk.browser();
                TobiiGazeAdapterV3.this.browser.addListener(new BrowserListener() {

                    @Override
                    public void removed(DeviceInfo arg0) {}

                    @Override
                    public void changed(DeviceInfo arg0) {}

                    @Override
                    public void added(final DeviceInfo arg0) {
                        System.out.println("Detected device '" + arg0.getGivenName() + "'");

                        if (!TobiiGazeAdapterV3.this.deviceName.equals(arg0.getGivenName()))
                            return;

                        final Future<Device> device = TobiiGazeAdapterV3.this.sdk.device(arg0);
                        
                        $(device).onNext(new F1<Device, Void>() {
                            @Override
                            public Void f(Device a) {
                                System.out.println("Adding gaze listener ... " + a);
                                try {
                                    a.addGazeListener(TobiiGazeAdapterV3.this);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        });
                    }
                });

                return null;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#start()
     */
    @Override
    public void start() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#stop()
     */
    @Override
    public void stop() {}

    /*
     * (non-Javadoc)
     * 
     * @see net.text20.devices.tobii.GazeListener#onGaze(net.text20.devices.tobii.GazeEvent)
     */
    @Override
    public void onGaze(GazeEvent event) {
        double[] l = event.getLeftGazePoint2D();
        double[] r = event.getRightGazePoint2D();
        double[] c = new double[2];

        c[0] = l[0] + r[0];
        c[1] = l[1] + r[1];
        c[0] /= 2;
        c[1] /= 2;

        TrackingEvent rval = new TrackingEvent();
        rval.pupilSizeLeft = (float) event.getLeftPupilSize();
        rval.pupilSizeRight = (float) event.getRightPupilSize();
        rval.observationTime = event.getArrivalTimestamp();
        rval.centerGaze = new Point();
        rval.centerGaze.x = (int) (c[0] * this.screenSize.width);
        rval.centerGaze.y = (int) (c[1] * this.screenSize.height);

        double[] leftEye = event.getLeftEyePosition3DRelative();
        double[] rightEye = event.getRightEyePosition3DRelative();

        rval.leftEyePos[0] = (float) leftEye[0];
        rval.leftEyePos[1] = (float) leftEye[1];
        rval.leftEyePos[2] = (float) leftEye[2];

        rval.rightEyePos[0] = (float) rightEye[0];
        rval.rightEyePos[1] = (float) rightEye[1];
        rval.rightEyePos[2] = (float) rightEye[2];

        rval.eyeDistances[0] = (float) event.getLeftEyePosition3D()[2];
        rval.eyeDistances[1] = (float) event.getRightEyePosition3D()[2];

        this.dequeue.add(rval);
    }
}
