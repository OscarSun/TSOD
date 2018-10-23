package com.lbs.tsod.hurricane.examples;

import com.lbs.tsod.hurricane.algorithm.*;
import com.lbs.tsod.hurricane.model.*;
import com.lbs.tsod.hurricane.file.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TRAOD {
    public final static double D = 85;
    public static double TH_DILTA = 85;
    public final static double p = 0.95;
    public final static double wV = 1.0/7.0D;
    public final static double wH = 1.0/7.0D;
    public final static double wX = 5.0/7.0D;
    public final static double F = 0.2;

    public static void main(String args[]) throws Exception {
        final JXMapViewer mapViewer = new JXMapViewer();
        {
            // Setup JXMapViewer
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            // Set the focus
            mapViewer.setZoom(14);
            mapViewer.setAddressLocation(new GeoPosition(18.8, -84.0));

            // Add interactions
            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            // Display the viewer in a JFrame
            JFrame frame = new JFrame("TRAOD");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();


        List<Trajectory> input_trajs = Input.getHurricane();
        /*for (Trajectory traj : input_trajs) {
            System.out.print("id:" + traj.getObjectID() + " ");
            for (int i = 0; i < traj.points.size(); i++) {
                System.out.print(traj.points.get(i).objectID  + " ");
            }
            System.out.println();
        }*/
        System.out.println("共有轨迹数目：" + input_trajs.size());

        // 第一步：轨迹划分
        List<Trajectory> simplifiedTrajs = new ArrayList<Trajectory>();
        {
            for (Trajectory traj : input_trajs) {
                Partition simplify = new Partition(traj);
                simplifiedTrajs.add(simplify.getTrajectoryPartition());
            }
        }
        /*{
            for (Trajectory traj : simplifiedTrajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                painter.setColor(Color.GREEN);
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }*/

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
                    //System.out.println("sum:"+sum+" "+list_dis.get(list_dis.size() - 1));
                }
            }
            System.out.println(sum / cnt);
            for (int i = 0; i < list_dis.size(); i++) {
                dev += Math.pow((list_dis.get(i) - sum / cnt), 2.0D);
            }

            TH_DILTA = Math.sqrt(dev / list_dis.size());
            System.out.println("标准差是:" + TH_DILTA );

            double sum_denstiy = 0D;
            for (int i = 0; i < fragments.size(); i++) {
                double density = 0D;
                for (int j = 0; j < fragments.size(); j++) {
                    if (fragments.get(i).getObjectId() == fragments.get(j).getObjectId()) continue;
                    if (fragments.get(i).getDistBaseline(fragments.get(j), wV, wH, wX) < TH_DILTA) {
                        density += 1.0D;
                    }
                }
                if(density==0) System.out.println("ERRoR");
                fragments.get(i).density = density;
                sum_denstiy += fragments.get(i).density;
            }
            for (int i = 0; i < fragments.size(); i++) {
                fragments.get(i).adj = sum_denstiy / (fragments.size()) / fragments.get(i).density;
            }
        }

        // 第三步：检测异常轨迹划分
        Map<Integer, List<TFragment>> outlier_fragments = new HashMap<Integer, List<TFragment>>();
        {
            for (TFragment fr1 : fragments) {
                Set<Integer> ctr = new HashSet<Integer>();
                for (TFragment fr2 : fragments) {
                    if (fr1.getObjectId() == fr2.getObjectId()) continue;
                    if (fr1.getDistBaseline(fr2, wV, wH, wX) <= D && fr2.getLength() > fr1.getLength()) {
                        ctr.add(fr2.getObjectId());
                    }
                }
                if (ctr.size() * fr1.adj <= (1 - p) * simplifiedTrajs.size()) {
                    if (!outlier_fragments.containsKey(fr1.getObjectId())) {
                        outlier_fragments.put(fr1.getObjectId(), new ArrayList<TFragment>());
                    }
                    outlier_fragments.get(fr1.getObjectId()).add(fr1);
                }
            }
        }
        /*{
            for (List<TFragment> list_fr : outlier_fragments.values()) {
                for (TFragment fr : list_fr) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(new LineSegment(fr.tp1, fr.tp2));
                    painter.setColor(Color.BLUE);
                    painters.add(painter);
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }*/

        // 第四步：检测异常轨迹
        Set<Integer> outlier_trajectories = new HashSet<Integer>();
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

        System.out.println("异常轨迹条数："+outlier_trajectories.size());
        {
            for (Trajectory traj : input_trajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                if (outlier_trajectories.contains(traj.getObjectID())) painter.setColor(Color.RED);
                else painter.setColor(Color.GREEN);
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }
    }
}
