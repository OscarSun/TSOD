package com.lbs.tsod.hurricane.algorithm;

import com.lbs.tsod.hurricane.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toyking on 2016/8/10.
 */
public class LAFD {
    public static double TH_DIST = 1E-2;

    public List<TFragment> fragments;

    public LAFD() {
        fragments = new ArrayList<TFragment>();
    }

    public LAFD(List<TFragment> fragments) {
        this.fragments = fragments;
    }

    public List<TFragment> getFragmentsLdd() {
        double maxDist = 0;
        for (int i = 0; i < fragments.size(); i++) {
            double nd = 0D, diff = 0D;
            for (int j = 0; j < fragments.size(); j++) {
                if (fragments.get(i).getObjectId() == fragments.get(j).getObjectId()) continue;
                double dist = fragments.get(i).getDist(fragments.get(j));
                if (dist < TH_DIST) {
                    diff += fragments.get(i).getHurricaneDiff(fragments.get(j));
                    nd += 1.0;
                }
                if (dist > maxDist) maxDist = dist;
            }
            //System.out.println("nd:" + nd + " diff:" + diff);
            if (diff > 0) fragments.get(i).ldd = nd / diff;
            else fragments.get(i).ldd = 0;
        }
        System.out.println("轨迹分段最远距离:" + maxDist);
        return fragments;
    }

    public List<TFragment> getFragmentsILAF() {
        double MAX_ILAF = -1;
        for (int i = 0; i < fragments.size(); i++) {
            double nd = 0D, diff = 0D;
            for (int j = 0; j < fragments.size(); j++) {
                if (fragments.get(i).getObjectId() == fragments.get(j).getObjectId()) continue;
                double dist = fragments.get(i).getDist(fragments.get(j));
                if (dist < TH_DIST) {
                    if (fragments.get(i).ldd <= 0) {
                        nd = 0;
                        break;
                    }
                    diff += fragments.get(j).ldd / fragments.get(i).ldd;
                    nd += 1.0;
                }
            }
            if (nd > 0.0) fragments.get(i).ILAF = diff / nd;
            else {
                fragments.get(i).ILAF = 0;
                System.out.println("LAFD-存在周围轨迹点数目为0的情况！");
            }
            if (fragments.get(i).ILAF > MAX_ILAF) MAX_ILAF = fragments.get(i).ILAF;
            //System.out.println("Nd:" + nd + " ILAF:" + fragments.get(i).ILAF);
        }
        System.out.println("MAX_ILAF:" + MAX_ILAF);
        return fragments;
    }

    public List<TFragment> getOutlierFragments() {
        getFragmentsLdd();
        getFragmentsILAF();
        return fragments;
    }
}
