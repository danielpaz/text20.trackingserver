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
import net.xeoh.plugins.diagnosis.local.options.status.OptionInfo;
import de.dfki.km.text20.trackingserver.common.remote.diagnosis.channels.tracing.CommonRegistryTracer;
import de.dfki.km.text20.trackingserver.common.remote.impl.CommonServerRegistry;
import de.dfki.km.text20.trackingserver.eyes.adapter.AdapterCommand;
import de.dfki.km.text20.trackingserver.eyes.adapter.GazeAdapter;
import de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand.OptionCalibratorNumPoints;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingClientCallback;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingCommand;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingServerRegistry;
import de.dfki.km.text20.trackingserver.eyes.remote.diagnosis.channel.status.DispatchingStatus;
import de.dfki.km.text20.trackingserver.eyes.remote.diagnosis.channel.status.ReceivingEvents;
import de.dfki.km.text20.trackingserver.eyes.remote.options.SendCommandOption;
import de.dfki.km.text20.trackingserver.eyes.remote.options.sendcommand.OptionRecalibrationPattern;

/**
 * 
 * @author rb
 */
@PluginImplementation
public class TrackingServerRegistryImpl
        extends
        CommonServerRegistry<TrackingEvent, TrackingClientCallback, TrackingDeviceInformation, GazeAdapter>
        implements TrackingServerRegistry {

    /** */
    ReferenceBasedDisplacementFilter displacementFilter = new ReferenceBasedDisplacementFilter();

    /** */
    @Override
    @Thread(isDaemonic = false)
    public void senderThread() {
        this.diagnosis.channel(CommonRegistryTracer.class).status("sender/start");
        this.diagnosis.channel(DispatchingStatus.class).status(Boolean.TRUE);

        int numMisdetected = 0;
        int numConsecutiveMisdetected = 0;

        while (true) {
            try {
                // Try to get the lastest event
                TrackingEvent latestEvent = this.events.poll(500, TimeUnit.MILLISECONDS);
                
                
                // Don't warn if we haven't received any event yet.
                if (latestEvent == null && this.numEventsReceived > 0) {
                    this.diagnosis.channel(CommonRegistryTracer.class).status("sender/polltimeout/stalled", new OptionInfo("alreadyreceived", "" + this.numEventsReceived), new OptionInfo("misdetected", "" + numMisdetected));

                    numMisdetected++;
                    numConsecutiveMisdetected++;

                    // It appears that sometimes (once every few minutes) we don't receive any
                    // event from the Tobii adapter. In that case, don't warn instantly, but rather 
                    // give the interface a few tries
                    if(numConsecutiveMisdetected >= 3) {
                        this.diagnosis.channel(ReceivingEvents.class).status(Boolean.FALSE);
                    }
                    
                    // Check for emergency restart.
                    if (numConsecutiveMisdetected >= 6) {
                        this.diagnosis.channel(CommonRegistryTracer.class).status("sender/polltimeout/emergencystop", new OptionInfo("alreadyreceived", "" + this.numEventsReceived), new OptionInfo("misdetected", "" + numMisdetected));                        
                        this.usedAdpater.stop();
                        this.diagnosis.channel(CommonRegistryTracer.class).status("sender/polltimeout/emergencystart", new OptionInfo("alreadyreceived", "" + this.numEventsReceived), new OptionInfo("misdetected", "" + numMisdetected));                        
                        this.usedAdpater.start();
                    }
                    continue;
                }
                
                // In case the first event was null
                if(latestEvent == null) {
                    this.diagnosis.channel(ReceivingEvents.class).status(Boolean.FALSE);
                    this.diagnosis.channel(CommonRegistryTracer.class).status("sender/polltimeout/nodata");
                    continue;
                }
                
                // Increase number of successful events
                this.diagnosis.channel(ReceivingEvents.class).status(Boolean.TRUE);                
                this.numEventsReceived++;
                numConsecutiveMisdetected = 0;

                
                // Filter events
                final TrackingEvent filteredEvent = filterEvent(latestEvent);
                
                
                // Output something if we are running fine ...
                if (this.numEventsReceived % 300 == 0 && this.numEventsReceived > 0) {
                    String gaze = "";
                    String filtered = "";
                    
                    if(latestEvent.centerGaze != null) {
                        gaze = latestEvent.centerGaze.x + "/" + latestEvent.centerGaze.y;
                        filtered = filteredEvent.centerGaze.x + "/" + filteredEvent.centerGaze.y;
                    }
                    
                    this.diagnosis.channel(CommonRegistryTracer.class).status("sender/receivedsome",  new OptionInfo("alreadyreceived", "" + this.numEventsReceived), new OptionInfo("gazepos", gaze), new OptionInfo("filteredpos", filtered));                    
                }


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
                this.diagnosis.channel(CommonRegistryTracer.class).status("sender/exception/interrupted", new OptionInfo("message", e.getMessage()),  new OptionInfo("alreadyreceived", "" + this.numEventsReceived));                             
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
            latestEvent.leftGaze = this.displacementFilter.filterEvent(latestEvent.leftGaze);
            latestEvent.rightGaze = this.displacementFilter.filterEvent(latestEvent.rightGaze);
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
        this.diagnosis.channel(CommonRegistryTracer.class).status("sendcommand/command", new OptionInfo("command", command.toString()));
        
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
                this.displacementFilter.updateReferencePoint(point, dx, dy, time);
            }
            break;
        case DROP_RECALIBRATION:
            this.displacementFilter.clearReferencePoints();
            break;
        case HARDWARE_CALIBRATION:
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
