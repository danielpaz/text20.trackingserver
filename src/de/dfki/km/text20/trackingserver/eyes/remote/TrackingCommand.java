/*
 * TrackingCommand.java
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
package de.dfki.km.text20.trackingserver.eyes.remote;

/**
 * @author rb
 *
 */
public enum TrackingCommand {
    
    /**
     * Start the device 
     */
    START,
    
    /**
     * Stop the device.
     */
    STOP,
    
    /**
     * Recalibrates the tracking device on the fly. 
     */
    ONLINE_RECALIBRATION,
    
    /**
     * Recalibrates/updates reference points without clearing existing points.
     */
    UPDATE_CALIBRATION,
    
    /**
     * Removes all recalibration points. Should be called before a ONLINE_RECALIB. is performed.
     */
    DROP_RECALIBRATION,

    /**
     * Tell the adapter to perform an internal (hardware) calibration
     */
    HARDWARE_CALIBRATION,

}
