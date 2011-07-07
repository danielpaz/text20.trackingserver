/*
 * TrackingDeviceInformation.java
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
package de.dfki.km.text20.trackingserver.brain.remote;

import de.dfki.km.text20.trackingserver.common.remote.CommonDeviceInformation;

/**
 * Information for the given tracking device.
 * 
 * @author Ralf Biedert
 */
public class TrackingDeviceInformation extends CommonDeviceInformation {
    /** */
    private static final long serialVersionUID = -7898108922872157550L;
    
    /** Names and orders of our channels */
    public String[] channelNames;

    /** The type of the adapter */
    public String hardwareType;
}
