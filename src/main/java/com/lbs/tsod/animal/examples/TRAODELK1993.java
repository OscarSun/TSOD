package com.lbs.tsod.animal.examples;

import com.lbs.tsod.hurricane.algorithm.*;
import com.lbs.tsod.hurricane.model.*;
import com.lbs.tsod.hurricane.file.*;

import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TRAODELK1993 extends Applet {

    public static double TH_DILTA = 55;
    public final static double D = 900.0D;
    public final static double p = 0.98D;
    public final static double wV = 1.0 / 4.0D;
    public final static double wH = 1.0 / 4.0D;
    public final static double wX = 1.0 / 4.0D;
    public final static double F = 0.10D;

    List<Trajectory> input_trajs = new ArrayList<Trajectory>();
    Set<Integer> outlier_trajectories = new HashSet<Integer>();
    List<Trajectory> simplifiedTrajs = new ArrayList<Trajectory>();

    public void init() {
        try {
            input_trajs = Input.getElk1993();
            System.out.println("共有轨迹数目：" + input_trajs.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 第一步：轨迹划分
        simplifiedTrajs = new ArrayList<Trajectory>();
        {
            for (Trajectory traj : input_trajs) {
                Partition simplify = new Partition(traj);
                simplifiedTrajs.add(simplify.getTrajectoryPartition());
            }
            simplifiedTrajs = Input.Filter(simplifiedTrajs);
        }

        // 第二步：计算轨迹划分密度
        List<TFragment> fragments = new ArrayList<TFragment>();
        {
            for (Trajectory traj : simplifiedTrajs) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    fragments.add(tf);
                }
            }

            List<Double> list_dis = new ArrayList<Double>();
            double sum = 0.0D, dev = 0.0D, cnt = 0.0D;
            for (int i = 0; i < fragments.size(); i++) {
                for (int j = 0; j < fragments.size(); j++) {
                    if (fragments.get(i).getObjectId() == fragments.get(j).getObjectId()) continue;
                    list_dis.add(fragments.get(i).getDistBaseline(fragments.get(j), wV, wH, wX));
                    sum += list_dis.get(list_dis.size() - 1);
                    cnt += 1.0D;
                }
            }
            System.out.println("平均距离是：" + sum / cnt);
            for (int i = 0; i < list_dis.size(); i++) {
                dev += Math.pow((list_dis.get(i) - sum / cnt), 2.0D);
            }

            TH_DILTA = Math.sqrt(dev / list_dis.size());
            System.out.println("标准差是:" + TH_DILTA);

            double sum_denstiy = 0D;
            for (int i = 0; i < fragments.size(); i++) {
                double density = 0D;
                for (int j = 0; j < fragments.size(); j++) {
                    if (fragments.get(i).getObjectId() == fragments.get(j).getObjectId()) continue;
                    if (fragments.get(i).getDistBaseline(fragments.get(j), wV, wH, wX) < TH_DILTA) {
                        density += 1.0D;
                    }
                }
                if (density == 0) {
                    System.out.println("ERROR");
                    fragments.get(i).density = 1.0; //错误
                } else fragments.get(i).density = density;
                sum_denstiy += fragments.get(i).density;
            }
            for (int i = 0; i < fragments.size(); i++) {
                fragments.get(i).adj = sum_denstiy / (fragments.size()) / fragments.get(i).density;
            }
        }

        // 第三步：检测异常轨迹划分
        Map<Integer, List<TFragment>> outlier_fragments = new HashMap<Integer, List<TFragment>>();
        {
            double maxDist = -1;
            for (TFragment fr1 : fragments) {
                Set<Integer> ctr = new HashSet<Integer>();
                for (TFragment fr2 : fragments) {
                    if (fr1.getObjectId() == fr2.getObjectId()) continue;
                    if (fr1.getDistBaseline(fr2, wV, wH, wX) > maxDist) maxDist = fr1.getDistBaseline(fr2, wV, wH, wX);
                    if (fr1.getDistBaseline(fr2, wV, wH, wX) <= D && fr1.getLength() < fr2.getLength()) {
                        ctr.add(fr2.getObjectId());
                    }
                }
                //System.out.println("ctr:" + ctr.size() + " adj:" + fr1.adj + " ctr*adj:" + ctr.size() * fr1.adj + " " + (1.0D - p) * simplifiedTrajs.size());
                if (ctr.size() * fr1.adj <= (1.0D - p) * simplifiedTrajs.size()) {
                    System.out.println("ctr:" + ctr.size() + " adj:" + fr1.adj + " ctr*adj:" + ctr.size() * fr1.adj + " " + (1.0D - p) * simplifiedTrajs.size());
                    if (!outlier_fragments.containsKey(fr1.getObjectId())) {
                        outlier_fragments.put(fr1.getObjectId(), new ArrayList<TFragment>());
                    }
                    outlier_fragments.get(fr1.getObjectId()).add(fr1);
                }
            }
            System.out.println("maxDist:" + maxDist);
        }

        // 第四步：检测异常轨迹
        outlier_trajectories = new HashSet<Integer>();
        for (Trajectory trajectory : simplifiedTrajs) {
            if (outlier_fragments.containsKey(trajectory.getObjectID())) {
                List<TFragment> tmp = outlier_fragments.get(trajectory.getObjectID());
                double ofrc = 0.0D;
                for (TFragment tf : tmp) {
                    ofrc += tf.getLength();
                }
                //System.out.println("ofrc:" + ofrc / trajectory.getLength());
                if (ofrc / trajectory.getLength() >= F) {
                    outlier_trajectories.add(trajectory.getObjectID());
                }
            }
        }
        System.out.println("异常轨迹条数：" + outlier_trajectories.size());


        setSize(1200, 900);
        setBackground(Color.white);
        setVisible(true);
    }


    public void drawTFragment(Graphics graphics, TFragment tf, Color color) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color);
        g.setStroke(new BasicStroke(1));
        //g.drawLine(5, 4, 100, 50);
        g.drawLine(100 + (int) tf.tp1.x, 100 + (int) tf.tp1.y, 100 + (int) tf.tp2.x, 100 + (int) tf.tp2.y);
        //System.out.println((int) tf.tp1.x + " " + (int) tf.tp1.y);
    }

    public void drawTPoint(Graphics graphics, TPoint tp, Color color) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color);
        //g.drawLine(5, 4, 100, 50);

        g.drawLine(100 + (int) tp.x, 100 + (int) tp.y, 100 + (int) tp.x + 10, 100 + (int) tp.y + 10);
        //System.out.println((int) tf.tp1.x + " " + (int) tf.tp1.y);
    }


    public void paint(Graphics g) {
        if (input_trajs.size() <= 0) return;

        for (Trajectory traj : input_trajs) {
            for (int i = 1; i < traj.points.size(); i++) {
                TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                drawTFragment(g, tf, Color.GREEN);
            }
        }
        for (Trajectory traj : input_trajs) {
            if (outlier_trajectories.contains(traj.getObjectID())) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    drawTFragment(g, tf, Color.RED);
                }
            }
        }

        /*for (Trajectory traj : input_trajs) {
            if (outlier_trajectories.contains(traj.getObjectID())) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    drawTFragment(g, tf, Color.RED);
                }
            } else {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    drawTFragment(g, tf, Color.GREEN);
                }
            }
        }*/
    }

    public void update(Graphics g) {

    }
}
