/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper;

public final class EE_ExpressivThreshold_t {
  public final static EE_ExpressivThreshold_t EXP_SENSITIVITY = new EE_ExpressivThreshold_t("EXP_SENSITIVITY");

  public final int swigValue() {
    return this.swigValue;
  }
  
  @Override
  public String toString() {
    return this.swigName;
  }

  public static EE_ExpressivThreshold_t swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + EE_ExpressivThreshold_t.class + " with value " + swigValue);
  }

  private EE_ExpressivThreshold_t(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private EE_ExpressivThreshold_t(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private EE_ExpressivThreshold_t(String swigName, EE_ExpressivThreshold_t swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static EE_ExpressivThreshold_t[] swigValues = { EXP_SENSITIVITY };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}

