package de.dfki.km.text20.trackingserver.eyes.adapter.impl.tobii;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.prefs.Preferences;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;
import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;
import de.dfki.eyetracker.EyeTracker2Java;
import de.dfki.eyetracker.EyetrackerException;
import de.dfki.eyetracker.filter.FilterChainBuilder;
import de.dfki.eyetracker.filter.FilterDefinition;
import de.dfki.eyetracker.filter.FilterOutput;
import de.dfki.eyetracker.filter.FilterRegistry;
import de.dfki.eyetracker.filter.FilterStage;
import de.dfki.eyetracker.filter.impl.PixelCoordsFilter;
import de.dfki.eyetracker.filter.impl.SingleCoordCombinationFilter;
import de.dfki.eyetracker.session.LiveTetSessionV2;
import de.dfki.eyetracker.session.LiveTetSessionV5;
import de.dfki.eyetracker.session.SessionMode;
import de.dfki.eyetracker.session.TrackingSession;
import de.dfki.eyetracker.session.TrackingSessionManager;
import de.dfki.eyetracker.util.PreferencesUtil;
import de.dfki.km.text20.trackingserver.common.adapter.diagnosis.channels.tracing.CommonAdapterTracer;
import de.dfki.km.text20.trackingserver.eyes.adapter.AdapterCommand;
import de.dfki.km.text20.trackingserver.eyes.adapter.GazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand.OptionCalibratorColor;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand.OptionCalibratorNumPoints;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand.OptionCalibratorPointSpeed;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingClientCallback;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;
import de.dfki.km.text20.trackingserver.eyes.remote.impl.TrackingServerRegistryImpl;

/**
 * Connects to a Tobii device.
 * 
 * @author Ralf Biedert
 * 
 */
@Author(name = "Ralf Biedert")
@Version
@PluginImplementation
public class TobiiGazeAdapter implements GazeAdapter {

    /** */
    @InjectPlugin
    public Diagnosis diagnosis;

    /** */
    @InjectPlugin
    public PluginConfiguration rawConfiguration;

    /** */
    private TrackingDeviceInformation trackingDeviceInfo;

    /** */
    protected BlockingQueue<TrackingEvent> dequeue;

    /** */
    final List<TrackingClientCallback> allCallbacks = new ArrayList<TrackingClientCallback>();

    // TODO: Create superclass and unify the two calibrators
    /** */
    TobiiCalibratorV2 calibratorV2;
    
    /** */
    TobiiCalibratorV5 calibratorV5;

    /** */
    final Lock callbacksLock = new ReentrantLock();

    /** */
    float maxDistance = 700;

    /** */
    float minDistance = 200;

    /** */
    final BlockingQueue<TrackingEvent> trackingEvents = new LinkedBlockingQueue<TrackingEvent>();
    
    /** Used for tracing */
    protected DiagnosisChannel<String> log;

    /** */
    @Init
    public void init() {
        this.log = this.diagnosis.channel(CommonAdapterTracer.class);
        this.calibratorV2 = new TobiiCalibratorV2(this);
        this.calibratorV5 = new TobiiCalibratorV5(this);
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
        this.log.status("adaptercommand/call", new OptionInfo("command", command.toString()));
        
        switch (command) {
        case CALIBRATE:
            calibrateAdapter(options);
            break;
        //    	case CALIBRATE_PRINT: calibratorV2.printout(); break;
        default:
            this.log.status("adaptercommand/unhandledcommand", new OptionInfo("command", command.toString()));            
            break;
        }

    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "gazeadapter:tobii" };
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
    @SuppressWarnings("boxing")
    public void setup(final BlockingQueue<TrackingEvent> eventQueue) {
        this.log.status("setup/start");            
        try {
            this.dequeue = eventQueue;
            
            this.log.status("setup/init/pre");
            final PluginConfigurationUtil configuration = new PluginConfigurationUtil(this.rawConfiguration);
            final FilterStage filter = new Filter(this);
            
            EyeTracker2Java.initialize();
            this.log.status("setup/init/post");            

            
            // Obtain remote TET ip
            final String remoteTETServer = configuration.getString(TobiiGazeAdapter.class, "tobii.server", "127.0.0.1");
            final String tetApiVersion = configuration.getString(TobiiGazeAdapter.class, "tobii.api");
            final int remoteTETServerPort = configuration.getInt(TobiiGazeAdapter.class, "tobii.port", 4455);
            this.minDistance = configuration.getFloat(TobiiGazeAdapter.class, "device.distance.min", 200f);
            this.maxDistance = configuration.getFloat(TobiiGazeAdapter.class, "device.distance.max", 700f);

            // Set IP to preferences
            final Preferences p = PreferencesUtil.newPreferences();
            
            this.log.status("setup/config", new OptionInfo("tobii.server", remoteTETServer), new OptionInfo("tobii.api", tetApiVersion), new OptionInfo("tobii.port", "" + remoteTETServerPort));
            
            if ("v2".equals(tetApiVersion)) {
                p.node(FilterRegistry.GLOBAL_PREFERENCES).put(LiveTetSessionV2.PREFKEY_TET_SERVER, remoteTETServer);
            }
            if ("v5".equals(tetApiVersion)) {
                p.node(FilterRegistry.GLOBAL_PREFERENCES).put(LiveTetSessionV5.PREFKEY_TET_SERVER, remoteTETServer);
            }
            
            // Setup screensizes (TODO: Do we really need this?)
            p.node(FilterRegistry.GLOBAL_PREFERENCES).put("SCREEN_WIDTH", configuration.getString(TrackingServerRegistryImpl.class, "screen.width"));
            p.node(FilterRegistry.GLOBAL_PREFERENCES).put("SCREEN_HEIGHT", configuration.getString(TrackingServerRegistryImpl.class, "screen.height"));


            this.log.status("setup/buildup");            
            
            // Setting up calibrator
            if ("v2".equals(tetApiVersion)) {
                this.calibratorV2.buildUp(remoteTETServer, remoteTETServerPort);
            }
            if ("v5".equals(tetApiVersion)) {
                this.calibratorV5.buildUp(remoteTETServer, remoteTETServerPort);
            }

            
            

            // Create new EyeTracker2Java session and filter
            this.log.status("setup/createsession/pre");            
            final TrackingSession session;
            if ("v2".equals(tetApiVersion)) {
                session = TrackingSessionManager.getInstance().createSession(SessionMode.LiveTetSessionV2, p);
            } else
            if ("v5".equals(tetApiVersion)) {
                session = TrackingSessionManager.getInstance().createSession(SessionMode.LiveTetSessionV5, p);
            } else {
                session = null;
                this.log.status("setup/createsession/unknownmethod");            
            }
            this.log.status("setup/createsession/post");            

            
            // Prepare chain elements
            final FilterDefinition filterPixelCoords = new FilterDefinition(PixelCoordsFilter.class.getName());
            final FilterDefinition filterSingleCoords = new FilterDefinition(SingleCoordCombinationFilter.class.getName());

            
            // Create filter output
            final FilterOutput output = FilterChainBuilder.addFilterChain(session, filterPixelCoords, filterSingleCoords);

            
            // Setup device info
            this.trackingDeviceInfo = new TrackingDeviceInformation();
            this.trackingDeviceInfo.trackingDeviceManufacturer = "Tobii";
            this.trackingDeviceInfo.deviceName = configuration.getString(TobiiGazeAdapter.class, "device.name", "unknown");
            this.trackingDeviceInfo.hardwareID = configuration.getString(TobiiGazeAdapter.class, "device.id", "unknown");

            // And connect our filter
            this.log.status("setup/connect");            
            filter.connectToOutput(output);
        } catch (final EyetrackerException e) {
            this.log.status("setup/exception/eyetrackerexception", new OptionInfo("message", e.getMessage()));            
            e.printStackTrace();
        }
        
        this.log.status("setup/stop");                    
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#start()
     */
    @Override
    public void start() {
        this.log.status("start/start");            

        try {
            TrackingSessionManager.getInstance().startTracking();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
        this.log.status("start/end");            
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#stop()
     */
    @Override
    public void stop() {
        this.log.status("stop/start");            

        try {
            TrackingSessionManager.getInstance().stopTracking();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
        this.log.status("stop/end");                    
    }

    /**
     * @param options
     */
    private void calibrateAdapter(final AdapterCommandOption... options) {
        this.log.status("calibrate/start");            

        final OptionUtils<AdapterCommandOption> ou = new OptionUtils<AdapterCommandOption>(options);

        if (ou.contains(OptionCalibratorNumPoints.class)) {
            final OptionCalibratorNumPoints numPoints = ou.get(OptionCalibratorNumPoints.class);
            this.calibratorV2.setNumPoints(numPoints.getNumPoints());
            this.calibratorV5.setNumPoints(numPoints.getNumPoints());
        }

        if (ou.contains(OptionCalibratorColor.class)) {
            final OptionCalibratorColor color = ou.get(OptionCalibratorColor.class);
            this.calibratorV2.setColor(color.getPointColor(), color.getBgColor());
            this.calibratorV5.setColor(color.getPointColor(), color.getBgColor());
        }

        if (ou.contains(OptionCalibratorPointSpeed.class)) {
            final OptionCalibratorPointSpeed speed = ou.get(OptionCalibratorPointSpeed.class);
            this.calibratorV2.setPointSpeed(speed.getPointSpeed());
            this.calibratorV5.setPointSpeed(speed.getPointSpeed());
        }

        try {
            this.calibratorV2.calibrate();
            this.calibratorV5.calibrate();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        
        this.log.status("calibrate/stop");            
    }
}
