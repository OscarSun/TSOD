package com.lbs.tsod.taxi1.osm;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.List;

public interface SpatialTreeIndex<T> {
    public T nearest(Coordinate c);

    public List<T> radius(Coordinate c, double r);

    public List<T> knearest(Coordinate c, int k);

    public void insert(T o);

    public int getCnt();
}
