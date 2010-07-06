/*
 * Launcher.java
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
package de.dfki.km.text20.trackingserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.JSPFProperties;

/**
 * @author Ralf Biedert
 */
public class Launcher {
    /**
     * Start the tracking server
     * 
     * @param args
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws MalformedURLException,
                                          UnknownHostException, URISyntaxException {

        
        final JSPFProperties props = new JSPFProperties();

        try {
            props.load(new FileInputStream(new File("config.properties")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        props.setProperty(PluginManager.class, "logging.level", "INFO");
        props.setProperty(PluginManager.class, "cache.enabled", "true");
        props.setProperty(PluginManager.class, "cache.mode",    "weak"); //optional
        
        
        final PluginManager pluginManager = PluginManagerFactory.createPluginManager(props);
        
        pluginManager.addPluginsFrom(new File("plugins/").toURI());
        pluginManager.addPluginsFrom(new URI("classpath://*"));
    }
}
