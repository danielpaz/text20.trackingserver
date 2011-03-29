/*
 * DecayingDisplacementFilter.java
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
package de.dfki.km.text20.trackingserver.eyes.remote.impl;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * BIG FAT FIXME: This code is cloned from Augmented Text Client, but the original code has 
 * dependencies to other interfaces as well. How do we fix that?
 * 
 * @author Eugen Massini
 */
public class ReferenceBasedDisplacementFilter {

    /** */
    private final List<Double> lenBuffer = new ArrayList<Double>();

    /** */
    private double radiusSq = 10000.;

    /** */
    private final List<ReferencePoint> references = new ArrayList<ReferencePoint>();

    /**
     * @param point
     * @return .
     */
    Point calcDisplacement(final Point point) {
        if (point == null) return point;

        buildLenBuffer(point);
        final int bufSize = this.lenBuffer.size();

        assert bufSize == this.references.size();

        final Point2D.Double p = new Point2D.Double(point.x, point.y);
        for (int i = 0; i < bufSize; ++i) {
            final double len = this.lenBuffer.get(i).doubleValue();
            final double dispX = this.references.get(i).xDisplacement;
            final double dispY = this.references.get(i).yDisplacement;

            p.x += len * dispX;
            p.y += len * dispY;
        }
        return new Point((int) p.x, (int) p.y);
    }

    /**
     * Removes all reference points from filter
     */
    public synchronized final void clearReferencePoints() {
        this.references.clear();
    }

    /**
     * @param event
     * @return .
     */
    public synchronized Point filterEvent(final Point event) {
        if (event == null) return event;
        return calcDisplacement(event);
    }

    /**
     * @return .
     */
    double getRemovingRadius() {
        return Math.sqrt(this.radiusSq);
    }

    /**
     * @param radius
     */
    void setRemovingRadius(final double radius) {
        this.radiusSq = radius * radius;
    }

    /**
     * Called when a new reference point becomes available.
     * 
     * @param referencePoint The measurement point to which the displacement applies.
     * @param xdisplacement If a point has been measured at the ref. point, apply this x displacement to correct it.
     * @param ydisplacement If a point has been measured at the ref. point, apply this y displacement to correct it.
     * @param measurementTime The time the displacements have been measured.
     */
    public synchronized void updateReferencePoint(final Point referencePoint,
                                                  final int xdisplacement,
                                                  final int ydisplacement,
                                                  final long measurementTime) {
        if (referencePoint == null) return;

        // TODO: What to do with the time????
        clearNearestRefPoints(referencePoint);
        this.references.add(new ReferencePoint(referencePoint, xdisplacement, ydisplacement, measurementTime));
    }

    /**
     * @param from
     */
    void clearNearestRefPoints(final Point from) {
        if (from == null) return;
        for (int i = 0; i < this.references.size(); ++i) {
            final ReferencePoint p = this.references.get(i);
            if (p.position.distanceSq(from) < this.radiusSq) {
                this.references.remove(i);
            }
        }
    }

    /**
     * @param point
     */
    final void buildLenBuffer(final Point point) {
        if (point == null) return;
        this.lenBuffer.clear();

        // NOTE: Assume there could be at most  one vector with distance 0 to the point
        int zeroAt = -1;
        double sumLen = 0.0;
        for (int i = 0; i < this.references.size(); ++i) {
            final double len = point.distance(this.references.get(i).position);
            if (len == 0.) {
                zeroAt = i;
                break;
            }
            this.lenBuffer.add(Double.valueOf(len));
            sumLen += len;
        }

        final int bufSize = this.references.size();
        // just for faster computation....
        if (zeroAt > -1) {
            this.lenBuffer.clear();
            for (int i = 0; i < bufSize; ++i)
                if (i != zeroAt) {
                    this.lenBuffer.add(Double.valueOf(0.));
                } else {
                    this.lenBuffer.add(Double.valueOf(1.));
                }

            return;
        }

        // else part: no zero lengths...

        double invSumLen = 1. / sumLen;
        // normalize
        for (int i = 0; i < bufSize; ++i) {
            double len = this.lenBuffer.get(i).doubleValue();

            len *= invSumLen;

            this.lenBuffer.set(i, Double.valueOf(len));
        }

        double prodLen = 1.;
        for (final Double d : this.lenBuffer) {
            prodLen *= d.doubleValue();
        }

        // transformation
        sumLen = 0.;
        for (int i = 0; i < bufSize; ++i) {
            double len = this.lenBuffer.get(i).doubleValue();
            len = Math.pow(prodLen / len, 2);
            sumLen += len;
            this.lenBuffer.set(i, Double.valueOf(len));
        }

        // normalization
        invSumLen = 1. / sumLen;
        for (int i = 0; i < bufSize; ++i) {
            double len = this.lenBuffer.get(i).doubleValue();
            len *= invSumLen;
            this.lenBuffer.set(i, Double.valueOf(len));
        }
    }
}
