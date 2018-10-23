package com.lbs.tsod.taxi1.examples;

import com.lbs.tsod.taxi1.algorithm.*;
import com.lbs.tsod.taxi1.file.*;
import com.lbs.tsod.taxi1.model.*;
import com.lbs.tsod.taxi1.osm.JxmapHelper;
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

//检测方向异常
public class NaiveDirection {

    public final static int TOP_K = 26;

    public static void main(String args[]) throws Exception {
        final JXMapViewer mapViewer = new JXMapViewer();
        {
            // Setup JXMapViewer
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            // Set the focus
            mapViewer.setZoom(5);
            mapViewer.setAddressLocation(new GeoPosition(31.2814, 121.5831));

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

        // 输入轨迹
        List<Trajectory> input_trajs = InputDirection.getTaxi();
        System.out.println("共有轨迹数：" + input_trajs.size());
        {
            for (Trajectory traj : input_trajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                painter.setColor(Color.GREEN);
                painter.line_width = 1;
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }
        /*{
            for (Trajectory traj : input_trajs) {
                if (traj.getObjectID() % 10 == 1) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                    painter.setColor(Color.RED);
                    painter.line_width = 2;
                    painters.add(painter);
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            Thread.sleep(10000);
        }*/

        // 简化轨迹
        List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();
        {
            for (Trajectory traj : input_trajs) {
                TrajectorySimplify simplify = new TrajectorySimplify(traj);
                simplified_trajs.add(simplify.getSimplifiedTrajectoty());
            }
        }
        List<TFragment> simplified_fragments = new ArrayList<TFragment>();
        {
            for (Trajectory traj : simplified_trajs) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment fr = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    simplified_fragments.add(fr);
                }
            }
            System.out.println("共有轨迹分段数：" + simplified_fragments.size());
        }
        /*{
            for (Trajectory traj : simplified_trajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                painter.setColor(Color.GREEN);
                painter.line_width = 2;
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }*/

        // 计算轨迹划分密度
        DensityCaculate caculate = new DensityCaculate(simplified_fragments);
        simplified_fragments = caculate.getDensity();

        // 轨迹分段的异常检测
        LAFD lafd = new LAFD(simplified_fragments);
        lafd.TH_DIST = 2E-3;
        List<TFragment> outlier_fragments = lafd.getOutlierFragments();
        /*{
            int cnt = 0;
            for (TFragment fragment : outlier_fragments) {
                if (fragment.ILAF > 1.5) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(fragment);
                    painter.setColor(Color.RED);
                    painter.line_width = 2;
                    painters.add(painter);
                    cnt++;
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            Thread.sleep(2000);
            System.out.println("异常分段数：" + cnt);
        }*/

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

        for (double eaf : avg_eaf) System.out.println(eaf);


        Set<Integer> outlierID = new HashSet<Integer>();
        for (int objectId : traj_eaf.keySet()) {
            if (traj_eaf.get(objectId) / traj_cnt.get(objectId) >= avg_eaf.get(avg_eaf.size() - TOP_K))
                outlierID.add(objectId);
        }
        System.out.println("异常轨迹数目：" + avg_eaf.get(avg_eaf.size() - TOP_K) + " " + outlierID.size());

        {
            for (Trajectory traj : input_trajs) {
                if (outlierID.contains(traj.getObjectID())) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                    painter.setColor(Color.RED);
                    painter.line_width = 2;
                    painters.add(painter);
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }

    }
}
