package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.braintracker.jna.types;

import com.sun.jna.ptr.ByReference;

public class DoubleArrayByReference extends ByReference {

	public DoubleArrayByReference(int elements) {
		super(elements * 4);
	}

}
