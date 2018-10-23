package com.lbs.tsod.taxi2.algorithm.centerline;

public class TrajSegment {
    public int _id;//car id
    public Point start;
    public Point end;

    public Point start2;
    public Point end2;
    public long time;
    public int n;
    public String mark;
    public int cluID;//cluster id

    public TrajSegment() {
        start = new Point(0, 0);
        end = new Point(0, 0);
        time = 0;
        mark = "unclassified";
        _id = 0;
        cluID = -1;
        n = 0;
    }

    public TrajSegment(Point s, Point e) {
        start = s;
        end = e;
        mark = "unclassified";
        cluID = -1;
    }

    public void rotateAxes(Point s, Point e) {
        start2 = new Point(s.x, s.y);
        end2 = new Point(e.x, e.y);
    }
}
