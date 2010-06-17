package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;

public class EE_DataChannel_t implements NativeMapped {

	public final static int ED_COUNTER 			= 0; 
	public final static int ED_INTERPOLATED		= 1;
	public final static int ED_RAW_CQ			= 2;
	public final static int ED_AF3				= 3;
	public final static int ED_F7				= 4;
	public final static int ED_F3				= 5;
	public final static int ED_FC5				= 6;
	public final static int ED_T7				= 7;
	public final static int ED_P7				= 8;
	public final static int ED_O1				= 9;
	public final static int ED_O2				= 10;
	public final static int ED_P8				= 11;
	public final static int ED_T8				= 12;
	public final static int ED_FC6				= 13;
	public final static int ED_F4				= 14;
	public final static int ED_F8				= 15;
	public final static int ED_AF4				= 16;
	public final static int ED_GYROX			= 17;
	public final static int ED_GYROY			= 18;
	public final static int ED_TIMESTAMP		= 19;
	public final static int ED_ES_TIMESTAMP		= 20;
	public final static int ED_FUNC_ID			= 21;
	public final static int ED_FUNC_VALUE		= 22;
	public final static int ED_MARKER			= 23;
	public final static int ED_SYNC_SIGNAL		= 24;
	
	protected int value;

	public EE_DataChannel_t(int val) {
		value = val;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public Object fromNative(Object nativ, FromNativeContext context) {
		if ( nativ instanceof Integer ) {
			switch ( (Integer)nativ ) {
				case 0:  return ED_COUNTER; 
				case 1:  return ED_INTERPOLATED;
				case 2:  return ED_RAW_CQ;
				case 3:  return ED_AF3;
				case 4:  return ED_F7;
				case 5:  return ED_F3; 
				case 6:  return ED_FC5; 
				case 7:  return ED_T7;
				case 8:  return ED_P7; 
				case 9:  return ED_O1; 
				case 10:  return ED_O2; 
				case 11:  return ED_P8; 
				case 12:  return ED_T8; 
				case 13:  return ED_FC6; 
				case 14:  return ED_F4;
				case 15:  return ED_F8; 
				case 16:  return ED_AF4; 
				case 17:  return ED_GYROX; 
				case 18:  return ED_GYROY; 
				case 19:  return ED_TIMESTAMP; 
				case 20:  return ED_ES_TIMESTAMP; 
				case 21:  return ED_FUNC_ID;
				case 22:  return ED_FUNC_VALUE; 
				case 23:  return ED_MARKER;
				case 24:  return ED_SYNC_SIGNAL;
			}
		}
		return null;
	}

	@Override
	public Class nativeType() {
		return Integer.class;
	}

	@Override
	public Object toNative() {
		return value;
	}
}
