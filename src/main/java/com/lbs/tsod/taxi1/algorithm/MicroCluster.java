package com.lbs.tsod.taxi1.algorithm;

import com.lbs.tsod.taxi1.model.*;
import com.vividsolutions.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by toyking on 2016/8/10.
 */
public class MicroCluster {
    public static double TH_DIR = 20;
    public static double TH_DISTANCE = 5.4E-5;

    public List<TFragment> list_tf;

    public MicroCluster() {
        list_tf = new ArrayList<TFragment>();
    }

    public MicroCluster(List<TFragment> list_tf) {
        this.list_tf = list_tf;
        for (int i = 0; i < list_tf.size(); i++) {
            list_tf.get(i).id = i;
        }
    }

    public List<AllyFragment> getCluster() {
        int color = 1;
        int[] mark = new int[list_tf.size() + 10];

        List<AllyFragment> list_af = new ArrayList<AllyFragment>();
        //clustering
        List<List<LineSegment>> list_clu = new ArrayList<List<LineSegment>>();
        for (int i = 0; i < list_tf.size(); i++) {
            if (mark[i] == 0) {
                mark[i] = color;
                AllyFragment af = new AllyFragment();
                af.af.add(list_tf.get(i));

                //bfs...
                {
                    TFragment tf = list_tf.get(i);
                    Queue<TFragment> queue = new LinkedList<TFragment>();
                    queue.add(tf);
                    while (queue.size() > 0) {
                        TFragment s = queue.remove();
                        for (TFragment d : list_tf) {// neighbour
                            if (s.getDcenter(d) < TH_DISTANCE && Math.abs(s.getDir() - d.getDir()) < TH_DIR) {
                                if (mark[d.id] != 0) continue;
                                mark[d.id] = color;
                                queue.add(d);
                                af.af.add(d);
                            }
                        }
                    }
                }

                list_af.add(af);
                color++;
            }
        }
        return list_af;
    }
}
