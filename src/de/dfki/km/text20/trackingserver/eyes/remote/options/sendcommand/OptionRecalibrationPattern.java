/*
 * OptionRecalibrationPattern.java
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
package de.dfki.km.text20.trackingserver.eyes.remote.options.sendcommand;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.dfki.km.text20.trackingserver.eyes.remote.options.SendCommandOption;

/**
 * @author rb
 *
 */
public class OptionRecalibrationPattern implements SendCommandOption {
    /** */
    private static final long serialVersionUID = -1214134228018911066L;

    /** */
    List<Object[]> points = new ArrayList<Object[]>();

    /**
     * @param p
     * @param dx
     * @param dy
     * @param time
     */
    public void addPoint(Point p, int dx, int dy, long time) {
        this.points.add(new Object[] { p, new Integer(dx), new Integer(dy), new Long(time) });
    }

    /**
     * @return .
     */
    public List<Object[]> getPoints() {
        return this.points;
    }

}
