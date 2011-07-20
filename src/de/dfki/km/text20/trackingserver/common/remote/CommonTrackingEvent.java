/*
 * CommonTrackingEvent.java
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
package de.dfki.km.text20.trackingserver.common.remote;

import java.io.Serializable;

/**
 * A common tracking event for all tracking devices
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CommonTrackingEvent implements Serializable, Cloneable {
    /**  */
    private static final long serialVersionUID = -7905990708070667751L;
    
    /** Time this event was observer (in ms) */
    public long observationTime = System.currentTimeMillis();
    
    /** Relative time this event was observed (in ns) */
    public long elapsedTime = 0;
}
