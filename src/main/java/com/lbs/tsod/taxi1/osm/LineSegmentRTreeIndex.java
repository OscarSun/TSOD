package com.lbs.tsod.taxi1.osm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.List;


public class LineSegmentRTreeIndex implements SpatialTreeIndex<LineSegment> {

    private SpatialIndex index;
    private int cnt;

    public LineSegmentRTreeIndex() {
        index = new STRtree();
        cnt = 0;
    }

    public LineSegment nearest(Coordinate c) {
        return null;
    }

    public List<LineSegment> radius(Coordinate c, double r) {
        Envelope search = new Envelope(c);
        search.expandBy(r);
        List<LineSegment> list = index.query(search);
        List<LineSegment> result = new ArrayList<LineSegment>();
        for (LineSegment l : list) {
            if (l.distance(c) < r) {
                result.add(l);
            }
        }
        return result;
    }

    public List<LineSegment> radius(LineSegment ls, double r) {
        List<LineSegment> list1 = radius(ls.p0, r);
        List<LineSegment> list2 = radius(ls.p1, r);
        List<LineSegment> result = new ArrayList<LineSegment>();
        for (LineSegment l : list1) {
            if (l.distance(ls.p0) < r && l.distance(ls.p1) < r) {
                result.add(l);
            }
        }
        for (LineSegment l : list2) {
            if (l.distance(ls.p0) < r && l.distance(ls.p1) < r) {
                result.add(l);
            }
        }
        return result;
    }

    public List<LineSegment> knearest(Coordinate c, int k) {
        return null;
    }

    public void insert(LineSegment o) {
        Envelope envelope = new Envelope();
        envelope.init(o.p0.x, o.p1.x, o.p0.y, o.p1.y);
        index.insert(envelope, o);
        cnt++;
    }

    public int getCnt() {
        return cnt;
    }
}
