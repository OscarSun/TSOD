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

/**
 * Created by toyking on 2016/9/6.
 */
public class Mao {

    public final static double TH_EAF = 40;

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
            JFrame frame = new JFrame("Mao");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        List<Trajectory> input_trajs = Input.getHurricane();

        // 第一步：轨迹划分
        List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();
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
        lafd.TH_DIST = 7.0D;
        List<TFragment> outlier_fragments = lafd.getOutlierFragments();

        /*// 第二步：计算轨迹划分密度
        {
            List<Double> list_dis = new ArrayList<Double>();
            double sum = 0.0D, dev = 0.0D, cnt = 0.0D;
            for (int i = 0; i < simplified_fragments.size(); i++) {
                for (int j = 0; j < simplified_fragments.size(); j++) {
                    if (simplified_fragments.get(i).getObjectId() == simplified_fragments.get(j).getObjectId()) continue;
                    list_dis.add(simplified_fragments.get(i).getDist(simplified_fragments.get(j)));
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
            for (int i = 0; i < simplified_fragments.size(); i++) {
                double density = 0D;
                for (int j = 0; j < simplified_fragments.size(); j++) {
                    if (simplified_fragments.get(i).getObjectId() == simplified_fragments.get(j).getObjectId()) continue;
                    if (simplified_fragments.get(i).getDist(simplified_fragments.get(j)) < TH_DILTA) {
                        density += 1.0D;
                    }
                }
                simplified_fragments.get(i).density = density;
                sum_denstiy += density;
            }
            for (int i = 0; i < simplified_fragments.size(); i++) {
                simplified_fragments.get(i).adj = sum_denstiy / (simplified_fragments.size()) / simplified_fragments.get(i).density;
            }
        }*/

        // 步骤3 进化的轨迹异常
        Map<Integer, Double> object_lafd = new HashMap<Integer, Double>();
        Set<Integer> outlier_id = new HashSet<Integer>();
        for (TFragment fragment : outlier_fragments) {
            double EAF = 0D;
            if (object_lafd.containsKey(fragment.getObjectId())) {
                EAF = object_lafd.get(fragment.getObjectId());
            }
            EAF += fragment.ILAF;
            object_lafd.put(fragment.getObjectId(), EAF);
        }

        for (int objectId : object_lafd.keySet()) {
            System.out.println(object_lafd.get(objectId));
            if (object_lafd.get(objectId) > TH_EAF) outlier_id.add(objectId);
        }

        System.out.println("异常轨迹条数："+outlier_id.size());
        {
            for (Trajectory traj : input_trajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                if (outlier_id.contains(traj.getObjectID())) painter.setColor(Color.RED);
                else painter.setColor(Color.GREEN);
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }
    }
}
