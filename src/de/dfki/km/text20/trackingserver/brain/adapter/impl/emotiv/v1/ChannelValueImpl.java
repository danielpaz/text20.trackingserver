/*
 * ChannelValueImpl.java
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
package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.v1;

import de.dfki.km.text20.trackingserver.brain.adapter.ChannelValue;

public class ChannelValueImpl implements ChannelValue {
    private double value;
    private String id;

    public ChannelValueImpl(String id, double value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String getID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getValue() {
        // TODO Auto-generated method stub
        return 0;
    }

}
