package com.lbs.tsod.animal.examples;


import com.lbs.tsod.animal.algorithm.*;
import com.lbs.tsod.animal.model.*;
import com.lbs.tsod.animal.file.*;

import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MaoELK1993 extends Applet {

    public final static double TH_EAF = 55;
    public static double TH_DILTA = 85;

    List<Trajectory> input_trajs = new ArrayList<Trajectory>();
    Set<Integer> outlier_id = new HashSet<Integer>();
    List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();

    public void init() {

        try {
            input_trajs = Input.getElk1993();
            System.out.println("共有轨迹数目：" + input_trajs.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 第一步：轨迹划分
        simplified_trajs = new ArrayList<Trajectory>();
        {
            for (Trajectory trajectory : input_trajs) {
                TrajectorySimplify simplify = new TrajectorySimplify(trajectory);
                simplified_trajs.add(simplify.getSimplifiedTrajectoty());
            }
            simplified_trajs = Input.Filter(simplified_trajs);
        }
        List<TFragment> simplified_fragments = new ArrayList<TFragment>();
        {
            for (Trajectory traj : simplified_trajs) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    simplified_fragments.add(tf);
                }
            }
        }

        // 步骤2 轨迹分段的异常检测
        LAFD lafd = new LAFD(simplified_fragments);
        lafd.TH_DIST = 80;
        List<TFragment> outlier_fragments = lafd.getOutlierFragments();

        // 步骤3 进化的轨迹异常
        Map<Integer, Double> object_lafd = new HashMap<Integer, Double>();
        outlier_id = new HashSet<Integer>();
        for (TFragment fragment : outlier_fragments) {
            double EAF = 0D;
            if (object_lafd.containsKey(fragment.getObjectId())) {
                EAF = object_lafd.get(fragment.getObjectId());
            }
            EAF += fragment.ILAF;
            object_lafd.put(fragment.getObjectId(), EAF);
        }

        for (int objectId : object_lafd.keySet()) {
            System.out.println("EAF:" + object_lafd.get(objectId));
            if (object_lafd.get(objectId) > TH_EAF) outlier_id.add(objectId);
        }

        setSize(1200, 900);
        setBackground(Color.white);
        setVisible(true);
    }

    public void drawTFragment(Graphics graphics, TFragment tf, Color color) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        //g.drawLine(5, 4, 100, 50);
        g.drawLine(100 + (int) tf.tp1.x, 100 + (int) tf.tp1.y, 100 + (int) tf.tp2.x, 100 + (int) tf.tp2.y);
        //System.out.println((int) tf.tp1.x + " " + (int) tf.tp1.y);
    }

    public void paint(Graphics g) {
        for (Trajectory traj : input_trajs) {
            for (int i = 1; i < traj.points.size(); i++) {
                TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                drawTFragment(g, tf, Color.GREEN);
            }
        }
        for (Trajectory traj : simplified_trajs) {
            if (outlier_id.contains(traj.getObjectID())) {
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

}
