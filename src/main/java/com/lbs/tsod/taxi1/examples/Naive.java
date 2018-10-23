package com.lbs.tsod.taxi1.examples;

import com.lbs.tsod.taxi1.file.*;
import com.lbs.tsod.taxi1.algorithm.*;
import com.lbs.tsod.taxi1.model.*;
import com.lbs.tsod.taxi1.osm.*;

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

//检测速度异常
public class Naive {

    public final static double TH_EAF = 15D;

    public static void main(String args[]) throws Exception {
        final JXMapViewer mapViewer = new JXMapViewer();
        {
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            mapViewer.setZoom(5);
            mapViewer.setAddressLocation(new GeoPosition(31.2814, 121.5831));

            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            JFrame frame = new JFrame("Naive");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        // 输入轨迹
        List<Trajectory> input_trajs = Input.getTaxi();
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


        // 计算轨迹划分密度
        DensityCaculate caculate = new DensityCaculate(simplified_fragments);
        simplified_fragments = caculate.getDensity();

        // 轨迹分段的异常检测
        LAFD lafd = new LAFD(simplified_fragments);
        lafd.TH_DIST = 2E-3;
        List<TFragment> outlier_fragments = lafd.getOutlierFragments();
        {
            int cnt = 0;
            for (TFragment fragment : outlier_fragments) {
                if (fragment.ILAF > 2) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(fragment);
                    painter.setColor(Color.RED);
                    painter.line_width = 2;
                    painters.add(painter);
                    cnt++;
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            System.out.println("异常分段数：" + cnt);
        }


        //进化的轨迹异常
        Map<Integer, Double> object_lafd = new HashMap<Integer, Double>();
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
        for (int id : traj_eaf.keySet()) {
            double EAF = traj_eaf.get(id) / traj_cnt.get(id);
            object_lafd.put(id, EAF);
        }

        Set<Integer> outlierID = new HashSet<Integer>();
        /*{
            for (int objectId : traj_eaf.keySet()) {
                System.out.println("AVG_EAF:" + object_lafd.get(objectId));
                if (traj_eaf.get(objectId) > TH_EAF) outlierID.add(objectId);
            }
        }*/
        {
            List<Double> all_eaf = new ArrayList<Double>();
            for (double eaf : object_lafd.values()) all_eaf.add(eaf);
            Collections.sort(all_eaf);

            for (int objectId : object_lafd.keySet()) {
                if (object_lafd.get(objectId) > all_eaf.get(all_eaf.size() - 19)) outlierID.add(objectId);
            }
        }

        System.out.println("异常轨迹条数：" + outlierID.size());

        for (Trajectory trajectory : input_trajs) {
            if (outlierID.contains(trajectory.getObjectID())) {
                RoutePainter routePainter = JxmapHelper.getRoutePainter(trajectory);
                routePainter.line_width = 1;
                routePainter.setColor(Color.RED);
                painters.add(routePainter);
            }
        }
        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
    }
}
