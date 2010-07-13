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

package de.dfki.km.text20.trackingserver.common.remote.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.PluginInformation;
import net.xeoh.plugins.base.PluginInformation.Information;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.base.util.PluginConfigurationUtil;
import net.xeoh.plugins.remote.ExportResult;
import net.xeoh.plugins.remote.RemoteAPILipe;
import de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter;
import de.dfki.km.text20.trackingserver.common.adapter.CommonAdapter;
import de.dfki.km.text20.trackingserver.common.remote.CommonClientCallback;
import de.dfki.km.text20.trackingserver.common.remote.CommonDeviceInformation;
import de.dfki.km.text20.trackingserver.common.remote.CommonTrackingEvent;
import de.dfki.km.text20.trackingserver.common.remote.CommonTrackingServerRegistry;

/**
 * 
 * @author Ralf Biedert
 * 
 * @param <T> 
 * @param <C> 
 * @param <I> 
 * @param <A> 
 */
public class CommonServerRegistry<T extends CommonTrackingEvent, C extends CommonClientCallback<T>, I extends CommonDeviceInformation, A extends CommonAdapter<T, I>>
        implements CommonTrackingServerRegistry<T, C, I> {

    /** */
    @InjectPlugin
    public RemoteAPILipe remoteAPILipe;

    /** */
    @InjectPlugin
    public PluginConfiguration configuration;

    /** */
    @InjectPlugin
    public PluginInformation information;

    /** */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /** */
    protected final List<BlockingQueue<T>> callbacks = new ArrayList<BlockingQueue<T>>();

    /** */
    protected final BlockingQueue<T> events = new LinkedBlockingQueue<T>();

    /** */
    protected final Lock callbacksLock = new ReentrantLock();

    /** */
    protected long numEventsReceived = 0;

    /** */
    protected BrainAdapter usedBrainAdpater;

    /** ID of the adapter to use */
    protected String adapterID = "undefined";

    /** Adapter used */
    protected A usedAdpater;
    

    /** */
    @Init
    public void init() {
        final PluginConfigurationUtil pcu = new PluginConfigurationUtil(this.configuration);
        this.adapterID = pcu.getString(getClass(), "adapter.id");
        
        // Debug the used adapter.
        this.logger.info("Requested adapter " + this.adapterID);

        // Export the plugin
        final ExportResult exportResult = this.remoteAPILipe.exportPlugin(this);
        final Collection<URI> exportURIs = exportResult.getExportURIs();

        // Log to console ... might be useful ...
        for (URI uri : exportURIs) {
            this.logger.info("Listening on URL " + uri);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.km.augmentedtext.trackingserver.remote.TrackingServerRegistry
     * #addTrackingListener
     * (de.dfki.km.augmentedtext.trackingserver.remote.TrackingClientCallback)
     */
    @Override
    public void addTrackingListener(final C callback) {
        final BlockingQueue<T> queue = new LinkedBlockingQueue<T>(10);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {

            @Override
            public void run() {
                // Pump data
                while (true) {
                    try {
                        final T event = queue.take();
                        callback.newTrackingEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        this.callbacksLock.lock();
        try {
            this.callbacks.add(queue);
        } finally {
            this.callbacksLock.unlock();
        }
    }

    /**
     * Callback for every loaded adapter.
     * 
     * @param adapter
     */
    @PluginLoaded
    public void pluginAdded(A adapter) {

        // Only add this once.
        if (this.usedBrainAdpater != null) return;

        // Obtain capabilities
        final Collection<String> info = this.information.getInformation(Information.CAPABILITIES, adapter);

        // Check if this is the requested adapter
        if (info.contains(this.adapterID)) {
            setupAdapter(adapter);
        }
    }

    /**
     * Sets up the given adapter.
     * 
     * @param adapter
     */
    private void setupAdapter(A adapter) {
        this.logger.fine("Starting adapter " + adapter);
        this.usedAdpater = adapter;
        this.usedAdpater.setup(this.events);
        this.usedAdpater.start();
    }

    /** */
    @Thread(isDaemonic = false)
    public void senderThread() {
        while (true) {
            try {
                // Try to get the lastest event
                final T latestEvent = this.events.poll(500, TimeUnit.MILLISECONDS);

                if (latestEvent == null) {
                    continue;
                }

                // Increase number of successful events
                this.numEventsReceived++;

                // Send evens to listener
                this.callbacksLock.lock();
                try {
                    for (BlockingQueue<T> queue : this.callbacks) {
                        queue.offer(latestEvent);
                    }
                } finally {
                    this.callbacksLock.unlock();
                }
            } catch (InterruptedException e) {
                this.logger.warning("Error waiting for some result...");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.km.augmentedtext.trackingserver.remote.TrackingServerRegistry
     * #getTrackingDeviceInformation()
     */
    @Override
    public I getTrackingDeviceInformation() {
        if (this.usedAdpater == null) return null;

        return this.usedAdpater.getDeviceInformation();
    }
}
