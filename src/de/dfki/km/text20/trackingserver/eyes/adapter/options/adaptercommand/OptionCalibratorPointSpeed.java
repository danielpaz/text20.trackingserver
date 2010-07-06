package de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand;

import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;

/**
 *  Speed of the point movement
 */
public class OptionCalibratorPointSpeed implements AdapterCommandOption {
    private static final long serialVersionUID = 8921013803047201819L;

    private final int pointSpeed;

    /**
     * @return .
     */
    public int getPointSpeed() {
        return this.pointSpeed;
    }

    /**
     * @param pointSpeed
     */
    public OptionCalibratorPointSpeed(int pointSpeed) {
        this.pointSpeed = pointSpeed;
    }
}
