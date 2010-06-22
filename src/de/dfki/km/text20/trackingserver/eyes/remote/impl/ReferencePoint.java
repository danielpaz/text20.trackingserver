/*
 * RefPoint.java
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
import java.io.Serializable;

/**
 * @author rb
 *
 */
public class ReferencePoint implements Serializable {
    /** */
    private static final long serialVersionUID = -2617480612742875654L;

    final Point position;

    final long time;

    final double xDisplacement;

    final double yDisplacement;

    /**
     * @param referencePoint
     * @param xDisplacement
     * @param yDisplacement
     * @param time
     */
    public ReferencePoint(final Point referencePoint, final double xDisplacement,
                          final double yDisplacement, final long time) {
        this.position = referencePoint;
        this.xDisplacement = xDisplacement;
        this.yDisplacement = yDisplacement;
        this.time = time;
    }

    /**
     * @return the position
     */
    public Point getPosition() {
        return this.position;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return this.time;
    }

    /**
     * @return the xDisplacement
     */
    public double getxDisplacement() {
        return this.xDisplacement;
    }

    /**
     * @return the yDisplacement
     */
    public double getyDisplacement() {
        return this.yDisplacement;
    }
}