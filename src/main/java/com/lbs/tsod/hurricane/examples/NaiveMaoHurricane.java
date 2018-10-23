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
 * Created by toyking on 2016/9/25.
 */
public class NaiveMaoHurricane {

    public final static int TOP_K = 9;

    public static double TH_DILTA = 85;
    public final static double p = 0.95;

    public static void main(String args[]) throws Exception {
        final JXMapViewer mapViewer = new JXMapViewer();
        {
            // Setup JXMapViewer
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            // Set the focus
            mapViewer.setZoom(7);
            mapViewer.setAddressLocation(new GeoPosition(18.8, -84.0));

            // Add interactions
            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            // Display the viewer in a JFrame
            JFrame frame = new JFrame("MaoTRAOD");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        // 1. 输入轨迹
        List<Trajectory> input_trajs = Input.getHurricane();
        {
            int sum = 0;
            for (Trajectory traj : input_trajs) {
                sum += traj.points.size();
            }
            System.out.println("point number:" + sum);
        }

        {
            for (Trajectory traj : input_trajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                painter.setColor(Color.GREEN);
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }

        // 2. 简化轨迹
        List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();
        List<TFragment> simplified_fragments = new ArrayList<TFragment>();
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


        // 3. 计算轨迹划分密度
        DensityCaculate caculate = new DensityCaculate(simplified_fragments);
        simplified_fragments = caculate.getDensity();


        // 4. 轨迹分段的异常检测
        LAFD lafd = new LAFD(simplified_fragments);
        lafd.TH_DIST = 80;
        List<TFragment> outlier_fragments = lafd.getOutlierFragments();

        // 5. 进化的轨迹异常
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


        // TOP-K
        Set<Integer> outlier_id = new HashSet<Integer>();
        for (int objectId : traj_eaf.keySet()) {
            if (traj_eaf.get(objectId) / traj_cnt.get(objectId) >= avg_eaf.get(avg_eaf.size() - TOP_K))
                outlier_id.add(objectId);
        }
        System.out.println("异常轨迹数目：" + outlier_id.size() + " " + avg_eaf.get(avg_eaf.size() - TOP_K));

        {
            for (Trajectory traj : input_trajs) {
                if (outlier_id.contains(traj.getObjectID())) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                    painter.setColor(Color.RED);
                    painters.add(painter);
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }
    }
}
