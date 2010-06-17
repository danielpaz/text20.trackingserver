package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types;

public class EE_Event_t {
	
	public static final int EE_UnknownEvent			= 0x0000;
	public static final int EE_EmulatorError		= 0x0001;
	public static final int EE_ReservedEvent		= 0x0002;
	public static final int EE_UserAdded			= 0x0010;
	public static final int EE_UserRemoved			= 0x0020;
	public static final int EE_EmoStateUpdated		= 0x0040;
	public static final int EE_ProfileEvent			= 0x0080;
	public static final int EE_CognitivEvent		= 0x0100;
	public static final int EE_ExpressivEvent		= 0x0200;
	public static final int EE_InternalStateChanged = 0x0400;
	public static final int EE_AllEvent				= EE_UserAdded | EE_UserRemoved | EE_EmoStateUpdated | EE_ProfileEvent |
						  				  		  	  EE_CognitivEvent | EE_ExpressivEvent | EE_InternalStateChanged;
}
