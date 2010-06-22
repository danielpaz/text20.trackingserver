package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.expressive.v1.brainplugin;

import net.xeoh.plugins.base.Plugin;

import org.json.JSONArray;


public interface BrainPlugin extends Plugin{
	public boolean connectToEngine();
	public JSONArray getBrainEvents();
	
	public JSONArray getEmotion();
	public boolean isConnected();
}
