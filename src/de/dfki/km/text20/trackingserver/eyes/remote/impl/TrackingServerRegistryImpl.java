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
package de.dfki.km.text20.trackingserver.eyes.remote.impl;

import java.awt.Point;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.util.OptionUtils;
import de.dfki.km.text20.trackingserver.common.remote.impl.CommonServerRegistry;
import de.dfki.km.text20.trackingserver.eyes.adapter.AdapterCommand;
import de.dfki.km.text20.trackingserver.eyes.adapter.GazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand.OptionCalibratorNumPoints;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingClientCallback;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingCommand;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingServerRegistry;
import de.dfki.km.text20.trackingserver.eyes.remote.options.SendCommandOption;
import de.dfki.km.text20.trackingserver.eyes.remote.options.sendcommand.OptionRecalibrationPattern;

/**
 * 
 * @author rb
 */
@PluginImplementation
public class TrackingServerRegistryImpl extends CommonServerRegistry<TrackingEvent, TrackingClientCallback, TrackingDeviceInformation, GazeAdapter>
        implements TrackingServerRegistry {

    /** */
    ReferenceBasedDisplacementFilter displacementFilter = new ReferenceBasedDisplacementFilter();

    /** */
    @Override
    @Thread(isDaemonic = false)
    public void senderThread() {
        
        int numMisdetected = 0;

        while (true) {
            try {
                // Try to get the lastest event
                final TrackingEvent latestEvent = this.events.poll(500, TimeUnit.MILLISECONDS);

                // Don't warn if we haven't received any event yet.
                if (latestEvent == null && this.numEventsReceived > 0) {
                    this.logger.warning("No tracking events anymore after event " + this.numEventsReceived + ".");
                    numMisdetected++;

                    // Check for emergency restart.
                    if (numMisdetected > 0 && numMisdetected % 6 == 0) {
                        this.logger.warning("Performing emergency restart of tracking device.");
                        this.logger.warning("Calling stop()");
                        this.usedAdpater.stop();
                        this.logger.warning("Calling start()");
                        this.usedAdpater.start();
                        this.logger.warning("Device restarted. Cross your fingers.");
                    }

                    continue;
                }
                // Increase number of successful events
                this.numEventsReceived++;
                
                // Output something if we are running fine ...
                if(this.numEventsReceived == 100) {
                    this.logger.info("Received a number of events. Should be running fine.");
                }

                // ... and output something regularly to check we are still running fine.
                if(this.numEventsReceived % 1000 == 0) {
                    this.logger.fine("Still running. Tracking event : " + latestEvent);
                }
                
                // Filter events
                final TrackingEvent filteredEvent = filterEvent(latestEvent);

                // Send evens to listener
                this.callbacksLock.lock();
                try {
                    for (BlockingQueue<TrackingEvent> queue : this.callbacks) {
                        queue.offer(filteredEvent);
                    }
                } finally {
                    this.callbacksLock.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("Error waiting for some result ...");
            }
        }
    }

    /**
     * @param latestEvent
     * @return
     */
    private TrackingEvent filterEvent(TrackingEvent latestEvent) {
        // Sanity check
        if (latestEvent == null) return null;

        // zomfg, this needs improvement, we also have to adjust all the other
        // values.
        if (this.displacementFilter != null && latestEvent.centerGaze != null) {
            latestEvent.centerGaze = this.displacementFilter.filterEvent(latestEvent.centerGaze);
            latestEvent._centerX = latestEvent.centerGaze.x;
            latestEvent._centerY = latestEvent.centerGaze.y;
        }

        // TODO: Make a copy, don't return the same object.
        return latestEvent;
    }

    /**
     * @param command
     * @param options
     */
    @Override
    @SuppressWarnings("boxing")
    public void sendCommand(TrackingCommand command, SendCommandOption... options) {
        this.logger.fine("Received command " + command);
        
        if (this.usedAdpater == null) return;

        // Process our options
        final OptionUtils<SendCommandOption> ou = new OptionUtils<SendCommandOption>(options);

        switch (command) {
            case START:
                this.usedAdpater.start();
                break;
            case STOP:
                this.usedAdpater.stop();
                break;
            case ONLINE_RECALIBRATION:
                this.logger.info("Performing an internal recalibration");
                if (!ou.contains(OptionRecalibrationPattern.class)) break;
    
                final OptionRecalibrationPattern rcp = ou.get(OptionRecalibrationPattern.class);
                final List<Object[]> points = rcp.getPoints();
    
                this.displacementFilter.clearReferencePoints();
    
                for (Object[] objects : points) {
                    if (objects.length != 4) continue;
    
                    final Point point = (Point) objects[0];
                    final Integer dx = (Integer) objects[1];
                    final Integer dy = (Integer) objects[2];
                    final Long time = (Long) objects[3];
    
                    this.logger.fine(point + " -> " + dx + ", " + dy);
                    this.displacementFilter.updateReferencePoint(point, dx, dy, time);
                }
                break;
            case DROP_RECALIBRATION:
                this.logger.info("Dropping old recalibration info");
                this.displacementFilter.clearReferencePoints();
                break;
            case HARDWARE_CALIBRATION:
                this.logger.info("Performing a harware calibration");
                this.usedAdpater.adapterCommand(AdapterCommand.CALIBRATE, new OptionCalibratorNumPoints(5));
                break;
        }
    }

    /**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "trackingregistry:eyes" };
    }

}
