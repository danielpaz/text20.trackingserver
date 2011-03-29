/*
 * SimulatingBrainAdapter.java
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
package de.dfki.km.text20.trackingserver.brain.adapter.impl.simulator;

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
public class SimulatingBrainAdapter implements BrainAdapter {

    /** */
    private BlockingQueue<TrackingEvent> eventQueue;

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
        /** To be implemented, yet! */
    }

    /** Retrieves the events from the brain tracker */
    @Timer(period = 8)
    public void pollChannels() {
        TrackingEvent trackingEvent = new TrackingEvent();
        
        trackingEvent.channels.put("channel:furrow", new Double(Math.random() * 2 - 1));
        trackingEvent.channels.put("channel:smile", new Double(Math.random() * 2 - 1));
        trackingEvent.channels.put("channel:laugh", new Double(Math.random() * 2 - 1));
        trackingEvent.channels.put("channel:instExcitement", new Double(Math.random() * 2 - 1));
        trackingEvent.channels.put("channel:engagement", new Double(Math.random() * 2 - 1));
        
        try {
            if (this.eventQueue == null) return;
            
            this.eventQueue.put(trackingEvent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        /** To be implemented, yet! */
    }

    @Override
    public TrackingDeviceInformation getDeviceInformation() {
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
        return new String[] { "brainadapter:simulator" };
    }

}
