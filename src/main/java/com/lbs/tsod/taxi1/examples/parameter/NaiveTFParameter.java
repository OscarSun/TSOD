package com.lbs.tsod.taxi1.examples.parameter;

import com.lbs.tsod.taxi1.algorithm.DensityCaculate;
import com.lbs.tsod.taxi1.algorithm.LAFD;
import com.lbs.tsod.taxi1.algorithm.TrajectorySimplify;
import com.lbs.tsod.taxi1.file.InputParameter;
import com.lbs.tsod.taxi1.model.RoutePainter;
import com.lbs.tsod.taxi1.model.TFragment;
import com.lbs.tsod.taxi1.model.Trajectory;
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

public class NaiveTFParameter {

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

            JFrame frame = new JFrame("TRAOD");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        // 输入轨迹
        List<Trajectory> input_trajs = InputParameter.getTaxi();
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
        {
            for (Trajectory traj : simplified_trajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                painter.setColor(Color.GREEN);
                painter.line_width = 1;
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }
        {
            for (Trajectory traj : simplified_trajs) {
                if (traj.getObjectID() <= 10 || traj.getObjectID() % 10 == 1) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                    painter.setColor(Color.RED);
                    painter.line_width = 1;
                    painters.add(painter);
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            Thread.sleep(10000);
        }

        for (LAFD.TH_DIST = 2E-4; LAFD.TH_DIST <= 1E-1; LAFD.TH_DIST = LAFD.TH_DIST * 2) {
            System.out.print("TH_DIST:" + LAFD.TH_DIST + " Precision:");
            for (double TH_ILAF = 0.0D; TH_ILAF <= 7.0D; TH_ILAF += 1.0D) {
                // 计算轨迹划分密度
                DensityCaculate caculate = new DensityCaculate(simplified_fragments);
                simplified_fragments = caculate.getDensity();

                // 轨迹分段的异常检测
                LAFD lafd = new LAFD(simplified_fragments);
                //lafd.TH_DIST = 4E-3;
                List<TFragment> outlier_fragments = lafd.getOutlierFragments();

                double tp = 0.0D, sumP = 0.0D, sumR = 0.0D;
                for (TFragment fragment : outlier_fragments) {
                    if (fragment.ILAF > TH_ILAF) {
                        sumP += 1.0D;
                        if (fragment.getObjectId() % 10 == 1 || fragment.getObjectId() <= 10) tp += 1.0D;
                    }
                }
                for (TFragment fragment : simplified_fragments) {
                    if (fragment.getObjectId() % 10 == 1 || fragment.getObjectId() <= 10) sumR += 1.0D;
                }

                //System.out.print(tp / sumP + " ");//Precision
                System.out.print(tp / sumR + " ");//Recall*/
            }
            System.out.println();
        }
    }
}
