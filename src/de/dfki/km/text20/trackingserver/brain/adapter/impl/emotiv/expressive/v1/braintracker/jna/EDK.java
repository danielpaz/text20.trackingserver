package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna;


import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_ExpressivAlgo_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EE_SignalStrength_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoEngineEventHandle;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types.EmoStateHandle;


public interface EDK extends Library {
	
    public static EDK INSTANCE = (EDK)Native.loadLibrary("edk", EDK.class);;
    
    public static final int EDK_OK = 0;
    
    public int EE_EngineConnect();
    
    public PointerByReference EE_DataCreate();
    
    public int EE_DataSetBufferSizeInSec(float bufferSizeInSec);
    
    public EmoEngineEventHandle EE_EmoEngineEventCreate();
    
    public EmoStateHandle EE_EmoStateCreate();
    
    public int EE_EmoEngineEventGetType(EmoEngineEventHandle hEvent);
    
    public int EE_EngineGetNextEvent(EmoEngineEventHandle hEvent);
    
    public int EE_EmoEngineEventGetUserId(EmoEngineEventHandle hEvent, LongByReference pUserIdOut);
    
    public int EE_DataAcquisitionEnable(long userId, boolean enable);
    
    public int EE_DataUpdateHandle(long userId, Pointer hData);
    
    public int EE_DataGetNumberOfSample(Pointer hData, IntByReference nSampleOut);
    
    public int EE_DataGet(Pointer hData, int channel, double[] buffer, int bufferSizeInSample);
    
    public int EE_EngineRemoteConnect(String szHost, short port);
    
    public int EE_EmoEngineEventGetEmoState(EmoEngineEventHandle hEvent, EmoStateHandle hEmoState);
    
    // emoStateDLL
    
    public float ES_GetTimeFromStart(EmoStateHandle state);
    
    public int ES_ExpressivIsBlink(EmoStateHandle state);
    
	public int ES_ExpressivIsLeftWink(EmoStateHandle state);

	public int ES_ExpressivIsRightWink(EmoStateHandle state);
	
	public int ES_ExpressivIsLookingLeft(EmoStateHandle state);

	public int ES_ExpressivIsLookingRight(EmoStateHandle state);
	
	public float ES_AffectivGetExcitementShortTermScore(EmoStateHandle state);
	
	public float ES_AffectivGetExcitementLongTermScore(EmoStateHandle state);
	
	public float ES_AffectivGetEngagementBoredomScore(EmoStateHandle state);
	
	public float ES_CognitivGetCurrentActionPower(EmoStateHandle state);
	
	public EE_SignalStrength_t ES_GetWirelessSignalStatus(EmoStateHandle state);
	
	public EE_ExpressivAlgo_t ES_ExpressivGetUpperFaceAction(EmoStateHandle state);

	public float ES_ExpressivGetUpperFaceActionPower(EmoStateHandle state);

	public EE_ExpressivAlgo_t ES_ExpressivGetLowerFaceAction(EmoStateHandle state);

	public float ES_ExpressivGetLowerFaceActionPower(EmoStateHandle state);
}
