/*
 * MonitorImpl.java
 * 
 * Copyright (c) 2011, Ralf Biedert All rights reserved.
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
package de.dfki.km.text20.trackingserver.ui.monitor.impl;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import de.dfki.km.text20.trackingserver.ui.monitor.Monitor;

/**
 * @author Ralf Biedert
 */
@PluginImplementation
public class MonitorImpl implements Monitor {
    /** Current tray icon */
    private TrayIcon trayIcon;

    /** Current system tray */
    private SystemTray systemTray;
    
    /** Create GUI and other components */
    @Init
    public void init() {
        // Check if we might have a system tray
        if (!SystemTray.isSupported()) return;

        try {
            // Load and start tray + icon
            final Image image = Toolkit.getDefaultToolkit().getImage(MonitorImpl.class.getResource("TrayIconInactive.gif"));
            this.trayIcon = new TrayIcon(image, "Tracking Server");
            this.systemTray = SystemTray.getSystemTray();
            this.systemTray.add(this.trayIcon);
            
            this.trayIcon.displayMessage("X", "Y", MessageType.ERROR);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
