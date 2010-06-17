package de.dfki.km.text20.trackingserver.eyes.adapter.options.adaptercommand;

import java.awt.Color;

import de.dfki.km.text20.trackingserver.eyes.adapter.options.AdapterCommandOption;

/**
 * Background Color and the color of the points
 */
public class OptionCalibratorColor implements AdapterCommandOption {
    private static final long serialVersionUID = 6137978132632197818L;

    /**
     *  Color of the points
     */
    private final Color pointColor;

    /**
     *  Background color
     */
    private final Color bgColor;

    /**
     * @param pointColor
     * @param bgColor
     */
    public OptionCalibratorColor(final Color pointColor, final Color bgColor) {
        this.pointColor = pointColor;
        this.bgColor = bgColor;
    }

    /**
     * @return .
     */
    public Color getBgColor() {
        return this.bgColor;
    }

    /**
     * @return .
     */
    public Color getPointColor() {
        return this.pointColor;
    }
}
