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
package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1;

import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Timer;
import de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingEvent;

/**
 * @author rb
 *
 */
@PluginImplementation
public class BrainAdapterImpl implements BrainAdapter {
	
	LowLevelAdapter lowLevelAdapter; 
	
	BlockingQueue<TrackingEvent> eventQueue;

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#start()
     */
    @Override
    public void start() {
    	lowLevelAdapter= new LowLevelAdapter();
        lowLevelAdapter.connectToEngine();  
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

	/* (non-Javadoc)
	 * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#getDeviceInformation()
	 */
	@Override
	public TrackingDeviceInformation getDeviceInformation() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#setup(java.util.concurrent.BlockingQueue)
	 */
	@Override
	public void setup(BlockingQueue<TrackingEvent> eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	/**
	 * Retrieves the events from the brain tracker
	 */
	@Timer(period=50)
	public void pollChannels(){
		try{
			if(lowLevelAdapter!=null){
				TrackingEvent t = new TrackingEvent();
				t.channels = lowLevelAdapter.getBrainEvent();
				if(t.channels!=null){				
					try {
						eventQueue.put(t);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	
	/**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "brainadapter:emotiv" };
    }
}
