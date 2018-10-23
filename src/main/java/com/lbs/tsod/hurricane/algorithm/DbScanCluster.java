package com.lbs.tsod.hurricane.algorithm;

import com.lbs.tsod.hurricane.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by toyking on 2016/8/10.
 */
public class DbScanCluster {
    public double EPS = 2E-4;
    public double MINPTS = 4;

    public List<AllyFragment> list_af;


    public DbScanCluster() {
        list_af = new ArrayList<AllyFragment>();
    }

    public DbScanCluster(List<AllyFragment> list_af) {
        this.list_af = list_af;
        for (int i = 0; i < list_af.size(); i++) list_af.get(i).id = i;
    }

    public List<LocalCluster> getCluster() {
        int color = 1;
        int visited[] = new int[list_af.size() + 10];
        int mark[] = new int[list_af.size() + 10];

        List<LocalCluster> list_lc = new ArrayList<LocalCluster>();
        for (AllyFragment af : list_af) {
            if (visited[af.id] != 0) continue;
            visited[af.id] = 1;
            if (af.af.size() > MINPTS) {//neighbour
                mark[af.id] = color;
                LocalCluster lc = new LocalCluster();
                //bfs
                {
                    Queue<AllyFragment> q = new LinkedList<AllyFragment>();
                    q.add(af);
                    while (q.size() > 0) {
                        AllyFragment s = q.remove();
                        List<AllyFragment> list_af = new ArrayList<AllyFragment>();// <EPS
                        for (AllyFragment d : list_af) {
                            if (visited[d.id] == 0) {
                                visited[d.id] = 1;
                                if (list_af.size() > MINPTS) {//neighbour
                                    mark[d.id] = color;
                                    q.add(d);
                                    lc.lc.add(d);
                                }
                            }
                            if (mark[d.id] == 0) {
                                mark[d.id] = color;
                                lc.lc.add(d);
                            }
                        }
                    }
                }

                list_lc.add(lc);
                color++;
            }
        }
        return list_lc;
    }
}
