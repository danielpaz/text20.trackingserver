/*
 * TrackingServer.java
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

package de.dfki.km.text20.trackingserver.brain.remote.impl;

import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingClientCallback;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingEvent;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingServerRegistry;
import de.dfki.km.text20.trackingserver.common.remote.CommonClientCallback;
import de.dfki.km.text20.trackingserver.common.remote.CommonTrackingEvent;
import de.dfki.km.text20.trackingserver.common.remote.impl.CommonServerRegistry;

/**
 * @author Ralf Biedert
 */
@PluginImplementation
public class TrackingServerRegistryImpl
        extends
        CommonServerRegistry<TrackingEvent, TrackingClientCallback, TrackingDeviceInformation, BrainAdapter> implements TrackingServerRegistry{
    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "trackingregistry:brain" };
    }

    /* (non-Javadoc)
     * @see de.dfki.km.text20.trackingserver.common.remote.impl.CommonServerRegistry#feedCallback(java.util.concurrent.BlockingQueue, de.dfki.km.text20.trackingserver.common.remote.CommonClientCallback)
     */
    @SuppressWarnings("cast")
    @Override
    protected void feedCallback(BlockingQueue<TrackingEvent> queue,
                                CommonClientCallback<CommonTrackingEvent> callback) throws Exception {
        // Okay, there are things that are more beautiful than this one (caused by LipeRMI array casting
        // problems)
        try {
            final TrackingEvent event = queue.take();
            callback.newTrackingEvents((CommonTrackingEvent[]) new TrackingEvent[] { event });        
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
