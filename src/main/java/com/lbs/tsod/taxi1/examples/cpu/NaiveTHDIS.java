package com.lbs.tsod.taxi1.examples.cpu;

import com.lbs.tsod.taxi1.algorithm.DensityCaculate;
import com.lbs.tsod.taxi1.algorithm.LAFD;
import com.lbs.tsod.taxi1.algorithm.MicroCluster;
import com.lbs.tsod.taxi1.algorithm.TrajectorySimplify;
import com.lbs.tsod.taxi1.algorithm.centerline.CenterLineFitting;
import com.lbs.tsod.taxi1.file.InputParameter;
import com.lbs.tsod.taxi1.model.*;
import com.lbs.tsod.taxi1.osm.JxmapHelper;
import com.lbs.tsod.taxi1.osm.TNode;
import com.lbs.tsod.taxi1.osm.TWay;
import com.lbs.tsod.taxi2.model.LabelWaypoint;
import com.lbs.tsod.taxi2.model.LabelWaypointOverlayPainter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by toyking on 2016/10/11.
 */
public class NaiveTHDIS {

    public final static int TOP_K = 410;

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
        //List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();


        for (double TH_DIST = 0.0512; TH_DIST <= 0.2; TH_DIST = TH_DIST * 2) {
            System.out.print("\nTH_DIST:" + TH_DIST + " Time: ");

            double sum_time = 0.0D;
            for (int k = 0; k < 7; k++) {
                List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

                Date begin_time = new Date();

                // 输入轨迹
                List<Trajectory> input_trajs = InputParameter.getTaxi();
                /*System.out.println("共有轨迹数：" + input_trajs.size());
                {
                    for (Trajectory traj : input_trajs) {
                        RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                        painter.setColor(Color.GREEN);
                        painter.line_width = 1;
                        painters.add(painter);
                    }
                    mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                }*/


                // 简化轨迹
                List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();
                List<TFragment> simplified_fragments = new ArrayList<TFragment>();
                {
                    for (Trajectory traj : input_trajs) {
                        TrajectorySimplify simplify = new TrajectorySimplify(traj);
                        simplified_trajs.add(simplify.getSimplifiedTrajectoty());
                    }
                }
                {
                    for (Trajectory traj : simplified_trajs) {
                        for (int i = 1; i < traj.points.size(); i++) {
                            TFragment fr = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                            simplified_fragments.add(fr);
                        }
                    }

                }


                // 计算轨迹划分密度
                /*DensityCaculate caculate = new DensityCaculate(simplified_fragments);
                simplified_fragments = caculate.getDensity();*/

                // 轨迹分段的异常检测
                LAFD lafd = new LAFD(simplified_fragments);
                lafd.TH_DIST = TH_DIST;
                List<TFragment> outlier_fragments = lafd.getOutlierFragments();

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

                Date end_time = new Date();

                sum_time += (end_time.getTime() - begin_time.getTime()) * 1.0D / 1000.0D;
                System.out.print(sum_time + " ");
            }

        }
    }
}
