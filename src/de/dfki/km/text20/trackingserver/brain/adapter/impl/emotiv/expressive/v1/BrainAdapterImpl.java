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
package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1;

import static net.jcores.jre.CoreKeeper.$;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Timer;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingEvent;

/**
 * @author Farida Ismail
 */
@PluginImplementation
public class BrainAdapterImpl implements BrainAdapter {

    LowLevelAdapter lowLevelAdapter;

    BlockingQueue<TrackingEvent> eventQueue;

    /** */
    @InjectPlugin
    public PluginManager pluginManager;

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#start()
     */
    @Override
    public void start() {
        this.lowLevelAdapter = new LowLevelAdapter(this.pluginManager);
        this.lowLevelAdapter.connectToEngine();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#getDeviceInformation()
     */
    @Override
    public TrackingDeviceInformation getDeviceInformation() {
        final TrackingDeviceInformation information = new TrackingDeviceInformation();
        information.channelNames = $("channel:emotion:furrow", "channel:emotion:smile", "channel:emotion:laugh", "channel:emotion:excitement", "channel:emotion:engagement").unsafearray();
        information.deviceName = "EPOC Headset";
        information.hardwareID = "n/a";
        information.trackingDeviceManufacturer = "Emotiv";
        information.hardwareType = "device:emotiv:expressive";
        return information;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter#setup(java.util.concurrent.BlockingQueue)
     */
    @Override
    public void setup(BlockingQueue<TrackingEvent> eventQueue) {
        this.eventQueue = eventQueue;
    }

    /**
     * Retrieves the events from the brain tracker
     */
    @SuppressWarnings("boxing")
    @Timer(period = 50)
    public void pollChannels() {
        try {
            if (this.lowLevelAdapter == null) return;

            final HashMap<String, Double> readings = this.lowLevelAdapter.getBrainEvent();
            final TrackingEvent t = new TrackingEvent();
            t.deviceReadings = new double[5];
            
            if(readings != null) {
                t.deviceReadings[0] = readings.get("channel:furrow");
                t.deviceReadings[1] = readings.get("channel:smile");
                t.deviceReadings[2] = readings.get("channel:laugh");
                t.deviceReadings[3] = readings.get("channel:instExcitement");
                t.deviceReadings[4] = readings.get("channel:engagement");
            }

            try {
                this.eventQueue.put(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "brainadapter:emotiv:expressive" };
    }
}
