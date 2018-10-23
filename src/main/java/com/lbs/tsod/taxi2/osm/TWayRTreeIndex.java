package com.lbs.tsod.taxi2.osm;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.ArrayList;
import java.util.List;

public class TWayRTreeIndex implements SpatialTreeIndex<TWay> {

    private SpatialIndex index;
    private int cnt = 0;
    private GeometryFactory geometryFactory;

    public TWayRTreeIndex() {
        cnt = 0;
        index = new STRtree();
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    }

    public TWay nearest(Coordinate c) {
        return null;
    }

    public List<TWay> radius(Coordinate c, double r) {
        Envelope search = new Envelope(c);
        search.expandBy(r);
        List<TWay> TWays = index.query(search);
        List<TWay> result = new ArrayList<TWay>();
        for (com.lbs.tsod.taxi2.osm.TWay TWay : TWays) {
            for (int i = 1; i < TWay.nodes.size(); i++) {
                LineSegment lineSegment = new LineSegment(TWay.nodes.get(i - 1), TWay.nodes.get(i));
                if (lineSegment.distance(c) < r) {
                    result.add(TWay);
                    break;
                }
            }
        }
        return result;
    }

    public List<TWay> knearest(Coordinate c, int k) {
        return null;
    }

    public void insert(TWay o) {
        Coordinate[] coordinates = new Coordinate[o.nodes.size()];
        o.nodes.toArray(coordinates);
        LineString lineString = geometryFactory.createLineString(coordinates);
        Envelope envelope = ((Geometry) lineString).getEnvelopeInternal();
        index.insert(envelope, o);
        cnt++;
    }

    public int getCnt() {
        return cnt;
    }
}
