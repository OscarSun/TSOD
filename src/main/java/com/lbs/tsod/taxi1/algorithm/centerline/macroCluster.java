package com.lbs.tsod.taxi1.algorithm.centerline;

import java.util.ArrayList;
import java.util.List;

public class macroCluster {
    public List<Integer> microCluster;
    public List<Point> repreTraj;
    public List<Point> repreTraj2;

    public macroCluster() {
        microCluster = new ArrayList<Integer>();
        repreTraj = new ArrayList<Point>();
        repreTraj2 = new ArrayList<Point>();
    }

    public void insertPoint(Point p) {
        repreTraj.add(p);
    }

    public void insertPoint2(Point p) {
        repreTraj2.add(p);
    }
}
