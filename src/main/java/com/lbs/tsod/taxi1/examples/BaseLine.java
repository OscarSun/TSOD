package com.lbs.tsod.taxi1.examples;

import com.lbs.tsod.taxi1.algorithm.*;
import com.lbs.tsod.taxi1.file.*;
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

//对比算法检测方向异常
public class BaseLine {
    public final static double D = 1E-3;
    public final static double p = 0.95;
    public final static double wV = 1.0 / 3.0D;
    public final static double wH = 1.0 / 3.0D;
    public final static double wX = 1.0 / 3.0D;
    public final static double F = 0.8;

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

        //获取输入的轨迹
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

        // 第一步：轨迹划分
        List<Trajectory> simplifiedTrajs = new ArrayList<Trajectory>();
        List<TFragment> fragments = new ArrayList<TFragment>();
        {
            for (Trajectory traj : input_trajs) {
                Partition simplify = new Partition(traj);
                simplifiedTrajs.add(simplify.getTrajectoryPartition());
            }
            for (Trajectory traj : simplifiedTrajs) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tf = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    fragments.add(tf);
                }
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
        DensityCaculateBaseLine caculate = new DensityCaculateBaseLine(fragments);
        fragments = caculate.getDensity();


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

        System.out.println("异常轨迹条数：" + outlier_trajectories.size());
        {
            for (Trajectory traj : input_trajs) {
                if (outlier_trajectories.contains(traj.getObjectID())) {
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
