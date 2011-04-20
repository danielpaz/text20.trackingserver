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

import static net.jcores.CoreKeeper.$;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.swing.UIManager;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.meta.updatecheck.UpdateCheck;

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
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws MalformedURLException,
                                          UnknownHostException, URISyntaxException,
                                          FileNotFoundException {

        // Set global look and feel. 
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("Unable to load native look and feel");
        }

        // The first thing we do is to redirect std out / std err (Fixed #10)
        if ($(args).filter("-noredirect").size() == 0) {
            // TODO: uncomment this
            //            System.setOut(new PrintStream("output.log"));
            //            System.setErr(new PrintStream("error.log"));
        }

        // Now we start JSPF and the rest
        final JSPFProperties props = new JSPFProperties();

        try {
            props.load(new FileInputStream(new File("config.properties")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        props.setProperty(PluginManager.class, "cache.enabled", "true");
        props.setProperty(PluginManager.class, "cache.mode", "weak");
        props.setProperty(UpdateCheck.class, "update.url", "http://api.text20.net/common/versioncheck/");
        props.setProperty(UpdateCheck.class, "product.name", "text20.trackingserver");
        props.setProperty(UpdateCheck.class, "product.version", "1.4");

        final PluginManager pluginManager = PluginManagerFactory.createPluginManager(props);

        pluginManager.addPluginsFrom(new File("plugins/").toURI());
        pluginManager.addPluginsFrom(new URI("classpath://*"));
    }
}
