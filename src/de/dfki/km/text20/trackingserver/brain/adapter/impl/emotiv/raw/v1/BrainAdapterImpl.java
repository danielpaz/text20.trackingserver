/*
 * BrainAdapter.java
 * 
 * Copyright (c) 2010, André Hoffmann, DFKI. All rights reserved.
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
package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1;

import java.util.concurrent.BlockingQueue;

import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Timer;

import de.dfki.km.text20.trackingserver.brain.adapter.BrainAdapter;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.EE_DataChannel_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.EE_Event_t;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.EdkErrorCodes;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.SWIGTYPE_p_EmoStateHandle;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.SWIGTYPE_p_double;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.SWIGTYPE_p_unsigned_int;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.SWIGTYPE_p_void;
import de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper.edk;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.brain.remote.TrackingEvent;

@PluginImplementation
public class BrainAdapterImpl implements BrainAdapter {

	public final static int STATE_DISCONNECTED 		= 1; 
	
	public final static int STATE_CONNECTED 		= 2;
	
	public final static int STATE_RETRIEVING_DATA	= 3;
	
	protected BlockingQueue<TrackingEvent> 	eventQueue;
	protected boolean 						connected 		= false;
	protected boolean						readyToCollect 	= false;
	protected long 							user;
	
	protected EE_DataChannel_t 				channels[]  	= null;
	
	protected SWIGTYPE_p_void 				eEvent;
	protected SWIGTYPE_p_EmoStateHandle 	eState;		
	protected SWIGTYPE_p_unsigned_int 		nSamplesTaken; 
	protected SWIGTYPE_p_unsigned_int 		pUser;
	protected SWIGTYPE_p_void 				hData;
	
	@Override
	public TrackingDeviceInformation getDeviceInformation() {
		TrackingDeviceInformation device = new TrackingDeviceInformation();
		
		device.deviceName = "Epoc";
		device.trackingDeviceManufacturer = "Emotiv";
		
		return device;
	}

	@Override
	public void setup(BlockingQueue<TrackingEvent> eventQueue) {

		System.loadLibrary("edk");
		System.loadLibrary("edk_utils");
		System.loadLibrary("edkWrapperV1");
		
		EE_DataChannel_t channels[] = {
				EE_DataChannel_t.ED_COUNTER,
				EE_DataChannel_t.ED_AF3, 
				EE_DataChannel_t.ED_F7, 
				EE_DataChannel_t.ED_F3, 
				EE_DataChannel_t.ED_FC5, 
				EE_DataChannel_t.ED_T7, 
				EE_DataChannel_t.ED_P7, 
				EE_DataChannel_t.ED_O1, 
				EE_DataChannel_t.ED_O2, 
				EE_DataChannel_t.ED_P8, 
				EE_DataChannel_t.ED_T8, 
				EE_DataChannel_t.ED_FC6, 
				EE_DataChannel_t.ED_F4, 
				EE_DataChannel_t.ED_F8, 
				EE_DataChannel_t.ED_AF4, 
				EE_DataChannel_t.ED_GYROX, 
				EE_DataChannel_t.ED_GYROY, 
				EE_DataChannel_t.ED_TIMESTAMP, 
				EE_DataChannel_t.ED_FUNC_ID, 
				EE_DataChannel_t.ED_FUNC_VALUE, 
				EE_DataChannel_t.ED_MARKER, 
				EE_DataChannel_t.ED_SYNC_SIGNAL	
		};
			
		this.channels = channels;
		this.eventQueue = eventQueue;
	}

	@Override
	public void start() {
		if ( this.connected ) {
			return;
		}
		
		this.eEvent 				= edk.EE_EmoEngineEventCreate();
		this.eState 				= edk.EE_EmoStateCreate();
		this.nSamplesTaken 			= edk.createPUInt(0);		
		this.pUser 					= edk.createPUInt(0L);
		
		int connectionStatus 		= edk.EE_EngineConnect();
		this.connected 				= (connectionStatus == EdkErrorCodes.EDK_OK);
		this.readyToCollect			= false;

		this.hData 					= edk.EE_DataCreate();
		edk.EE_DataSetBufferSizeInSec(1.0f);
		
		if ( ! this.connected ) {
			stop();
		}
	}

	@Override
	public void stop() {
		this.connected = false;
		edk.EE_EngineDisconnect();
		edk.EE_EmoStateFree(this.eState);
		edk.EE_EmoEngineEventFree(this.eEvent);
		edk.freePUInt(this.nSamplesTaken);
		edk.freePUInt(this.pUser);
		edk.EE_DataFree(this.hData);
	}

	/**
	 * Retrieves the events from the brain tracker
	 */
	@Timer(period=50)
	public void pollChannels(){
		
		if ( this.connected ) {
		
			if ( this.readyToCollect ) {
				
				edk.EE_DataUpdateHandle(this.user, this.hData);
				edk.EE_DataGetNumberOfSample(this.hData, this.nSamplesTaken);
				
				long sampleCount = edk.pUIntToUInt(this.nSamplesTaken);
				
				if ( sampleCount > 0 ) {
					SWIGTYPE_p_double 	buffer 	= edk.createDataBuffer(this.nSamplesTaken);
					
					for ( int sample = 0; sample < sampleCount; sample++ ) {
						
						TrackingEvent t = new TrackingEvent();
						// TODO: t.date = 
						
						for (  int i = 0; i < this.channels.length; i++ ) {
							EE_DataChannel_t channel = this.channels[i];
							edk.EE_DataGet(this.hData, channel, buffer, sampleCount);
							
							String channelName = "channel:"+channel.toString().replace("ED_", "").toLowerCase();
							t.channels.put(channelName, edk.readFromDataBuffer(buffer, sample));
						}
						
						// enqueue event
						try {
							this.eventQueue.put(t);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					edk.freeDataBuffer(buffer);
				}				
			} else {
				// not yet ready to collect data
				edk.EE_EngineGetNextEvent(this.eEvent);
				
				EE_Event_t eventType = edk.EE_EmoEngineEventGetType(this.eEvent);

				edk.EE_EmoEngineEventGetUserId(this.eEvent, this.pUser);
				
				if ( eventType == EE_Event_t.EE_UserAdded ) {	

					this.user = edk.pUIntToUInt(this.pUser);
					edk.EE_DataAcquisitionEnable(this.user,true);
					
					this.readyToCollect = true;
				}
			}
		}
	}
	

	/**
     * @return .
     */
    @Capabilities
    public String[] getCapabilities() {
        return new String[] { "brainadapter:emotiv:raw" };
    }
}