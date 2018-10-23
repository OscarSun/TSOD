package com.lbs.tsod.hurricane.examples;

import com.lbs.tsod.hurricane.algorithm.DensityCaculate;
import com.lbs.tsod.hurricane.algorithm.LAFD;
import com.lbs.tsod.hurricane.algorithm.TrajectorySimplify;
import com.lbs.tsod.hurricane.file.Input;
import com.lbs.tsod.hurricane.model.TFragment;
import com.lbs.tsod.hurricane.model.Trajectory;

import java.applet.Applet;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.*;
import java.util.List;

/**
 * Created by toyking on 2016/9/25.
 */
public class NaiveMaoHurricaneFrame extends Applet {

    public final static int TOP_K = 6;

    //public static double TH_DILTA = 85;
    //public final static double p = 0.95;

    // 1. 输入轨迹
    List<Trajectory> input_trajs = new ArrayList<Trajectory>();

    // 2. 简化轨迹
    List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();
    List<TFragment> simplified_fragments = new ArrayList<TFragment>();

    // 4. 轨迹分段的异常检测
    List<TFragment> outlier_fragments = new ArrayList<TFragment>();

    // 5. 进化的轨迹异常
    Map<Integer, Double> traj_eaf = new HashMap<Integer, Double>();
    Map<Integer, Double> traj_cnt = new HashMap<Integer, Double>();

    //top-k
    Set<Integer> outlier_id = new HashSet<Integer>();

    public void init() {
        // 1. 输入轨迹
        try {
            input_trajs = Input.getHurricane();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. 简化轨迹
        simplified_trajs = new ArrayList<Trajectory>();
        simplified_fragments = new ArrayList<TFragment>();
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

        // 3. 计算轨迹划分密度
        DensityCaculate caculate = new DensityCaculate(simplified_fragments);
        simplified_fragments = caculate.getDensity();

        // 4. 轨迹分段的异常检测
        LAFD lafd = new LAFD(simplified_fragments);
        lafd.TH_DIST = 40;
        outlier_fragments = lafd.getOutlierFragments();

        // 5. 进化的轨迹异常
        traj_eaf = new HashMap<Integer, Double>();
        traj_cnt = new HashMap<Integer, Double>();
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


        // top-k
        outlier_id = new HashSet<Integer>();
        for (int objectId : traj_eaf.keySet()) {
            if (traj_eaf.get(objectId) / traj_cnt.get(objectId) >= avg_eaf.get(avg_eaf.size() - TOP_K))
                outlier_id.add(objectId);
        }
        System.out.println("异常轨迹数目：" + outlier_id.size() + " " + avg_eaf.get(avg_eaf.size() - TOP_K));

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


    public void drawTFragmentWithDir(Graphics graphics, TFragment tf, Color color) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color);
        //g.setStroke(new BasicStroke(3));
        drawAL(ConvertX(tf.tp1.x), ConvertY(tf.tp1.y), ConvertX(tf.tp2.x), ConvertY(tf.tp2.y), g);
    }

    public static void drawAL(int sx, int sy, int ex, int ey, Graphics2D g2) {
        double H = 15; // 箭头高度
        double L = 5; // 底边的一半
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点
        double y_3 = ey - arrXY_1[1];
        double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点
        double y_4 = ey - arrXY_2[1];

        Double X3 = new Double(x_3);
        x3 = X3.intValue();
        Double Y3 = new Double(y_3);
        y3 = Y3.intValue();
        Double X4 = new Double(x_4);
        x4 = X4.intValue();
        Double Y4 = new Double(y_4);
        y4 = Y4.intValue();
        // 画线
        g2.drawLine(sx, sy, ex, ey);
        //
        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(ex, ey);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.closePath();
        //实心箭头
        g2.fill(triangle);
        //非实心箭头
        //g2.draw(triangle);

    }

    // 计算
    public static double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen) {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
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
            if (outlier_id.contains(traj.getObjectID())) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    drawTFragment(g, tf, Color.RED);
                }
            }
        }
        for (TFragment fragment : outlier_fragments) {
            if (fragment.ILAF > 1.5 && outlier_id.contains(fragment.getObjectId())) {
                drawTFragment2(g, fragment, Color.RED);
            }
        }
    }*/

    /*显示原来的轨迹*/
    public void paint(Graphics g) {
        for (Trajectory traj : input_trajs) {
            for (int i = 1; i < traj.points.size(); i++) {
                TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                if (i == traj.points.size() - 1) drawTFragmentWithDir(g, tf, Color.GREEN);
                else drawTFragment(g, tf, Color.GREEN);
                //drawTFragment(g, tf, Color.GREEN);
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
            if (outlier_id.contains(traj.getObjectID())) {
                if(traj.getObjectID()!=1999016) continue;
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    if (i == traj.points.size() - 1) drawTFragmentWithDir(g, tf, Color.RED);
                    else drawTFragment(g, tf, Color.RED);
                }
            }
        }

        for (TFragment fragment : outlier_fragments) {
            if (fragment.ILAF > 1.5 && outlier_id.contains(fragment.getObjectId())) {
                if(fragment.getObjectId()!=1999016) continue;
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
