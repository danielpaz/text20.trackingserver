package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1;

import java.util.HashMap;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.EDK;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_Event_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_ExpressivAlgo_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoEngineEventHandle;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoStateHandle;


/**
 * @author rb
 *
 */
public class LowLevelAdapter {

    /** Indicates whether the brain tracker is connected */
    protected boolean connected;

    /** Emotiv Wrapper */
    protected EDK edk;

    /** Brain tracker event */
    protected EmoEngineEventHandle eEvent;

    /** Handle to the emotional state */
    protected EmoStateHandle eState;

    /** */
    public PluginConfiguration rawConfiguration;

    /** State of the brain tracker */
    protected int state;

    /**
     * @param pluginManager
     */
    public LowLevelAdapter(final PluginManager pluginManager) {
        this.rawConfiguration = pluginManager.getPlugin(PluginConfiguration.class);
    }

    /** Connects to the brain tracker */
    @SuppressWarnings("boxing")
    public void connectToEngine() {

        this.edk = EDK.INSTANCE;

        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.rawConfiguration);
        final String connectorType = pcu.getString(LowLevelAdapter.class, "connector.type", "device");

        int connectionStatus = -1;

       
        if ("device".equals(connectorType)) {
            connectionStatus = this.edk.EE_EngineConnect();
        }
        
        if ("composer".equals(connectorType)) {
            final String simulatorServer = pcu.getString(LowLevelAdapter.class, "simulator.server", "127.0.0.1");
            final short simulatorPort = (short) pcu.getInt(LowLevelAdapter.class, "simulator.port", 1726);

            connectionStatus = this.edk.EE_EngineRemoteConnect(simulatorServer, simulatorPort);
        }

        // initialize variables if connected
        if (connectionStatus == EDK.EDK_OK) {
            this.state = EDK.EDK_OK;
            this.eState = this.edk.EE_EmoStateCreate();
            this.eEvent = this.edk.EE_EmoEngineEventCreate();
        }

        this.connected = connectionStatus == EDK.EDK_OK;
        if (this.connected) {
            System.out.println("brain tracker connected");
        }

    }

    /**
     * Retrieves the event from the brain tracker
     * 
     * @return The channels and their corresponding values
     */
    public HashMap<String, Double> getBrainEvent() {
        if (this.connected) {
            this.state = this.edk.EE_EngineGetNextEvent(this.eEvent);

            // check if an event was found
            if (this.state == EDK.EDK_OK) {
                final int eventType = this.edk.EE_EmoEngineEventGetType(this.eEvent);

                // Retrieve emotional state if it has been updated
                if (eventType == EE_Event_t.EE_EmoStateUpdated) {
                    this.edk.EE_EmoEngineEventGetEmoState(this.eEvent, this.eState);

                    final EE_ExpressivAlgo_t upperFaceAction = this.edk.ES_ExpressivGetUpperFaceAction(this.eState);
                    final float upperFacePower = this.edk.ES_ExpressivGetUpperFaceActionPower(this.eState);

                    final EE_ExpressivAlgo_t lowerFaceAction = this.edk.ES_ExpressivGetLowerFaceAction(this.eState);
                    final float lowerFacePower = this.edk.ES_ExpressivGetLowerFaceActionPower(this.eState);

                    final HashMap<String, Double> eventChannel = new HashMap<String, Double>();
                    eventChannel.put("channel:furrow", (EE_ExpressivAlgo_t.EXP_FURROW == upperFaceAction.getValue() ? upperFacePower : 0.0));
                    eventChannel.put("channel:smile", (EE_ExpressivAlgo_t.EXP_SMILE == lowerFaceAction.getValue() ? lowerFacePower : 0.0));
                    eventChannel.put("channel:laugh", (EE_ExpressivAlgo_t.EXP_LAUGH == lowerFaceAction.getValue() ? lowerFacePower : 0.0));
                    eventChannel.put("channel:instExcitement", (double) this.edk.ES_AffectivGetExcitementShortTermScore(this.eState));
                    eventChannel.put("channel:engagement", (double) this.edk.ES_AffectivGetEngagementBoredomScore(this.eState));
                    return eventChannel;
                }
            }
        }
        return null;
    }
}
