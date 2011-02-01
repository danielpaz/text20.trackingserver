package de.dfki.km.text20.trackingserver.eyes.adapter.impl.tobii;

import java.awt.Color;
import java.util.logging.Logger;

import net.xeoh.plugins.diagnosis.local.DiagnosisChannel;

import com4j.ComException;

import de.dfki.eyetracker.EyetrackerException;
import de.dfki.eyetracker.tetapi.common.TetNumCalibPoints;
import de.dfki.eyetracker.tetapi.v2.ClassFactory;
import de.dfki.eyetracker.tetapi.v2.ITetCalibProc;
import de.dfki.eyetracker.tetapi.v2.TetCalibPointSpeed;
import de.dfki.eyetracker.tetapi.v2.TetCalibType;

/**
 * @author Eugen Massini
 * 
 */
public class TobiiCalibratorV2 {
	/** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

	
	// TODO: probably it is good to put the following two fcts in a utility
	// class
	/**
	 * COM_COLORS are represented in BGR manner. so this converts it the right
	 * way...
	 */
	static final int getBGR(final Color color) {
		final int c = (0xFF & color.getBlue()) << 16
				| (0xFF & color.getGreen()) << 8 | 0xFF & color.getRed();
		return c;
	}

	/**
	 * This converts a BGR to Color
	 */
	static final Color getColorFromBGR(int _color) {

		int color = _color;
		final int red = color & 0xFF;

		color >>= 8;
		final int green = color & 0xFF;

		color >>= 8;
		final int blue = color & 0xFF;

		return new Color(red, green, blue);
	}

	DiagnosisChannel<String> log;
	private int bgColor;
	private ITetCalibProc calib;
	private TetNumCalibPoints numPoints;
	private int pointColor;
	private TetCalibPointSpeed pointSpeed;

	/**
	 * @param tobiiGazeAdapter 
     * 
     */
	public TobiiCalibratorV2(TobiiGazeAdapter tobiiGazeAdapter) {
		this.numPoints = TetNumCalibPoints.TetNumCalibPoints_5;
		this.pointSpeed = TetCalibPointSpeed.TetCalibPointSpeed_Medium;
		this.pointColor = getBGR(new Color(100, 100, 255));
		this.bgColor = getBGR(new Color(163, 163, 163));
		this.log = tobiiGazeAdapter.log;
	}

	/**
	 * Tries to create the calibration with a connection to server...
	 * 
	 * @param tetServer
	 * @param tetPort
	 * @throws EyetrackerException
	 */
	public void buildUp(final String tetServer, final int tetPort)
			throws EyetrackerException {

		// create com object
		this.calib = ClassFactory.createTetCalibProc();

		if (this.calib == null)
			throw new EyetrackerException(
					"creation of TetCalibProc object failed.");

		try {
			// try to establish connection
			this.calib.connect(tetServer, tetPort);

			if (!this.calib.isConnected()) // no connection -> errror
				throw new EyetrackerException(
						"connecting to tet server failed.");

			// // add event handler for calibration window
			// this.evtCalibCookie = calib.advise(DTetCalibProcEvents.class,
			// new DTetCalibProcEvents() {
			// @Override
			// public void onCalibrationEnd(int hr) {
			//							
			// // calib.windowVisible(false);
			// }
			// });

			// this.calibmanager = this.calib.calibManager();
		} catch (final ComException e) {
			// could be thrown if something unexpectable went wrong
			throw new EyetrackerException(e.getMessage());
		}
	}

	/**
	 * @throws EyetrackerException
	 */
	public void calibrate(/* int timeoutMs */) throws EyetrackerException {
		startCalibration();
	}

	/**
	 * @param bgColor2
	 */
	public void setBgColor(final Color bgColor2) {
		this.bgColor = getBGR(bgColor2);
	}

	/**
	 * Change the window visibility state if necessary
	 * 
	 * @param state
	 */
	public void setCalibWindowVisibleAndTopmost(final boolean state) {
		if (this.calib == null)
			return;

		try {
			if (state != this.calib.windowVisible()) {
				this.calib.windowVisible(state);
			}
		} catch (Exception e) {
            this.logger.warning("Unable to alter window visibility ... Here comes our strange bug again");		}
		
		try {
			if (state != this.calib.windowTopmost()) {
				this.calib.windowTopmost(state);				
			}
		} catch (Exception e) {
			this.logger.warning("Unable to alter window topmostness ... Here comes our strange bug again");
		}
	}

	/**
	 * @param pointColor
	 * @param bgColor
	 */
	public void setColor(final Color pointColor, final Color bgColor) {
		setPointColor(pointColor);
		setBgColor(bgColor);
	}

	/**
	 * will be converted to the nearest of 2, 5 or 9
	 * 
	 * @param numPoints
	 */
	public void setNumPoints(final int numPoints) {
		final int d2 = Math.abs(2 - numPoints);
		final int d5 = Math.abs(5 - numPoints);
		final int d9 = Math.abs(9 - numPoints);

		// find the minimum distance
		final int minD = Math.min(d2, Math.min(d5, d9));

		if (minD == d2) {
			this.numPoints = TetNumCalibPoints.TetNumCalibPoints_2;
		} else if (minD == d5) {
			this.numPoints = TetNumCalibPoints.TetNumCalibPoints_5;
		} else if (minD == d9) {
			this.numPoints = TetNumCalibPoints.TetNumCalibPoints_9;
		}
	}

	/**
	 * @param pointColor2
	 */
	public void setPointColor(final Color pointColor2) {
		this.pointColor = getBGR(pointColor2);
	}

	/**
	 * close connection to the calibratorV2 and release com-object
	 */
	// public void quit() {
	// if (this.calib != null){
	// stopCalibration();
	// this.calib.disconnect();
	// this.calib.dispose();
	// this.calib = null;
	// }
	// }

	/**
	 * Will be fit to the range 1 to 5. Where 1 is the fastest and 5 the slowest
	 * 
	 * it just set the values bigger the 5 to 5 and lower the 1 to 1
	 * 
	 * @param _ps
	 */
	public void setPointSpeed(final int _ps) {
		int ps = _ps;

		if (ps < 1) {
			ps = 1;
		} else if (ps > 5) {
			ps = 5;
		}

		switch (ps) {
		case 1:
			this.pointSpeed = TetCalibPointSpeed.TetCalibPointSpeed_Fast;
			break;
		case 2:
			this.pointSpeed = TetCalibPointSpeed.TetCalibPointSpeed_MediumFast;
			break;
		case 3:
			this.pointSpeed = TetCalibPointSpeed.TetCalibPointSpeed_Medium;
			break;
		case 4:
			this.pointSpeed = TetCalibPointSpeed.TetCalibPointSpeed_MediumSlow;
			break;
		case 5:
			this.pointSpeed = TetCalibPointSpeed.TetCalibPointSpeed_Slow;
			break;
		}
	}

	/**
	 * Starts the calibration process.
	 */
	private void startCalibration() throws EyetrackerException {

		if (this.calib == null)
			return;

		try {

			setCalibWindowVisibleAndTopmost(true);

			// TODO: If we enable this, we're unable to perform a second calib
			// as COM4J appears to be unable to set the window topmost (says something
			// about "window is already visible")
			if (/*!this.calib.windowTopmost() ||*/ !this.calib.windowVisible()
					|| this.calib.isCalibrating()) {
				this.logger.warning("Unable to calibrate because conditions were not met ...");
				return;
			}

			this.calib.numPoints(this.numPoints);
			this.calib.pointColor(this.pointColor);
			this.calib.pointSpeed(this.pointSpeed);
			this.calib.backgroundColor(this.bgColor);

			try {
				this.calib.startCalibration(TetCalibType.TetCalibType_Calib,
						true);
				waitForCalibrating();

				// now repeat this process as long as there are points for
				// recalibration but max 2 times
				for (int cals = 0; cals < 2 && hasRecalibrationPoints(); ++cals) {

					this.calib.startCalibration(
							TetCalibType.TetCalibType_Recalib, true);
					
					waitForCalibrating();
				}

				setCalibWindowVisibleAndTopmost(false);
			} catch (final ComException e) {
				// hope the only reason is that we dont have a connection to the
				// eye tracker.
				throw new EyetrackerException("Calibration failed", e);
			}
		} finally {
			setCalibWindowVisibleAndTopmost(false);
		}
	}

	private void waitForCalibrating() {
		try {
			while (this.calib.isCalibrating()) {
				Thread.sleep(100);
			}
		} catch (final InterruptedException e) {
			//
		}
	}

	// private void stopCalibration(){
	// if (this.calib == null)
	// return;
	// if (this.calib.isCalibrating()){
	// this.calib.interruptCalibration();
	// calib.windowVisible(false);
	// }
	// }

	/**
	 * After a calibration process there could be some points which we have to
	 * recalibrate. Here you can check if there are some.
	 */
	boolean hasRecalibrationPoints() {
		return this.calib.calibManager().getRecalibPoints().size() != 0;
	}

	// public void printout() {
	// if (calib.isCalibrating()){
	// System.out.println("we are still calibrating");
	// return;
	// }
	//		
	// ITetCalibManager mgr = calib.calibManager();
	// ITetPointDArray points = mgr.getRecalibPoints();
	// mgr.
	// }

}
