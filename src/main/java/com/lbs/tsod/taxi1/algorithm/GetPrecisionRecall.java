package com.lbs.tsod.taxi1.algorithm;

import com.lbs.tsod.taxi1.model.*;
import com.lbs.tsod.taxi1.osm.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import java.util.List;

/**
 * Created by toyking on 2016/10/1.
 */
public class GetPrecisionRecall {
    public final static double SPLIT_LENGTH = 2E-4;
    public final static double TH_DIR = 1E-7;
    public final static double TH_DIS = 1E-7;


    //true negative / all inferred
    public static double getPrecision(List<Trajectory> ways_deleted, List<Trajectory> ways_inferred) throws Exception {
        // build index on ways_deleted
        LineSegmentRTreeIndex index = new LineSegmentRTreeIndex();
        for (Trajectory TWay : ways_deleted) {
            List<TPoint> nodes = TWay.points;
            for (int i = 1; i < nodes.size(); i++) {
                LineSegment lineSegment = new LineSegment(nodes.get(i - 1), nodes.get(i));
                index.insert(lineSegment);
            }
        }

        // sum(for each ground truth is true) / ground truth
        double tp = 0.0D, sum = 0.0D;
        for (Trajectory TWay : ways_inferred) {
            List<TPoint> nodes = TWay.points;
            for (int i = 1; i < nodes.size(); i++) {
                TPoint n0 = nodes.get(i - 1), n1 = nodes.get(i);
                sum += n0.distance(n1);
                LineSegment ls = new LineSegment(n0, n1);
                List<LineSegment> segments = index.radius(ls, TH_DIS);
                for (LineSegment segment : segments) {
                    LineSegment newLs;
                    if (segment.p0.distance(n0) < segment.p0.distance(n1)) {
                        newLs = new LineSegment(n0, n1);
                    } else {
                        newLs = new LineSegment(n1, n0);
                    }
                    if (Math.abs(segment.angle() - newLs.angle()) < TH_DIR) {
                        tp += n0.distance(n1);
                        break;
                    }
                }

            }
        }
        return tp / sum;
    }

    //true negative / ground truth
    public static double getRecall(List<Trajectory> ways_deleted, List<Trajectory> ways_inferred) throws Exception {
        // build index on ways_inferred
        LineSegmentRTreeIndex index = new LineSegmentRTreeIndex();
        for (Trajectory TWay : ways_inferred) {
            List<TPoint> nodes = TWay.points;
            for (int i = 1; i < nodes.size(); i++) {
                LineSegment lineSegment = new LineSegment(nodes.get(i - 1), nodes.get(i));
                index.insert(lineSegment);
            }
        }

        // sum(for each ground truth is true) / ground truth
        double tp = 0.0D, ground_truth = 0.0D;
        for (Trajectory TWay : ways_deleted) {
            List<TPoint> nodes = TWay.points;
            for (int i = 1; i < nodes.size(); i++) {
                TPoint n1 = nodes.get(i - 1), n2 = nodes.get(i);
                ground_truth += n1.distance(n2);
                int k = (int) (n1.distance(n2) / SPLIT_LENGTH) + 1;
                for (int t = 0; t < k; t++) {
                    Coordinate c0 = new Coordinate(n1.x + (n2.x - n1.x) / k * t, n1.y + (n2.y - n1.y) / k * t);
                    Coordinate c1 = new Coordinate(n1.x + (n2.x - n1.x) / k * (t + 1), n1.y + (n2.y - n1.y) / k * (t + 1));
                    LineSegment ls = new LineSegment(c0, c1);
                    List<LineSegment> segments = index.radius(ls, TH_DIS);
                    for (LineSegment segment : segments) {
                        LineSegment newLs;
                        if (segment.p0.distance(c0) < segment.p0.distance(c1)) {
                            newLs = new LineSegment(c0, c1);
                        } else {
                            newLs = new LineSegment(c1, c0);
                        }
                        if (Math.abs(segment.angle() - newLs.angle()) < TH_DIR) {
                            tp += c0.distance(c1);
                            break;
                        }
                    }
                }
            }
        }
        return tp / ground_truth;
    }
}
