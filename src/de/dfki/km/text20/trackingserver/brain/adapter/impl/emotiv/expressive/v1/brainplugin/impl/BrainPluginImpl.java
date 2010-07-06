package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.brainplugin.impl;

import java.util.HashMap;
import java.util.LinkedList;

import net.xeoh.plugins.base.annotations.Timer;

import org.json.JSONArray;
import org.json.JSONException;

import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.brainplugin.BrainPlugin;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.EDK;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_Event_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_ExpressivAlgo_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoEngineEventHandle;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoStateHandle;

public class BrainPluginImpl implements BrainPlugin {

    enum Channel {
        FURROW(true, 0.2, true), 
        SMILE(true, 1, true), 
        LAUGH(true, 1, true), 
        INSTANTANEOUS_EXCITEMENT(false), 
        ENGAGEMENT(true, 0.75, true),
        BOREDOM(true, 0.4, false);

        private boolean peak;
        private double threshold;
        private boolean above;

        Channel(boolean peak) {
            this.peak = peak;
        }

        Channel(boolean peak, double threshold, boolean above) {
            this(peak);
            this.setThreshold(threshold);
            this.setAbove(above);
        }

        boolean hasPeak() {
            return this.peak;
        }

        public void setAbove(boolean above) {
            this.above = above;
        }

        public boolean isAbove() {
            return this.above;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public double getThreshold() {
            return this.threshold;
        }
    }

    enum Emotion {
        HAPPY(new Channel[] {Channel.LAUGH}),
        INTERESTED(new Channel[] {Channel.ENGAGEMENT}),
        DOUBT(new Channel[] {Channel.FURROW}),
        BORED(new Channel[] {Channel.BOREDOM});
        
        private Channel[] channels;

        Emotion(Channel... channels) {
            this.setChannels(channels);
        }

        public void setChannels(Channel[] channels) {
            this.channels = channels;
        }

        public Channel[] getChannels() {
            return this.channels;
        }
    }

    protected EDK edk;
    protected int state;
    protected EmoStateHandle eState;
    protected EmoEngineEventHandle eEvent;

    protected LinkedList<HashMap<Channel, Double>> bEvents;
    protected boolean retrieved;
    protected boolean connected;

    private final int DECIMALS = 2;

    //private final int CHANNELS = 6;

    @Override
    public boolean connectToEngine() {
        //NativeLibrary.addSearchPath("edk", System.getProperty("user.dir"+"/lib"));
        //NativeLibrary.addSearchPath("edk_utils", System.getProperty("user.dir"+"/lib"));

        this.edk = EDK.INSTANCE;
        this.state = EDK.EDK_OK;
        this.eState = this.edk.EE_EmoStateCreate();
        this.eEvent = this.edk.EE_EmoEngineEventCreate();

        //int connectionStatus = edk.EE_EngineRemoteConnect("127.0.0.1", (short)1726);
        int connectionStatus = this.edk.EE_EngineConnect();

        if (connectionStatus == EDK.EDK_OK) {
            this.state = EDK.EDK_OK;
            this.eState = this.edk.EE_EmoStateCreate();
            this.eEvent = this.edk.EE_EmoEngineEventCreate();
            this.bEvents = new LinkedList<HashMap<Channel, Double>>();
        }

        this.connected = (connectionStatus == EDK.EDK_OK) ? true : false;
        System.out.println("+++++++++########################&&&&&&&&&&&&&&&&&&connected? " + this.connected);
        return this.connected;
    }

    @Timer(period = 50)
    public void logBrainEvents() {
        //System.out.println("in logbrainevents"); // DEBUGGING
        if (isConnected()) {
            this.state = this.edk.EE_EngineGetNextEvent(this.eEvent);

            if (this.state == EDK.EDK_OK) {
                //System.out.println("state: OK"); // DEBUGGING
                int eventType = this.edk.EE_EmoEngineEventGetType(this.eEvent);

                // Log the EmoState if it has been updated
                if (eventType == EE_Event_t.EE_EmoStateUpdated) {

                    this.edk.EE_EmoEngineEventGetEmoState(this.eEvent, this.eState);
                    // float timestamp = this.edk.ES_GetTimeFromStart(this.eState);

                    EE_ExpressivAlgo_t upperFaceAction = this.edk.ES_ExpressivGetUpperFaceAction(this.eState);
                    float upperFacePower = this.edk.ES_ExpressivGetUpperFaceActionPower(this.eState);

                    EE_ExpressivAlgo_t lowerFaceAction = this.edk.ES_ExpressivGetLowerFaceAction(this.eState);
                    float lowerFacePower = this.edk.ES_ExpressivGetLowerFaceActionPower(this.eState);

                    /*int[] expressions = {
                    		//EE_ExpressivAlgo_t.EXP_EYEBROW,
                    		EE_ExpressivAlgo_t.EXP_FURROW,
                    		EE_ExpressivAlgo_t.EXP_SMILE,
                    		//EE_ExpressivAlgo_t.EXP_CLENCH,
                    		//EE_ExpressivAlgo_t.EXP_SMIRK_LEFT,
                    		//EE_ExpressivAlgo_t.EXP_SMIRK_RIGHT,
                    		EE_ExpressivAlgo_t.EXP_LAUGH,	
                    };
                    
                    double [] event = new double[CHANNELS];
                    int index = 0;
                    
                    event[index++] = timestamp;
                    
                    for ( int expression : expressions ) {
                    	if ( expression == upperFaceAction.getValue() ) {
                    		event[index++] = upperFacePower;
                    	} else if ( expression == lowerFaceAction.getValue() ) {
                    		event[index++] = lowerFacePower;
                    	} else {
                    		event[index++] = 0.0;;
                    	}
                    }

                    // Affectiv Suite results
                    event[index++] = edk.ES_AffectivGetExcitementShortTermScore(eState);
                    //result.append(edk.ES_AffectivGetExcitementLongTermScore(eState) + ",");
                    event[index++] = edk.ES_AffectivGetEngagementBoredomScore(eState);*/

                    HashMap<Channel, Double> event = new HashMap<Channel, Double>();
                    event.put(Channel.FURROW, new Double((EE_ExpressivAlgo_t.EXP_FURROW == upperFaceAction.getValue() ? upperFacePower : 0.0)));
                    event.put(Channel.SMILE, new Double((EE_ExpressivAlgo_t.EXP_SMILE == lowerFaceAction.getValue() ? lowerFacePower : 0.0)));
                    event.put(Channel.LAUGH, new Double((EE_ExpressivAlgo_t.EXP_LAUGH == lowerFaceAction.getValue() ? lowerFacePower : 0.0)));
                    event.put(Channel.INSTANTANEOUS_EXCITEMENT, new Double(this.edk.ES_AffectivGetExcitementShortTermScore(this.eState)));
                    event.put(Channel.ENGAGEMENT, new Double(this.edk.ES_AffectivGetEngagementBoredomScore(this.eState)));
                    event.put(Channel.BOREDOM, new Double(this.edk.ES_AffectivGetEngagementBoredomScore(this.eState)));

                    //System.out.println("******************"+event.get(Channel.SMILE)); // debugging

                    //return "time "+timestamp;

                    /*JSONArray array = new JSONArray();
                    try {
                    	array.put(timestamp);
                    } catch (JSONException e) {
                    	e.printStackTrace();
                    }
                    System.out.println("before returning array"); // DEBUGGING
                    return array;*/

                    this.bEvents.add(event);

                    //System.out.println(logEmoState(user, eState, writeHeader));
                    //writeHeader = false;
                }
            }

        } else {
            connectToEngine();
        }
        /*JSONArray array = new JSONArray();
        try {
        	array.put(0.0);
        } catch (JSONException e) {
        	e.printStackTrace();
        }
        return array;*/
        //return "@";
    }

    @Override
    public JSONArray getEmotion() {
        if (!this.bEvents.isEmpty()) {
            LinkedList<HashMap<Channel, Double>> allEvents = this.bEvents;
            JSONArray emotions = new JSONArray();
            HashMap<Channel, Double> avg = new HashMap<Channel, Double>();

            for (Channel c : Channel.values()) {
                double value = 0;
                if (c.hasPeak()) {
                    for (int j = 0; j < allEvents.size(); j++) {
                        if ( (c.isAbove() && (allEvents.get(j).get(c).doubleValue() >= c.getThreshold())) ||
                            (!c.isAbove() && (allEvents.get(j).get(c).doubleValue() <= c.getThreshold()))) {
                            value = 1;
                            break;
                        }
                        
                        /*
                        if (c.isAbove()) {
                            if (allEvents.get(j).get(c).doubleValue() >= c.getThreshold()) {
                                value = 1;
                                break;
                            }   
                        } else {
                            if (allEvents.get(j).get(c).doubleValue() <= c.getThreshold()) {
                                value = 1;
                                break;
                            }
                        }
                        */
                    }
                    
                    System.out.println(c + "#######################" + value);
                } else {
                    double sum = 0;
                    for (int j = 0; j < allEvents.size(); j++) {
                        sum += allEvents.get(j).get(c).doubleValue();
                    }
                    value = sum / allEvents.size();
                    System.out.println(c + "#######################" + value);
                }
                avg.put(c, new Double(value));
            }

            for (Emotion e : Emotion.values()) {
                boolean felt = true;
                for (Channel c : e.getChannels()) {
                    if (c.hasPeak()) {
                        if (avg.get(c).doubleValue() != 1) {
                            felt = false;
                        }
                    } else {
                        if (c.isAbove()) {
                            if (avg.get(c).doubleValue() < c.getThreshold()) {
                                felt = false;
                            }
                        } else {
                            if (avg.get(c).doubleValue() > c.getThreshold()) {
                                System.out.println("BORED"); // DEBUGGING
                                felt = false;
                            }
                        }
                    }
                }

                try {
                    emotions.put(felt ? 1.0 : 0.0);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }

            System.out.println("returned values");
            this.bEvents = new LinkedList<HashMap<Channel, Double>>();

            try {
                System.out.println("&&&&&&&" + emotions.getDouble(0));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            return emotions;
        }
        return null;
    }

    @Override
    public JSONArray getBrainEvents() {
        if (!this.bEvents.isEmpty()) {
            LinkedList<HashMap<Channel, Double>> allEvents = this.bEvents;
            JSONArray rEvents = new JSONArray();


            for (Channel e : Channel.values()) {
                double value = 0;
                if (e.hasPeak()) {
                    for (int j = 0; j < allEvents.size(); j++) {
                        if (allEvents.get(j).get(e).doubleValue() >= e.getThreshold()) {
                            value = 1;
                            break;
                        }
                    }
                    
                    System.out.println(e + "#######################" + value);
                } else {
                    double sum = 0;
                    for (int j = 0; j < allEvents.size(); j++) {
                        sum += allEvents.get(j).get(e).doubleValue();
                    }
                    
                    value = sum / allEvents.size();
                    System.out.println(e + "#######################" + value);
                }

                try {
                    //System.out.println("#######################"+value);
                    rEvents.put(round(value));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }

            System.out.println("returned values");
            this.bEvents = new LinkedList<HashMap<Channel, Double>>();

            return rEvents;
        }
        return null;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    private double round(double value) {
        double d = Math.pow(10, this.DECIMALS);
        return (Math.round(value * d) / d);
    }
}
