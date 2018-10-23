package com.lbs.tsod.animal.examples;

import com.lbs.tsod.animal.algorithm.*;
import com.lbs.tsod.animal.file.*;
import com.lbs.tsod.animal.model.*;

import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.util.List;

//检测动物数据ELK
public class NaiveMaoELK1993 extends Applet {

    public final static int TOP_K = 2;

    List<Trajectory> input_trajs = new ArrayList<Trajectory>();
    List<TFragment> simplified_fragments = new ArrayList<TFragment>();
    Set<Integer> outlier_id = new HashSet<Integer>();
    List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();

    public void init() {
        // 1. 输入轨迹
        try {
            input_trajs = Input.getElk1993();
            System.out.println("共有轨迹数目：" + input_trajs.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. 简化轨迹
        {
            for (Trajectory traj : input_trajs) {
                TrajectorySimplify simplify = new TrajectorySimplify(traj);
                simplified_trajs.add(simplify.getSimplifiedTrajectoty());
            }
        }
        simplified_trajs = Input.Filter(simplified_trajs);
        {
            for (Trajectory traj : simplified_trajs) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment fr = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    simplified_fragments.add(fr);
                }
            }
            System.out.println("共有轨迹分段数：" + simplified_fragments.size());
        }

        // 计算轨迹划分密度
        DensityCaculate caculate = new DensityCaculate(simplified_fragments);
        simplified_fragments = caculate.getDensity();

        // 轨迹分段的异常检测
        LAFD lafd = new LAFD(simplified_fragments);
        lafd.TH_DIST = 80;
        List<TFragment> outlier_fragments = lafd.getOutlierFragments();

        //进化的轨迹异常
        Map<Integer, Double> traj_eaf = new HashMap<Integer, Double>();
        Map<Integer, Double> traj_cnt = new HashMap<Integer, Double>();
        for (TFragment fragment : outlier_fragments) {
            double EAF = 0D;
            if (traj_eaf.containsKey(fragment.getObjectId())) EAF = traj_eaf.get(fragment.getObjectId());
            EAF += fragment.ILAF;
            traj_eaf.put(fragment.getObjectId(), EAF);

            double cnt = 0D;
            if (traj_cnt.containsKey(fragment.getObjectId())) cnt = traj_cnt.get(fragment.getObjectId());
            cnt += 1.0D;
            traj_cnt.put(fragment.getObjectId(), cnt);
        }
        List<Double> avg_eaf = new ArrayList<Double>();
        for (int id : traj_eaf.keySet()) {
            avg_eaf.add(traj_eaf.get(id) / traj_cnt.get(id));
        }
        Collections.sort(avg_eaf);

        for (double eaf : avg_eaf) System.out.println("AVG_EAF:" + eaf);


        for (int objectId : traj_eaf.keySet()) {
            if (traj_eaf.get(objectId) / traj_cnt.get(objectId) >= avg_eaf.get(avg_eaf.size() - TOP_K))
                outlier_id.add(objectId);
        }
        System.out.println("异常轨迹数目：" + outlier_id.size() + " " + avg_eaf.get(avg_eaf.size() - TOP_K));

        setSize(1200, 900);
        setBackground(Color.white);
        setVisible(true);
    }

    public void drawTFragment(Graphics graphics, TFragment tf, Color color) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color);
        g.setStroke(new BasicStroke(1));
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
        for (Trajectory traj : input_trajs) {
            if (outlier_id.contains(traj.getObjectID())) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    drawTFragment(g, tf, Color.RED);
                }
            }
        }
    }
}
