package com.lbs.tsod.taxi2.osm;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.List;

public class LineStringRTreeIndex implements SpatialTreeIndex<LineString> {

    private SpatialIndex index;
    private int cnt = 0;

    public LineStringRTreeIndex() {
        cnt = 0;
        index = new STRtree();
    }

    public LineString nearest(Coordinate c) {
        return null;
    }

    public List<LineString> radius(Coordinate c, double r) {
        Envelope search = new Envelope(c);
        search.expandBy(r);
        List<LineString> list = index.query(search);
        List<LineString> result = new ArrayList<LineString>();
        for (LineString lineString : list) {
            Coordinate coordinates[] = lineString.getCoordinates();
            for (int i = 1; i < coordinates.length; i++) {
                LineSegment lineSegment = new LineSegment(coordinates[i - 1], coordinates[i]);
                if (lineSegment.distance(c) < r) {
                    result.add(lineString);
                    break;
                }
            }
        }
        return result;
    }

    public List<LineString> knearest(Coordinate c, int k) {
        return null;
    }

    public int getCnt() {
        return cnt;
    }

    public void insert(LineString lineString) {
        Envelope envelope = ((Geometry) lineString).getEnvelopeInternal();
        index.insert(envelope, lineString);
        cnt++;
    }
}
