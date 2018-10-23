package com.lbs.tsod.taxi2.algorithm;

import com.lbs.tsod.taxi2.osm.*;

import java.util.ArrayList;
import java.util.List;

public class MapMatching {
    public List<TNode> trajs;
    public List<TWay> TWays;
    public double TH_DIS;

    public MapMatching() {
        trajs = new ArrayList<TNode>();
        TWays = new ArrayList<TWay>();
        TH_DIS = 2E-4D; //about 22m
    }

    public MapMatching(List<TNode> trajs, List<TWay> TWays) {
        this.trajs = trajs;
        this.TWays = TWays;
        TH_DIS = 2E-4D; //about 22m
    }

    public MapMatching(List<TNode> trajs, List<TWay> TWays, double TH_DIS) {
        this.trajs = trajs;
        this.TWays = TWays;
        this.TH_DIS = TH_DIS;
    }

    public List<TNode> getMapMatchedTrajs() {
        List<TNode> result = new ArrayList<TNode>();
        TWayRTreeIndex index = new TWayRTreeIndex();
        for (TWay TWay : TWays) index.insert(TWay);
        for (TNode node : trajs) {
            if (index.radius(node, TH_DIS).size() > 0) {
                result.add(node);
            }
        }
        return result;
    }

    public List<TNode> getUnmatchedTrajs() {
        List<TNode> unmatched = new ArrayList<TNode>();
        TWayRTreeIndex index = new TWayRTreeIndex();
        for (TWay TWay : TWays) index.insert(TWay);
        for (TNode node : trajs) {
            if (index.radius(node, TH_DIS).size() == 0) {
                unmatched.add(node);
            }
        }
        return unmatched;
    }

    public List<TNodeSequence> getUnmatchedTrajsSequence() {
        List<TNodeSequence> unmatched = new ArrayList<TNodeSequence>();
        TWayRTreeIndex index = new TWayRTreeIndex();
        for (TWay TWay : TWays) index.insert(TWay);
        for (int i = 0; i < trajs.size(); i++) {
            TNode node = trajs.get(i);
            if (index.radius(node, TH_DIS).size() == 0) {
                unmatched.add(new TNodeSequence(node, i));
            }
        }
        return unmatched;
    }
}
