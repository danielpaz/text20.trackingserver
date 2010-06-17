package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.brainplugin.impl;

import java.util.HashMap;
import java.util.LinkedList;

import net.xeoh.plugins.base.annotations.Timer;

import org.json.JSONArray;
import org.json.JSONException;

import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.brainplugin.BrainPlugin;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.EDK;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types.EE_Event_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types.EE_ExpressivAlgo_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types.EmoEngineEventHandle;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types.EmoStateHandle;


public class BrainPluginImpl implements BrainPlugin {
	
	enum Channel{
		FURROW(true, 0.2, true), 
		SMILE(true, 1, true), 
		LAUGH(true, 1, true), 
		INSTANTANEOUS_EXCITEMENT(false), 
		ENGAGEMENT(true, 0.75, true),
		BOREDOM(true, 0.4, false);
		
		private boolean peak;
		private double threshold;
		private boolean above;
		
		Channel(boolean peak){
			this.peak = peak;
		}
		
		Channel(boolean peak, double threshold, boolean above){
			this(peak);
			this.threshold = threshold;
			this.above = above;
		}
		
		boolean hasPeak(){
			return peak;
		}
	}
	
	enum Emotion{
		HAPPY(new Channel[]{Channel.LAUGH}),
		INTERESTED(new Channel[]{Channel.ENGAGEMENT}),
		DOUBT(new Channel[]{Channel.FURROW}),
		BORED(new Channel[]{Channel.BOREDOM});
		
		private Channel[] channels;
		
		Emotion(Channel...channels ){
			this.channels = channels;
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
	public boolean connectToEngine(){
		//NativeLibrary.addSearchPath("edk", System.getProperty("user.dir"+"/lib"));
		//NativeLibrary.addSearchPath("edk_utils", System.getProperty("user.dir"+"/lib"));
		
		edk = EDK.INSTANCE;
		state = EDK.EDK_OK;
		eState = edk.EE_EmoStateCreate();
		eEvent = edk.EE_EmoEngineEventCreate();
		
		//int connectionStatus = edk.EE_EngineRemoteConnect("127.0.0.1", (short)1726);
		int connectionStatus = edk.EE_EngineConnect();
		
		if(connectionStatus==EDK.EDK_OK){
			state 		= EDK.EDK_OK;
			eState		= edk.EE_EmoStateCreate();
			eEvent 		= edk.EE_EmoEngineEventCreate();
			bEvents= new LinkedList<HashMap<Channel, Double>>();
		}
		
		connected = connectionStatus==EDK.EDK_OK ? true : false;
		System.out.println("+++++++++########################&&&&&&&&&&&&&&&&&&connected? "+connected);
		return connected;
	}
	
	

	@Timer(period=50)
	public void logBrainEvents() {
		//System.out.println("in logbrainevents"); // DEBUGGING
		if(isConnected()){				
			state = edk.EE_EngineGetNextEvent(eEvent);
			
			if ( state == EDK.EDK_OK ) {				
				//System.out.println("state: OK"); // DEBUGGING
				int eventType = edk.EE_EmoEngineEventGetType(eEvent);

				// Log the EmoState if it has been updated
				if (eventType == EE_Event_t.EE_EmoStateUpdated) {

					edk.EE_EmoEngineEventGetEmoState(eEvent, eState);
					float timestamp = edk.ES_GetTimeFromStart(eState);
					
					EE_ExpressivAlgo_t upperFaceAction = edk.ES_ExpressivGetUpperFaceAction(eState);
					float			   upperFacePower  = edk.ES_ExpressivGetUpperFaceActionPower(eState);

					EE_ExpressivAlgo_t lowerFaceAction = edk.ES_ExpressivGetLowerFaceAction(eState);
					float			   lowerFacePower  = edk.ES_ExpressivGetLowerFaceActionPower(eState);

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
					event.put(Channel.FURROW, (EE_ExpressivAlgo_t.EXP_FURROW==upperFaceAction.getValue()?upperFacePower:0.0));
					event.put(Channel.SMILE, (EE_ExpressivAlgo_t.EXP_SMILE==lowerFaceAction.getValue()?lowerFacePower:0.0));
					event.put(Channel.LAUGH, (EE_ExpressivAlgo_t.EXP_LAUGH==lowerFaceAction.getValue()?lowerFacePower:0.0));
					event.put(Channel.INSTANTANEOUS_EXCITEMENT, (double) edk.ES_AffectivGetExcitementShortTermScore(eState));
					event.put(Channel.ENGAGEMENT, (double) edk.ES_AffectivGetEngagementBoredomScore(eState));
					event.put(Channel.BOREDOM, (double) edk.ES_AffectivGetEngagementBoredomScore(eState));
					
					//System.out.println("******************"+event.get(Channel.SMILE)); // debugging
					
					//return "time "+timestamp;
					
					
						
					/*JSONArray array = new JSONArray();
					try {
						array.put(timestamp);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("before returning array"); // DEBUGGING
					return array;*/
					
					bEvents.add(event);
					
					//System.out.println(logEmoState(user, eState, writeHeader));
					//writeHeader = false;
				}
			}
			
			
			
		}else{
			connectToEngine();
		}
		/*JSONArray array = new JSONArray();
		try {
			array.put(0.0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;*/
		//return "@";
	}
	
	@Override
	public JSONArray getEmotion(){
		if(!bEvents.isEmpty()){
			LinkedList<HashMap<Channel, Double>> allEvents = bEvents;
			JSONArray emotions= new JSONArray();
			HashMap<Channel, Double> avg = new HashMap<Channel, Double>();
			for(Channel c : Channel.values()){
				double value = 0; 
				if(c.hasPeak()){
					for(int j =0; j< allEvents.size(); j++){
						if(c.above){
							if(allEvents.get(j).get(c)>=c.threshold){
								value = 1;
								break;
							}
						}else{
							if(allEvents.get(j).get(c)<=c.threshold){
								value = 1;
								break;
							}
						}
						
					}
					System.out.println(c+"#######################"+value);
				}else{
					float sum=0;
					for(int j =0; j< allEvents.size(); j++){
						sum+= allEvents.get(j).get(c);
					}
					value = sum/allEvents.size();
					System.out.println(c+"#######################"+value);
				}
				avg.put(c, value);				
			}	
			
			for(Emotion e : Emotion.values()){
				boolean felt = true;
				for(Channel c : e.channels){
					if(c.hasPeak()){						
						if(avg.get(c)!=1){
							felt = false;
						}
					} else{
						if(c.above){
							if(avg.get(c)<c.threshold){
								felt = false;
							}	
						}else{
							if(avg.get(c)>c.threshold){
								System.out.println("BORED"); // DEBUGGING
								felt = false;
							}
						}
					}
				}
				
				try{
					if(felt){
						emotions.put(1.0);
					}else{
						emotions.put(0.0);
					}
				}catch(JSONException ex){
					ex.printStackTrace();
				}
			}			
			System.out.println("returned values");
			bEvents = new LinkedList<HashMap<Channel, Double>>();	
			try {
				System.out.println("&&&&&&&"+emotions.getDouble(0));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return emotions;	
		}
		return null;
	}


	@Override
	public JSONArray getBrainEvents() {
		if(!bEvents.isEmpty()){
			LinkedList<HashMap<Channel, Double>> allEvents = bEvents;
			JSONArray rEvents= new JSONArray();
			for(Channel e : Channel.values()){
				double value = 0; 
				if(e.hasPeak()){
					for(int j =0; j< allEvents.size(); j++){
						if(allEvents.get(j).get(e)>=e.threshold){
							value = 1;
							break;
						}
					}
					System.out.println(e+"#######################"+value);
				}else{
					float sum=0;
					for(int j =0; j< allEvents.size(); j++){
						sum+= allEvents.get(j).get(e);
					}
					value = sum/allEvents.size();
					System.out.println(e+"#######################"+value);
				}
				try {
					//System.out.println("#######################"+value);
					rEvents.put(round(value));
				} catch (JSONException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}			
			System.out.println("returned values");
			bEvents = new LinkedList<HashMap<Channel, Double>>();	
			return rEvents;	
		}
		return null;
	}
	

	@Override
	public boolean isConnected() {
		return connected;
	}
	
	private double round(double value){
		double d = Math.pow(10, DECIMALS);
		return (Math.round(value*d)/d);
	}
}
