/*
 * TimingImpl.java
 * 
 * Copyright (c) 2011, Ralf Biedert, DFKI. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package de.dfki.km.text20.trackingserver.common.measurement.impl;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import de.dfki.km.text20.trackingserver.common.measurement.Timing;

/**
 * @author Ralf Biedert
 * @since 1.1
 */
@PluginImplementation
public class TimingImpl implements Timing {

    /** The start time in ms. */
    protected long startTimeMS = 0;

    /** The relative start time in ns */
    protected long startTimeNS = 0;

    /** Set up times */
    @Init
    public void init() {
        this.startTimeMS = System.currentTimeMillis();
        this.startTimeNS = System.nanoTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.text20.trackingserver.common.measurement.Timing#getStartTime()
     */
    @Override
    public long getStartTime() {
        return this.startTimeMS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dfki.km.text20.trackingserver.common.measurement.Timing#getElapsedTime()
     */
    @Override
    public long getElapsedTime() {
        return System.nanoTime() - this.startTimeNS;
    }
}
