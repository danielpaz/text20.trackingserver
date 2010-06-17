package de.dfki.km.text20.trackingserver.eyes.adapter.impl.tobii;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import de.dfki.eyetracker.filter.impl.AbstractEventFilter;
import de.dfki.eyetracker.filter.impl.SingleCoordCombinationFilter;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;
import de.dfki.util.event.Event;

/**
 * Listens for events and puts them into a queue.
 * 
 * @author rb
 */
class Filter extends AbstractEventFilter {

    /** */
    private final TobiiGazeAdapter m_tobiiGazeAdapter;

    /**
     * @param tobiiGazeAdapter
     */
    Filter(final TobiiGazeAdapter tobiiGazeAdapter) {
        this.m_tobiiGazeAdapter = tobiiGazeAdapter;
    }

    @SuppressWarnings("boxing")
    public void processTrackingEvent(final Event e) {

        try {
            // TODO: Sometimes this filter simply isn't called anymore from the EyeTracking2Java Library...

            // Obtain ???
            final Map<String, Object> pixels = e.get(SingleCoordCombinationFilter.class.getName());

            final HashMap<String, Object> m = e.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_DATA_RAW);
            // Sanity check
            if (pixels == null) return;

            // Extract positions
            final int x = (Integer) pixels.get(SingleCoordCombinationFilter.GAZE_PIXEL_X);
            final int y = (Integer) pixels.get(SingleCoordCombinationFilter.GAZE_PIXEL_Y);

            final float xl = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.CAMERA_POS_X_LEFT_EYE).toString());
            final float yl = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.CAMERA_POS_Y_LEFT_EYE).toString());
            final float dl = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.DISTANCE_LEFT_EYE).toString());
            final float xr = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.CAMERA_POS_X_RIGHT_EYE).toString());
            final float yr = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.CAMERA_POS_Y_RIGHT_EYE).toString());
            final float dr = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.DISTANCE_RIGHT_EYE).toString());

            final float pl = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.PUPIL_DIAMETER_LEFT_EYE).toString());
            final float pr = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.PUPIL_DIAMETER_RIGHT_EYE).toString());

            final float x2 = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_POS_X_LEFT_EYE).toString());
            final float y2 = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_POS_Y_LEFT_EYE).toString());
            final float x3 = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_POS_X_RIGHT_EYE).toString());
            final float y3 = Float.parseFloat(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_POS_Y_RIGHT_EYE).toString());

            // Extract flags
            final int _validity = (Integer) pixels.get(SingleCoordCombinationFilter.VALIDITY_DATA);
            final boolean valitity = _validity == SingleCoordCombinationFilter.VALIDITY_VALID || _validity == SingleCoordCombinationFilter.VALIDITY_SEMI_VALID;

            // Setup a serializable tracking event 
            final TrackingEvent trackingEvent = new TrackingEvent();

            trackingEvent.date = System.currentTimeMillis();

            // Deprecated
            trackingEvent._centerX = x;
            trackingEvent._centerY = y;
            trackingEvent._centerValidity = valitity;

            if (valitity) {
                trackingEvent.centerGaze = new Point();
                trackingEvent.centerGaze.x = x;
                trackingEvent.centerGaze.y = y;
            }

            final float range = this.m_tobiiGazeAdapter.maxDistance - this.m_tobiiGazeAdapter.minDistance;
            final float ddl = (dl - this.m_tobiiGazeAdapter.minDistance) / range;
            final float ddr = (dr - this.m_tobiiGazeAdapter.minDistance) / range;

            trackingEvent.leftEyePos[0] = 1.0f - xl;
            trackingEvent.leftEyePos[1] = yl;
            trackingEvent.leftEyePos[2] = ddl;

            trackingEvent.rightEyePos[0] = 1.0f - xr;
            trackingEvent.rightEyePos[1] = yr;
            trackingEvent.rightEyePos[2] = ddr;

            trackingEvent.eyeDistances[0] = dl;
            trackingEvent.eyeDistances[1] = dr;

            trackingEvent.pupilSizeLeft = pl;
            trackingEvent.pupilSizeRight = pr;

            try {
                final int xr2 = Integer.parseInt(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_PIXEL_X_RIGHT_EYE).toString());
                final int yr2 = Integer.parseInt(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_PIXEL_Y_RIGHT_EYE).toString());
                final int xl2 = Integer.parseInt(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_PIXEL_X_LEFT_EYE).toString());
                final int yl2 = Integer.parseInt(m.get(de.dfki.eyetracker.event.TrackingEvent.GAZE_PIXEL_Y_LEFT_EYE).toString());
                trackingEvent.leftGaze = new Point();
                trackingEvent.rightGaze = new Point();

                trackingEvent.leftGaze.x = xl2;
                trackingEvent.leftGaze.y = yl2;
                trackingEvent.rightGaze.x = xr2;
                trackingEvent.rightGaze.y = yr2;
            } catch (final Exception ee) {
                // 
            }

            // 
            trackingEvent.gazeLeftPos[0] = x2;
            trackingEvent.gazeRightPos[0] = x3;
            trackingEvent.gazeLeftPos[1] = y2;
            trackingEvent.gazeRightPos[1] = y3;

            // And put it into the queue.		
            this.m_tobiiGazeAdapter.dequeue.add(trackingEvent);

        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }
}