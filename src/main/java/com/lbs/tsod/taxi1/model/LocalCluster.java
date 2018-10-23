package com.lbs.tsod.taxi1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toyking on 2016/8/10.
 */
public class LocalCluster {
    public List<AllyFragment> lc;

    public LocalCluster() {
        lc = new ArrayList<AllyFragment>();
    }

    public LocalCluster(List<AllyFragment> lc) {
        this.lc = lc;
    }
}