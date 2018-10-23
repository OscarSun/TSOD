package com.lbs.tsod.hurricane.examples;

import com.lbs.tsod.hurricane.algorithm.TrajectorySimplify;
import com.lbs.tsod.hurricane.file.Input;
import com.lbs.tsod.hurricane.model.TFragment;
import com.lbs.tsod.hurricane.model.Trajectory;

import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TRAODFrame extends Applet {
    public final static double D = 85;
    public final static double p = 0.97;
    public final static double wV = 1.0 / 7.0D;
    public final static double wH = 1.0 / 7.0D;
    public final static double wX = 5.0 / 7.0D;
    public final static double F = 0.2;

    //0. 输入的轨迹
    List<Trajectory> input_trajs = new ArrayList<Trajectory>();

    // 第一步：轨迹划分
    List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();
    List<TFragment> simplified_fragments = new ArrayList<TFragment>();

    // 第三步：检测异常轨迹划分
    Map<Integer, List<TFragment>> outlier_fragments = new HashMap<Integer, List<TFragment>>();

    // 第四步：检测异常轨迹
    Set<Integer> outlier_trajectories = new HashSet<Integer>();

    public void init() {
        try {
            input_trajs = Input.getHurricane();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 第一步：轨迹划分
        simplified_trajs = new ArrayList<Trajectory>();
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
            System.out.println("共有简化后的轨迹分段数：" + simplified_fragments.size());
        }

        // 第二步：计算轨迹划分密度
        {
            double TH_DILTA;

            List<Double> list_dis = new ArrayList<Double>();
            double sum = 0.0D, dev = 0.0D, cnt = 0.0D;
            for (int i = 0; i < simplified_fragments.size(); i++) {
                for (int j = 0; j < simplified_fragments.size(); j++) {
                    if (simplified_fragments.get(i).getObjectId() == simplified_fragments.get(j).getObjectId())
                        continue;
                    list_dis.add(simplified_fragments.get(i).getDistBaseline(simplified_fragments.get(j), wV, wH, wX));
                    sum += list_dis.get(list_dis.size() - 1);
                    cnt += 1.0D;
                    //System.out.println("sum:"+sum+" "+list_dis.get(list_dis.size() - 1));
                }
            }
            System.out.println("平均距离：" + sum / cnt);
            for (int i = 0; i < list_dis.size(); i++) {
                dev += Math.pow((list_dis.get(i) - sum / cnt), 2.0D);
            }

            TH_DILTA = Math.sqrt(dev / list_dis.size());
            System.out.println("标准差是：" + TH_DILTA);

            double sum_denstiy = 0D;
            for (int i = 0; i < simplified_fragments.size(); i++) {
                double density = 0D;
                for (int j = 0; j < simplified_fragments.size(); j++) {
                    if (simplified_fragments.get(i).getObjectId() == simplified_fragments.get(j).getObjectId())
                        continue;
                    if (simplified_fragments.get(i).getDistBaseline(simplified_fragments.get(j), wV, wH, wX) < TH_DILTA) {
                        density += 1.0D;
                    }
                }
                if (density == 0) System.out.println("ERRoR");
                simplified_fragments.get(i).density = density;
                sum_denstiy += simplified_fragments.get(i).density;
            }
            for (int i = 0; i < simplified_fragments.size(); i++) {
                simplified_fragments.get(i).adj = sum_denstiy / (simplified_fragments.size()) / simplified_fragments.get(i).density;
            }
        }

        // 第三步：检测异常轨迹划分
        outlier_fragments = new HashMap<Integer, List<TFragment>>();
        {
            for (TFragment fr1 : simplified_fragments) {
                Set<Integer> ctr = new HashSet<Integer>();
                for (TFragment fr2 : simplified_fragments) {
                    if (fr1.getObjectId() == fr2.getObjectId()) continue;
                    if (fr1.getDistBaseline(fr2, wV, wH, wX) <= D && fr2.getLength() > fr1.getLength()) {
                        ctr.add(fr2.getObjectId());
                    }
                }
                if (ctr.size() * fr1.adj <= (1 - p) * simplified_trajs.size()) {
                    if (!outlier_fragments.containsKey(fr1.getObjectId())) {
                        outlier_fragments.put(fr1.getObjectId(), new ArrayList<TFragment>());
                    }
                    outlier_fragments.get(fr1.getObjectId()).add(fr1);
                }
            }
        }

        // 第四步：检测异常轨迹
        outlier_trajectories = new HashSet<Integer>();
        for (Trajectory trajectory : simplified_trajs) {
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
        System.out.println("异常轨迹数：" + outlier_trajectories.size());

        {
            setSize(1200, 900);
            setBackground(Color.white);
            setVisible(true);
        }
    }

    public int ConvertX(double x) {
        return 1100 + (int) (x * 10.0);
    }

    public int ConvertY(double y) {
        return 800 + (int) (-y * 10.0);
    }

    public void drawTFragment(Graphics graphics, TFragment tf, Color color) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color);
        g.setStroke(new BasicStroke(1));
        g.drawLine(ConvertX(tf.tp1.x), ConvertY(tf.tp1.y), ConvertX(tf.tp2.x), ConvertY(tf.tp2.y));
    }

    public void drawTFragment2(Graphics graphics, TFragment tf, Color color) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color);
        g.setStroke(new BasicStroke(3));
        g.drawLine(ConvertX(tf.tp1.x), ConvertY(tf.tp1.y), ConvertX(tf.tp2.x), ConvertY(tf.tp2.y));
    }


    /*显示简化后的轨迹
    public void paint(Graphics g) {

        for (Trajectory traj : simplified_trajs) {
            for (int i = 1; i < traj.points.size(); i++) {
                TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                drawTFragment(g, tf, Color.GREEN);
            }
        }
        for (Trajectory traj : simplified_trajs) {
            if (outlier_trajectories.contains(traj.getObjectID())) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    drawTFragment(g, tf, Color.RED);
                }
            }
        }
        for (int id : outlier_fragments.keySet()) {
            if (outlier_trajectories.contains(id)) {
                for (TFragment fragment : outlier_fragments.get(id)) {
                    drawTFragment2(g, fragment, Color.RED);
                }
            }
        }

    }*/

    /*显示原来的轨迹*/
    public void paint(Graphics g) {
        for (Trajectory traj : input_trajs) {
            for (int i = 1; i < traj.points.size(); i++) {
                TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                drawTFragment(g, tf, Color.GREEN);
            }
        }

        /*显示简化后的效果*/
        /*for (Trajectory traj : simplified_trajs) {
            for (int i = 1; i < traj.points.size(); i++) {
                TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                drawTFragment(g, tf, Color.black);
            }
        }*/

        for (Trajectory traj : input_trajs) {
            if (outlier_trajectories.contains(traj.getObjectID())) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    drawTFragment(g, tf, Color.RED);
                }
            }
        }

        for (int id : outlier_fragments.keySet()) {
            if (outlier_trajectories.contains(id)) {
                for (TFragment fragment : outlier_fragments.get(id)) {
                    for (Trajectory traj : input_trajs) {
                        for (int i = 0; i < traj.points.size(); i++) {
                            if (traj.points.get(i).x == fragment.tp1.x && traj.points.get(i).y == fragment.tp1.y) {
                                for (int j = i + 1; j < traj.points.size(); j++) {
                                    if (traj.points.get(j).x == fragment.tp2.x && traj.points.get(j).y == fragment.tp2.y) {
                                        for (int k = i + 1; k <= j; k++) {
                                            TFragment tf = new TFragment(traj.points.get(k - 1), traj.points.get(k));
                                            drawTFragment2(g, tf, Color.RED);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }

}
