package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1.braintracker.jna.types;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;

public class EE_ExpressivAlgo_t implements NativeMapped {

	public final static int EXP_NEUTRAL 		= 0x0001; 
	public final static int EXP_BLINK			= 0x0002;
	public final static int EXP_WINK_LEFT		= 0x0004;
	public final static int EXP_WINK_RIGHT		= 0x0008;
	public final static int EXP_HORIEYE			= 0x0010;
	public final static int EXP_EYEBROW			= 0x0020;
	public final static int EXP_FURROW			= 0x0040;
	public final static int EXP_SMILE			= 0x0080;
	public final static int EXP_CLENCH			= 0x0100;
	public final static int EXP_LAUGH			= 0x0200;
	public final static int EXP_SMIRK_LEFT		= 0x0400;
	public final static int EXP_SMIRK_RIGHT		= 0x0800;
	
	protected int value;

	public EE_ExpressivAlgo_t() {
		this(EXP_NEUTRAL);
	}
	
	public EE_ExpressivAlgo_t(int val) {
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
				case 0x0001:  return new EE_ExpressivAlgo_t(EXP_NEUTRAL); 
				case 0x0002:  return new EE_ExpressivAlgo_t( EXP_BLINK);
				case 0x0004:  return new EE_ExpressivAlgo_t( EXP_WINK_LEFT);
				case 0x0008:  return new EE_ExpressivAlgo_t( EXP_WINK_RIGHT);
				case 0x0010:  return new EE_ExpressivAlgo_t( EXP_HORIEYE);
				case 0x0020:  return new EE_ExpressivAlgo_t( EXP_EYEBROW); 
				case 0x0040:  return new EE_ExpressivAlgo_t( EXP_FURROW); 
				case 0x0080:  return new EE_ExpressivAlgo_t( EXP_SMILE);
				case 0x0100:  return new EE_ExpressivAlgo_t( EXP_CLENCH); 
				case 0x0200:  return new EE_ExpressivAlgo_t( EXP_LAUGH); 
				case 0x0400:  return new EE_ExpressivAlgo_t( EXP_SMIRK_LEFT); 
				case 0x0800:  return new EE_ExpressivAlgo_t( EXP_SMIRK_RIGHT); 
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
