package com.lbs.tsod.taxi2.osm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.List;

public class TNodeRTreeIndex implements SpatialTreeIndex<TNode> {
    private SpatialIndex index;
    private int cnt = 0;

    public TNodeRTreeIndex() {
        index = new STRtree();
    }

    public TNode nearest(Coordinate c) {
        return null;
    }

    public List<TNode> radius(Coordinate c, double r) {
        Envelope search = new Envelope(c);
        search.expandBy(r);
        List<TNode> list = index.query(search);
        return list;
    }

    public List<TNode> knearest(Coordinate c, int k) {
        return null;
    }

    public void insert(TNode o) {
        Envelope envelope = new Envelope(o);
        index.insert(envelope, o);
        cnt++;
    }

    public int getCnt() {
        return cnt;
    }
}
