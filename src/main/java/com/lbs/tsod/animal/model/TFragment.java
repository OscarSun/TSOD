package com.lbs.tsod.animal.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toyking on 2016/8/10.
 */
public class TFragment {
    public int id;
    public TPoint tp1;
    public TPoint tp2;
    public double ldd;
    public double ILAF;

    public double density;
    public double adj;

    public TFragment() {
        tp1 = new TPoint();
        tp2 = new TPoint();
    }

    public TFragment(TPoint tp1, TPoint tp2) {
        this.tp1 = tp1;
        this.tp2 = tp2;
    }

    public double getDClosest(TFragment tf) {
        double d1 = new LineSegment(tf.tp1, tf.tp2).distance(tp1);
        double d2 = new LineSegment(tf.tp1, tf.tp2).distance(tp2);
        return Math.min(d1, d2);
    }

    public double getDcenter(TFragment tf) {
        LineSegment L1 = new LineSegment(tp1, tp2);
        LineSegment L2 = new LineSegment(tf.tp1, tf.tp2);
        return L1.midPoint().distance(L2.midPoint());
    }

    public double getDHorizonal(TFragment tf) {
        LineSegment L1, L2;
        if (this.getLength() < tf.getLength()) {
            L1 = new LineSegment(tp1, tp2);
            L2 = new LineSegment(tf.tp1, tf.tp2);
        } else {
            L1 = new LineSegment(tf.tp1, tf.tp2);
            L2 = new LineSegment(tp1, tp2);
        }
        Coordinate c1 = L2.project(L1.p0);
        Coordinate c2 = L2.project(L1.p1);
        double l1 = Math.min(L2.p0.distance(c1), L2.p1.distance(c1));
        double l2 = Math.min(L2.p0.distance(c2), L2.p1.distance(c2));
        //System.out.println(c1.x + " " + c1.y + " " + c2.x + " " + c2.y + " d:" + l1 + " " + l2+" "+L2.p0.distance(L2.p1));
        return (l1 + l2) / 2.0D;
    }

    public double getDxita(TFragment tf) {
        LineSegment L1, L2;
        if (this.getLength() < tf.getLength()) {
            L1 = new LineSegment(tp1, tp2);
            L2 = new LineSegment(tf.tp1, tf.tp2);
        } else {
            L1 = new LineSegment(tf.tp1, tf.tp2);
            L2 = new LineSegment(tp1, tp2);
        }
        double angle = getAngle(tf);
        if (angle <= Math.PI / 2.0) return L1.getLength() * Math.sin(angle);
        else return L1.getLength();
    }

    //飓风（动物）的距离
    public double getDist(TFragment tf) {
        return 0.15 * getDcenter(tf) + 0.15 * getDHorizonal(tf) + 0.70 * getDxita(tf);
    }


    public double getDHorizonalBaseLine(TFragment tf) {
        return getDHorizonal(tf);
    }

    public double getDverticalBaseLine(TFragment tf) {
        LineSegment L1, L2;
        if (this.getLength() < tf.getLength()) {
            L1 = new LineSegment(tp1, tp2);
            L2 = new LineSegment(tf.tp1, tf.tp2);
        } else {
            L1 = new LineSegment(tf.tp1, tf.tp2);
            L2 = new LineSegment(tp1, tp2);
        }
        double d1 = L2.distance(L1.p0);
        double d2 = L2.distance(L1.p1);
        if (d1 + d2 > 0.0D) return (d1 * d1 + d2 * d2) / (d1 + d2);
        return 0.0D;
    }


    public double getDxitaBaseLine(TFragment tf) {
        return getDxita(tf);
    }


    public double getLength() {
        return tp1.distance(tp2);
    }

    public double getDistBaseline(TFragment tf, double wV, double wH, double wX) {
        //System.out.println(getDverticalBaseLine(tf) + " " + getDHorizonalBaseLine(tf) + " " + getDxitaBaseLine(tf));
        return getDverticalBaseLine(tf) * wV + getDHorizonalBaseLine(tf) * wH + getDxitaBaseLine(tf) * wX;
    }

    public double getAnimalDiff(TFragment tf) {
        return this.getAngle(tf);
    }

    public int getObjectId() {
        if (tp1.objectID != tp2.objectID) System.out.println("错误：同一个轨迹分段的两点具有不同的objectID");
        return tp1.objectID;
    }

    public RoutePainter getRoutePainter() {
        List<GeoPosition> positions = new ArrayList<GeoPosition>();
        positions.add(new GeoPosition(tp1.y, tp1.x));
        positions.add(new GeoPosition(tp2.y, tp2.x));
        RoutePainter routePainter = new RoutePainter(positions);
        return routePainter;
    }

    public double getAngle(TFragment tf) {
        Coordinate c1 = new Coordinate(tp2.x - tp1.x, tp2.y - tp1.y);
        Coordinate c2 = new Coordinate(tf.tp2.x - tf.tp1.x, tf.tp2.y - tf.tp1.y);
        double c1Mod = Math.sqrt(c1.x * c1.x + c1.y * c1.y), c2Mod = Math.sqrt(c2.x * c2.x + c2.y * c2.y);
        double cos = (c1.x * c2.x + c1.y * c2.y) / (c1Mod * c2Mod);
        if (c1Mod <= 0 || c2Mod <= 0) return 0;
        //if (c1Mod <= 0 || c2Mod <= 0) System.out.println("TFragment - ERROR " + c1Mod + " " + c2Mod);
        if (cos < -1.0D) cos = 0.0;
        else if (cos > 1.0D) cos = 1.0;
        double angle = Math.acos(cos);
        return angle;
    }
}
