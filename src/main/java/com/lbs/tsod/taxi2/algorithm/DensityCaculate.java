package com.lbs.tsod.taxi2.algorithm;

import com.lbs.tsod.taxi2.model.TFragment;

import java.util.ArrayList;
import java.util.List;

public class DensityCaculate {
    public List<TFragment> fragments;

    public DensityCaculate() {
        fragments = new ArrayList<TFragment>();
    }

    public DensityCaculate(List<TFragment> fragments) {
        this.fragments = fragments;
    }

    public List<TFragment> getDensity() {
        List<Double> list_dis = new ArrayList<Double>();
        double sum = 0.0D, dev = 0.0D, cnt = 0.0D;
        for (int i = 0; i < fragments.size(); i++) {
            for (int j = 0; j < fragments.size(); j++) {
                if (fragments.get(i).getObjectId() == fragments.get(j).getObjectId()) continue;
                list_dis.add(fragments.get(i).getDist(fragments.get(j)));
                sum += list_dis.get(list_dis.size() - 1);
                cnt += 1.0D;
            }
        }
        System.out.println("平均距离是：" + sum / cnt);
        for (int i = 0; i < list_dis.size(); i++) {
            dev += Math.pow((list_dis.get(i) - sum / cnt), 2.0D);
        }

        double TH_DILTA = Math.sqrt(dev / list_dis.size());
        System.out.println("标准差是:" + TH_DILTA);

        double sum_denstiy = 0D;
        for (int i = 0; i < fragments.size(); i++) {
            double density = 0D;
            for (int j = 0; j < fragments.size(); j++) {
                if (fragments.get(i).getObjectId() == fragments.get(j).getObjectId()) continue;
                if (fragments.get(i).getDist(fragments.get(j)) < TH_DILTA) {
                    density += 1.0D;
                }
            }
            if (density == 0) {
                //System.out.println("DENSITY ERROR");
                fragments.get(i).density = 1.0; //错误
            } else fragments.get(i).density = density;
            sum_denstiy += fragments.get(i).density;
    }
        for (int i = 0; i < fragments.size(); i++) {
            fragments.get(i).adj = sum_denstiy / (fragments.size()) / fragments.get(i).density;
        }
        return fragments;
    }
}
