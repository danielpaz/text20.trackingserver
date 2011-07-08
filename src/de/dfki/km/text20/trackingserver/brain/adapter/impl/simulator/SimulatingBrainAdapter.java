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

import static net.jcores.jre.CoreKeeper.$;

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
    public void init() { }

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
        final TrackingEvent t = new TrackingEvent();
        t.deviceReadings = new double[5];
        t.deviceReadings[0] = Math.random() * 2 - 1;
        t.deviceReadings[1] = Math.random() * 2 - 1;
        t.deviceReadings[2] = Math.random() * 2 - 1;
        t.deviceReadings[3] = Math.random() * 2 - 1;
        t.deviceReadings[4] = Math.random() * 2 - 1;

        try {
            if (this.eventQueue == null) return;
            
            this.eventQueue.put(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.common.adapter.CommonAdapter#stop()
     */
    @Override
    public void stop() {
        /** To be implemented, yet! */
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.common.adapter.CommonAdapter#getDeviceInformation()
     */
    @Override
    public TrackingDeviceInformation getDeviceInformation() {
        final TrackingDeviceInformation information = new TrackingDeviceInformation();
        information.channelNames = $("channel:emotion:furrow", "channel:emotion:smile", "channel:emotion:laugh", "channel:emotion:excitement", "channel:emotion:engagement").unsafearray();
        information.deviceName = "Simulating Adapter";
        information.hardwareID = "n/a";
        information.trackingDeviceManufacturer = "Text 2.0 Project";
        information.hardwareType = "device:simulator";
        return information;
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.common.adapter.CommonAdapter#setup(java.util.concurrent.BlockingQueue)
     */
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
