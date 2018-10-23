package com.lbs.tsod.taxi1.osm;

import com.vividsolutions.jts.geom.Coordinate;

public class TNode extends Coordinate {
    public int id;
    public long time;

    public TNode() {
        super();
        this.id = -1;
    }

    public TNode(Coordinate c) {
        super(c);
        this.id = -1;
    }

    public TNode(int id, Coordinate c) {
        super(c);
        this.id = id;
    }


    public TNode(int id, Coordinate c,long time) {
        super(c);
        this.id = id;
        this.time = time;
    }

    public TNode(TNode node) {
        super(node);
        this.id = node.id;
    }

    public double angle(TNode node) {
        double len1 = node.distance(new Coordinate(0, 0));
        double len2 = this.distance(new Coordinate(0, 0));
        double angle = Math.acos((this.x * node.x + this.y * node.y) / (len1 * len2));
        return angle;
    }

    public int compareTo(Object obj) {
        TNode other = (TNode) obj;
        return this.id < other.id ? -1 : (this.id > other.id ? 1 : super.compareTo(other));
    }


}
