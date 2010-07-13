package de.dfki.km.text20.trackingserver.eyes.adapter.impl.tobii;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.annotations.meta.Author;
import net.xeoh.plugins.base.annotations.meta.Version;
import net.xeoh.plugins.base.util.OptionUtils;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
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
    public PluginConfiguration rawConfiguration;

    /** */
    private TrackingDeviceInformation trackingDeviceInfo;

    /** */
    protected BlockingQueue<TrackingEvent> dequeue;

    /** */
    final List<TrackingClientCallback> allCallbacks = new ArrayList<TrackingClientCallback>();

    // TODO: Create superclass and unify the two calibrators
    /** */
    final TobiiCalibratorV2 calibratorV2 = new TobiiCalibratorV2();
    
    /** */
    final TobiiCalibratorV5 calibratorV5 = new TobiiCalibratorV5();

    /** */
    final Lock callbacksLock = new ReentrantLock();

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    float maxDistance = 700;

    /** */
    float minDistance = 200;

    /** */
    final BlockingQueue<TrackingEvent> trackingEvents = new LinkedBlockingQueue<TrackingEvent>();

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
            calibrateAdapter(options);
            break;
        //    	case CALIBRATE_PRINT: calibratorV2.printout(); break;
        default:
            this.logger.warning("Unknown command " + command);
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
        try {
            this.dequeue = eventQueue;
            this.logger.info("Initializing Eye-Tracker connection");

            final PluginConfigurationUtil configuration = new PluginConfigurationUtil(this.rawConfiguration);
            final FilterStage filter = new Filter(this);

            EyeTracker2Java.initialize();

            
            // Obtain remote TET ip
            final String remoteTETServer = configuration.getString(getClass(), "tobii.server", "127.0.0.1");

            // Set IP to preferences
            final Preferences p = PreferencesUtil.newPreferences();

            
            
            // Setting up Tet Session depending on the api version given in the config file
            final String tetApiVersion = configuration.getString(TobiiGazeAdapter.class, "tobii.api");
            
            if ("v2".equals(tetApiVersion)) {
                p.node(FilterRegistry.GLOBAL_PREFERENCES).put(LiveTetSessionV2.PREFKEY_TET_SERVER, remoteTETServer);
                this.logger.info("Using Tet Api Version 2");
            }
            if ("v5".equals(tetApiVersion)) {
                p.node(FilterRegistry.GLOBAL_PREFERENCES).put(LiveTetSessionV5.PREFKEY_TET_SERVER, remoteTETServer);
                this.logger.info("Using Tet Api Version 5");
            }
            
            
            // Setup screensizes (TODO: Do we really need this?)
            p.node(FilterRegistry.GLOBAL_PREFERENCES).put("SCREEN_WIDTH", configuration.getString(TrackingServerRegistryImpl.class, "screen.width"));
            p.node(FilterRegistry.GLOBAL_PREFERENCES).put("SCREEN_HEIGHT", configuration.getString(TrackingServerRegistryImpl.class, "screen.height"));

            this.minDistance = configuration.getFloat(TobiiGazeAdapter.class, "device.distance.min", 200f);
            this.maxDistance = configuration.getFloat(TobiiGazeAdapter.class, "device.distance.max", 700f);

            
            // Get TET port
            final int remoteTETServerPort = configuration.getInt(getClass(), "tobii.port", 4455);
           
            
            // Setting up calibrator
            if ("v2".equals(tetApiVersion)) {
                this.calibratorV2.buildUp(remoteTETServer, remoteTETServerPort);
            }
            if ("v5".equals(tetApiVersion)) {
                this.calibratorV5.buildUp(remoteTETServer, remoteTETServerPort);
            }

            
            
            // Create new EyeTracker2Java session and filter
            final TrackingSession session;
            if ("v2".equals(tetApiVersion)) {
                session = TrackingSessionManager.getInstance().createSession(SessionMode.LiveTetSessionV2, p);
            } else
            if ("v5".equals(tetApiVersion)) {
                session = TrackingSessionManager.getInstance().createSession(SessionMode.LiveTetSessionV5, p);
            } else {
                session = null;
                this.logger.warning("Unexpected Error. No TrackingSession instantiated!");
            }
            
            
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
            filter.connectToOutput(output);
        } catch (final EyetrackerException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#start()
     */
    @Override
    public void start() {
        TobiiGazeAdapter.this.logger.info("Starting tracking ...");

        try {
            TrackingSessionManager.getInstance().startTracking();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.augmentedtext.trackingserver.adapter.GazeAdapter#stop()
     */
    @Override
    public void stop() {
        TobiiGazeAdapter.this.logger.info("Stopping tracking ...");
        try {
            TrackingSessionManager.getInstance().stopTracking();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param options
     */
    private void calibrateAdapter(final AdapterCommandOption... options) {
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
    }
}
