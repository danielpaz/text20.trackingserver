/*
 * CommonAdapter.java
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
package de.dfki.km.text20.trackingserver.common.adapter;

import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.Plugin;
import de.dfki.km.text20.trackingserver.common.remote.CommonDeviceInformation;
import de.dfki.km.text20.trackingserver.common.remote.CommonTrackingEvent;

/**
 * @author Ralf Biedert
 * @param <T> 
 * @param <I>
 */
public interface CommonAdapter<T extends CommonTrackingEvent, I extends CommonDeviceInformation> extends Plugin {
    /**
     * Called when the adapter should prepare itself for startup, the queue has to be filled with 
     * tracking events after start().
     * 
     * @param eventQueue 
     */
    public void setup(BlockingQueue<T> eventQueue);

    /**
     * The adapter should start pumping events obtained from the eye tracker into the queue. 
     */
    public void start();

    /**
     * The adapter should stop pumping events into the queue.
     */
    public void stop();
    
    /**
     * Returns information about this device.
     * 
     * @return .
     */
    public I getDeviceInformation();
}
