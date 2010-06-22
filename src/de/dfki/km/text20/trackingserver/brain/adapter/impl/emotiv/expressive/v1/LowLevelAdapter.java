package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1;

import java.util.HashMap;

import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.EDK;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_Event_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_ExpressivAlgo_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoEngineEventHandle;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoStateHandle;

public class LowLevelAdapter{
	
	/** Emotiv Wrapper */
	protected EDK edk;
	
	/** State of the brain tracker */
	protected int state;
	
	/** Handle to the emotional state */
	protected EmoStateHandle eState;
	
	/** Brain tracker event */
	protected EmoEngineEventHandle eEvent;
	
	/** Indicates whether the brain tracker is connected */
	protected boolean connected;
	
	
	/** Connects to the brain tracker */
	public void connectToEngine(){	
		
		edk = EDK.INSTANCE;		
		
		// connecting to the EmoComposer (Simulator)
		//int connectionStatus = edk.EE_EngineRemoteConnect("127.0.0.1", (short)1726);
		// connecting to the EmoEngine (Brain tracker)
		int connectionStatus = edk.EE_EngineConnect();
		
		// initialize variables if connected
		if(connectionStatus==EDK.EDK_OK){
			state 		= EDK.EDK_OK;
			eState		= edk.EE_EmoStateCreate();
			eEvent 		= edk.EE_EmoEngineEventCreate();
		}
		
		connected = connectionStatus==EDK.EDK_OK;
		if(connected){
			System.out.println("brain tracker connected");
		}
		
	}
	
	
	/**
	 * Retrieves the event from the brain tracker
	 * 
	 * @return The channels and their corresponding values
	 */
	public HashMap<String, Double> getBrainEvent() {
		if(connected){				
			state = edk.EE_EngineGetNextEvent(eEvent);
			
			// check if an event was found
			if ( state == EDK.EDK_OK ) {
				int eventType = edk.EE_EmoEngineEventGetType(eEvent);

				// Retrieve emotional state if it has been updated
				if (eventType == EE_Event_t.EE_EmoStateUpdated) {
					edk.EE_EmoEngineEventGetEmoState(eEvent, eState);
					
					EE_ExpressivAlgo_t upperFaceAction = edk.ES_ExpressivGetUpperFaceAction(eState);
					float			   upperFacePower  = edk.ES_ExpressivGetUpperFaceActionPower(eState);

					EE_ExpressivAlgo_t lowerFaceAction = edk.ES_ExpressivGetLowerFaceAction(eState);
					float			   lowerFacePower  = edk.ES_ExpressivGetLowerFaceActionPower(eState);				
					
					HashMap<String, Double> eventChannel = new HashMap<String, Double>();
					eventChannel.put("channel:furrow", (EE_ExpressivAlgo_t.EXP_FURROW==upperFaceAction.getValue()?upperFacePower:0.0));
					eventChannel.put("channel:smile", (EE_ExpressivAlgo_t.EXP_SMILE==lowerFaceAction.getValue()?lowerFacePower:0.0));
					eventChannel.put("channel:laugh", (EE_ExpressivAlgo_t.EXP_LAUGH==lowerFaceAction.getValue()?lowerFacePower:0.0));
					eventChannel.put("channel:instExcitement", (double) edk.ES_AffectivGetExcitementShortTermScore(eState));
					eventChannel.put("channel:engagement", (double) edk.ES_AffectivGetEngagementBoredomScore(eState));
					return eventChannel;
				}
			}			
		}
		return null;
	}
}
