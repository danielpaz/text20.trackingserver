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

import static net.jcores.CoreKeeper.$;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.jcores.interfaces.functions.F0;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.diagnosis.local.Diagnosis;
import net.xeoh.plugins.diagnosis.local.util.DiagnosisUtil;
import net.xeoh.plugins.diagnosis.local.util.conditions.TwoStateMatcherAND;
import net.xeoh.plugins.diagnosis.local.util.conditions.matcher.Is;
import net.xeoh.plugins.meta.updatecheck.UpdateCheck;
import net.xeoh.plugins.meta.updatecheck.UpdateInformation;
import net.xeoh.plugins.meta.updatecheck.UpdateListener;
import de.dfki.km.text20.trackingserver.eyes.adapter.impl.tobii.diagnosis.channels.status.TobiiSDKNotFoundStatus;
import de.dfki.km.text20.trackingserver.eyes.remote.diagnosis.channel.status.ReceivingEvents;
import de.dfki.km.text20.trackingserver.ui.monitor.Monitor;

/**
 * @author Ralf Biedert
 */
@PluginImplementation
public class MonitorImpl implements Monitor {
    /** Needed to monitor our status */
    @InjectPlugin
    public Diagnosis diagnosis;
    
    /** To check and inform upon new versions */
    @InjectPlugin
    public UpdateCheck updateCheck;

    /** Current tray icon */
    TrayIcon trayIcon;

    /** Current system tray */
    SystemTray systemTray;

    /** Our three images (bad, uncertain, good) */
    final Image images[] = new Image[3];

    /** Messages to keep */
    final List<String> messages = new ArrayList<String>();

    /** Joined status of all sub-modules. */
    final Map<String, Integer> joinedStatus = new ConcurrentHashMap<String, Integer>();

    /** Create GUI and other components */
    @Init
    public void init() {
        // Check if we might have a system tray
        if (!SystemTray.isSupported()) return;

        this.images[0] = Toolkit.getDefaultToolkit().getImage(MonitorImpl.class.getResource("win.bad.gif"));
        this.images[1] = Toolkit.getDefaultToolkit().getImage(MonitorImpl.class.getResource("win.uncertain.gif"));
        this.images[2] = Toolkit.getDefaultToolkit().getImage(MonitorImpl.class.getResource("win.good.gif"));

        // Start in uncertain mode ...
        try {
            // Load and start tray + icon
            this.trayIcon = new TrayIcon(this.images[1], "Tracking Server");
            this.trayIcon.setPopupMenu(getMenu());
           
            this.systemTray = SystemTray.getSystemTray();
            this.systemTray.add(this.trayIcon);

            message("Starting up Text 2.0 Tracking Server...");
        } catch (AWTException e) {
            e.printStackTrace();
        }

        
        // Add an update listener to the tracking server
        this.updateCheck.registerUpdateListner(new UpdateListener() {
            @Override
            public void onUpdate(final UpdateInformation info) {
                // In case we have an update info, add the download button
                $.edt(new F0() {
                    @Override
                    public void f() {
                        // Create a new menu item 
                        final MenuItem update = new MenuItem("Download Update");
                        update.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // First, we notify the user ...
                                userNotify("Update Info", "Starting download ...", MessageType.INFO);
                                message("Starting download to " + new File(".").getAbsolutePath());

                                // Then we start downloading in the background
                                $.oneTime(new F0() {
                                    @Override
                                    public void f() {
                                        // Download this to our folder ...
                                        $(info.url).uri().download(".");
                                        
                                        // And inform the user that we have something
                                        $.edt(new F0 () {
                                            @Override
                                            public void f() {
                                                userNotify("Update Info", "Update has been downloaded to the tracking server's folder.", MessageType.INFO);
                                                message("Update downloaded.");
                                                try {
                                                    Desktop.getDesktop().open(new File("."));
                                                } catch (IOException xxx) {
                                                }
                                                
                                                // Eventually remove the update menu
                                                MonitorImpl.this.trayIcon.getPopupMenu().remove(update);
                                            }
                                        });
                                    }
                                }, 1);
                            }
                        });
                        
                        // And add it
                        MonitorImpl.this.trayIcon.getPopupMenu().addSeparator();
                        MonitorImpl.this.trayIcon.getPopupMenu().add(update);
                    }
                });
                
                // Also, notify the user
                userNotify("Update Info", "There is a new version (" + info.latestVersion + ") available for download. Get it at text20.net, or by clicking this icon's menu.", MessageType.INFO);
            }
            
            @Override
            public void onMessage(String message) {
                //
            }
        });

        
        registerConditions();
    }

    /**
     * Depending on the platform, display a message to the user.
     * 
     * @param title
     * @param text
     * @param type
     */
    void userNotify(String title, String text, MessageType type) {
        System.out.println(title + ": " + text);
        if (!$(System.getProperty("os.name")).get("UNDEFINED").toLowerCase().contains("win") || this.trayIcon == null)
            return;
        this.trayIcon.displayMessage(title, text, type);
    }

    
    /** */
    private void registerConditions() {
        // We register a new monitor for each condition we like to observe.
        final DiagnosisUtil util = new DiagnosisUtil(this.diagnosis);

        // Startup Monitor (become ON after startup is complete)
        util.registerCondition(new TwoStateMatcherAND() {
            @Override
            protected void setupMatcher() {
                match(ReceivingEvents.class, new Is(Boolean.TRUE));
            }

            @Override
            public void stateChanged(STATE arg0) {
                MonitorImpl.this.trayIcon.setImage(arg0 == STATE.ON ? MonitorImpl.this.images[2] : MonitorImpl.this.images[0]);
                message(arg0 == STATE.ON ? "Receiving Events" : "Error receiving events");

                // In case things go wrong
                if (arg0 == STATE.OFF) {
                    userNotify("Tracking Server", "Due to an error the eye tracking processing stopped. Please see the logs for details.", MessageType.ERROR);
                }
            }
        });

        // Tobii SDK not found monitor (will be fired only once)
        util.registerCondition(new TwoStateMatcherAND() {
            @Override
            protected void setupMatcher() {
                match(TobiiSDKNotFoundStatus.class, new Is(Boolean.TRUE));
            }

            @Override
            public void stateChanged(STATE arg0) {
                if (arg0 == STATE.OFF) return;

                MonitorImpl.this.trayIcon.setImage(MonitorImpl.this.images[0]);
                message("Tobii SDK not installed. Please download the Tobii SDK v2 and install it before starting the tracking server. Also, check the configured API version to use (v2/v5) inside 'config.properties'.");
                userNotify("Tracking Server", "Tobii SDK not found. Please install the SDK before starting the tracking server. Also, check the configured API version to use (v2/v5).", MessageType.ERROR);
            }
        });

    }

    /**
     * Adds a message to our log and the image tooltip
     * 
     * @param message Message to add
     */
    public void message(String message) {
        this.trayIcon.setToolTip(message);

        synchronized (this.messages) {
            this.messages.add(System.currentTimeMillis() + ": " + message);
            if (this.messages.size() > 50) this.messages.remove(0);
        }
    }

    /**
     * Returns the Popup menu
     * 
     * @return
     */
    PopupMenu getMenu() {
        final PopupMenu menu = new PopupMenu();
        final MenuItem title = new MenuItem("Text 2.0 Tracking Server");

        menu.add(title);
        menu.addSeparator();

        // Add our exit menu
        final MenuItem console = new MenuItem("Show Console");
        console.addActionListener(new ActionListener() {
            @SuppressWarnings("static-access")
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame frame = new JFrame();
                final Container contentPane = frame.getContentPane();
                final JTextArea textArea = new JTextArea();

                textArea.setEditable(false);
                textArea.setText($(MonitorImpl.this.messages).string().join("\n"));

                contentPane.setLayout(new BorderLayout());
                contentPane.add(new JScrollPane(textArea), BorderLayout.CENTER);

                frame.setTitle("Text 2.0 Tracking Diagnosis Log");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(400, 600);
                frame.setVisible(true);
            }
        });
        menu.add(console);

        // Add our exit menu
        final MenuItem exitMenu = new MenuItem("Quit Server");
        exitMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(exitMenu);

        return menu;
    }
}
