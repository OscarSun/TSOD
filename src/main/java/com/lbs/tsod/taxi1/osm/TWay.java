package com.lbs.tsod.taxi1.osm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.List;

public class TWay {
    public int id;
    public List<TNode> nodes;

    public TWay() {
        this.id = -1;
        this.nodes = new ArrayList<TNode>();
    }

    public TWay(List<TNode> nodes) {
        this.id = -1;
        this.nodes = nodes;
    }

    public TWay(int id) {
        this.id = id;
        this.nodes = new ArrayList<TNode>();
    }

    public TWay(int id, List<TNode> list) {
        this.id = id;
        this.nodes = list;
    }

    public TNode getDir() {
        double delta_x_sum = 0, delta_y_sum = 0;
        for (int i = 1; i < nodes.size(); i++) {
            LineSegment ls = new LineSegment(nodes.get(i - 1), nodes.get(i));
            delta_x_sum += (ls.p1.x - ls.p0.x) / ls.getLength();
            delta_y_sum += (ls.p1.y - ls.p0.y) / ls.getLength();
        }
        double delta_x_avg = delta_x_sum / (nodes.size() - 1);
        double delta_y_avg = delta_y_sum / (nodes.size() - 1);
        return new TNode(1, new Coordinate(delta_x_avg, delta_y_avg));
    }

    public TNode getAvgNode() {
        double x_sum = 0, y_sum = 0;
        for (TNode node : nodes) {
            x_sum += node.x;
            y_sum += node.y;
        }
        return new TNode(1, new Coordinate(x_sum / nodes.size(), y_sum / nodes.size()));
    }

    public Envelope getEnvelope() {
        Envelope envelope = new Envelope(nodes.get(0));
        for (TNode node : nodes) envelope.expandToInclude(node);
        return envelope;
    }

    public boolean hasSimilarPart(TWay way) {
        double TH_DIR = Math.PI / (180.0 / 25.0);
        double TH_DIS = 1.5E-4;

        for (int i = 1; i < nodes.size(); i++) {
            LineSegment ls = new LineSegment(nodes.get(i - 1), nodes.get(i));
            for (int j = 1; j < way.nodes.size(); j++) {
                LineSegment segment = new LineSegment(way.nodes.get(j - 1), way.nodes.get(j));
                if (ls.distance(segment.p0) > TH_DIS || ls.distance(segment.p1) > TH_DIS) continue;
                LineSegment newLs;
                if (segment.p0.distance(ls.p0) < segment.p0.distance(ls.p1)) newLs = new LineSegment(ls.p0, ls.p1);
                else newLs = new LineSegment(ls.p1, ls.p0);
                if (Math.abs(segment.angle() - newLs.angle()) < TH_DIR) {
                    return true;
                }
            }
        }
        return false;
    }

    public double lenth() {
        double sum = 0.0;
        for (int i = 1; i < nodes.size(); i++) {
            TNode n1 = nodes.get(i - 1), n2 = nodes.get(i);
            sum += n1.distance(n2);
        }
        return sum;
    }

    public double getMinlenth() {
        double sum = 0.0;
        if (nodes.size() > 1) {
            sum = nodes.get(0).distance(nodes.get(nodes.size() - 1));
        }
        return sum;
    }
}
