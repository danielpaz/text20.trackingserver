/*
 * BrainAdapterImpl.java
 * 
 * Copyright (c) 2010, Ralf Biedert, DFKI. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA 02110-1301  USA
 *
 */
package de.dfki.km.text20.trackingserver.brain.adapter.impl.dummy;

import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Timer;
import net.xeoh.plugins.base.annotations.events.Init;
import de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingEvent;

/**
 * @author rb
 *
 */
@PluginImplementation
public class BrainAdapterImpl implements BrainAdapter {

    /** */
     BlockingQueue<TrackingEvent> eventQueue;

    /**  */
    @Init
    public void init() {
        //
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#start()
     */
    @Override
    public void start() {
    }
    
    /**
	 * Retrieves the events from the brain tracker
	 */
	@Timer(period=50)
	public void pollChannels(){
		TrackingEvent t = new TrackingEvent();
		t.channels.put("channel:furrow", 0.3);
		t.channels.put("channel:smile", 0.1);
		t.channels.put("channel:laugh", 0.0);
		t.channels.put("channel:instExcitement", 0.5);
		t.channels.put("channel:engagement", 0.6);
		try {
		    if(eventQueue == null) return;
			eventQueue.put(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public TrackingDeviceInformation getDeviceInformation() {
        // TODO Auto-generated method stub
        return new TrackingDeviceInformation();
    }

    @Override
    public void setup(BlockingQueue<TrackingEvent> eventQueue) {
        this.eventQueue = eventQueue;
    }
    
    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "brainadapter:dummy" };
    }

}
