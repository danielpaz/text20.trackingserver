package de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand;

import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;

/**
 *  Number of calibrating points
 */
public class OptionCalibratorNumPoints implements AdapterCommandOption {
    private static final long serialVersionUID = -2654658295588206598L;

    private final int numPoints;

    /**
     * @return .
     */
    public int getNumPoints() {
        return this.numPoints;
    }

    /**
     * @param numPoints
     */
    public OptionCalibratorNumPoints(int numPoints) {
        this.numPoints = numPoints;
    }
}
