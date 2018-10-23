package com.lbs.tsod.taxi2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toyking on 2016/8/10.
 */
public class AllyFragment {
    public int id;
    public List<TFragment> af;

    public AllyFragment() {
        af = new ArrayList<TFragment>();
    }

    public AllyFragment(List<TFragment> af) {
        this.af = af;
    }

    public double getSpeed() {
        if (af.size() <= 0) return 0;
        double sum = 0D;
        for (TFragment fragment : af) {
            sum += fragment.getSpeed();
        }
        return sum / (af.size() * 1.0);
    }
}
