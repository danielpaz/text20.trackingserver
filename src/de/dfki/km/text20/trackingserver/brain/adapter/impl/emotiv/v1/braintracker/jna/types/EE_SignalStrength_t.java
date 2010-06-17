package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;

public class EE_SignalStrength_t implements NativeMapped {

	public final static int NO_SIGNAL 		= 0; 
	public final static int BAD_SIGNAL		= 1;
	public final static int GOOD_SIGNAL		= 2;
	
	protected int value;

	public EE_SignalStrength_t() {
		this(NO_SIGNAL);
	}
	
	public EE_SignalStrength_t(int val) {
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
				case 0:  return new EE_SignalStrength_t(NO_SIGNAL); 
				case 1:  return new EE_SignalStrength_t(BAD_SIGNAL);
				case 2:  return new EE_SignalStrength_t(GOOD_SIGNAL);
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
