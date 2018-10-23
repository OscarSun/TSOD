package com.lbs.tsod.animal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toyking on 2016/8/10.
 */
public class Trajectory {
    public List<TPoint> points;

    public Trajectory() {
        points = new ArrayList<TPoint>();
    }

    public Trajectory(List<TPoint> points) {
        this.points = points;
    }

    public double getPartitionCost(int startIndex, int endIndex) {
        double length = 0D;
        length += Math.log(points.get(startIndex).distance(points.get(endIndex))) / Math.log(2.0D);
        double sumDxita = 0D, sumDcenter = 0D;
        TFragment tfy = new TFragment(points.get(startIndex), points.get(endIndex));
        for (int i = startIndex; i < endIndex; i++) {
            TFragment tfx = new TFragment(points.get(i), points.get(i + 1));
            sumDxita += tfx.getDxita(tfy);
            sumDcenter += tfx.getDcenter(tfy);
        }
        length += Math.log(sumDxita) / Math.log(2.0D);
        length += Math.log(sumDcenter) / Math.log(2.0D);
        return length;
    }

    public double getNoPartitionCost(int startIndex, int endIndex) {
        double length = 0D;
        for (int i = startIndex; i < endIndex; i++) {
            length += Math.log(points.get(i).distance(points.get(i + 1))) / Math.log(2.0D);
        }
        return length;
    }

    public double getBaseLinePartitionCost(int startIndex, int endIndex) {
        double length = 0D;
        length += Math.log(points.get(startIndex).distance(points.get(endIndex))) / Math.log(2.0D);
        double sumDxita = 0D, sumDvertical = 0D;
        TFragment tfy = new TFragment(points.get(startIndex), points.get(endIndex));
        for (int i = startIndex; i < endIndex; i++) {
            TFragment tfx = new TFragment(points.get(i), points.get(i + 1));
            sumDxita += tfx.getDxitaBaseLine(tfy);
            sumDvertical += tfx.getDverticalBaseLine(tfy);
        }
        length += Math.log(sumDxita) / Math.log(2.0D);
        length += Math.log(sumDvertical) / Math.log(2.0D);
        return length;
    }

    public double getBaseLineNoPartitionCost(int startIndex, int endIndex) {
        double length = 0D;
        for (int i = startIndex; i < endIndex; i++) {
            length += Math.log(points.get(i).distance(points.get(i + 1))) / Math.log(2.0D);
        }
        return length;
    }

    public double getLength() {
        double sum = 0.0D;
        for (int i = 1; i < points.size(); i++) {
            sum += points.get(i).distance(points.get(i - 1));
        }
        return sum;
    }

    public int getObjectID(){
        return points.get(0).objectID;
    }
}
