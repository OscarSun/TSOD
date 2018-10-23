package com.lbs.tsod.taxi1.examples.parameter;

import com.lbs.tsod.taxi1.algorithm.LAFD;
import com.lbs.tsod.taxi1.algorithm.MicroCluster;
import com.lbs.tsod.taxi1.algorithm.TrajectorySimplify;
import com.lbs.tsod.taxi1.algorithm.centerline.CenterLineFitting;
import com.lbs.tsod.taxi1.file.InputParameter;
import com.lbs.tsod.taxi1.model.*;
import com.lbs.tsod.taxi1.osm.TNode;
import com.lbs.tsod.taxi1.osm.TWay;
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

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by toyking on 2016/10/11.
 */
public class IncrementalEAFParameter {

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
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

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


        for (LAFD.TH_DIST = 2E-4; LAFD.TH_DIST <= 1E-1; LAFD.TH_DIST = LAFD.TH_DIST * 2) {
            //System.out.print("\nTH_DIST:" + LAFD.TH_DIST + " Precision: ");
            System.out.print("\nTH_DIST:" + LAFD.TH_DIST + " Recall: ");

            Map<Integer, Double> object_eaf = new HashMap<Integer, Double>();

            for (int t = 30; t <= InputParameter.time_max; t += 30) {
                List<TFragment> input_fragments = new ArrayList<TFragment>();
                for (TFragment fragment : simplified_fragments) {
                    if (fragment.tp2.time >= t - 30 && fragment.tp2.time <= t) {
                        input_fragments.add(fragment);
                    }
                }

                // 步骤3 第一遍聚类
                MicroCluster cluster = new MicroCluster(input_fragments);
                cluster.TH_DISTANCE = 1E-9;
                List<AllyFragment> allyFragments = cluster.getCluster();
                //System.out.println("聚类数：" + allyFragments.size());

                // 步骤4 计算代表轨迹
                List<TFragment> reFragments = new ArrayList<TFragment>();
                int id = 0;
                Map<Integer, AllyFragment> re_allyFragment = new HashMap<Integer, AllyFragment>();
                Map<Integer, TWay> re_TWay = new HashMap<Integer, TWay>();

                for (AllyFragment allyFragment : allyFragments) {
                    List<LineSegment> clu = new ArrayList<LineSegment>();
                    for (TFragment fragment : allyFragment.af) {
                        clu.add(new LineSegment(fragment.tp1, fragment.tp2));
                    }

                    //显示某个聚类的轨迹
                    /*{
                        for (TFragment fragment : allyFragment.af) {
                            RoutePainter painter = fragment.getRoutePainter();
                            painter.line_width = 2;
                            painter.setColor(Color.CYAN);
                            painters.add(painter);

                        }
                        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                        //Thread.sleep(2000);
                    }*/

                    CenterLineFitting fitting = new CenterLineFitting(clu);
                    TWay way = fitting.getCenterLine();
                    if (way.nodes.size() > 0) {
                        TNode avgNode = way.getAvgNode();
                        TNode avgDir = way.getDir();
                        TPoint p1 = new TPoint(new Coordinate(avgNode.x + avgDir.x * way.getMinlenth() / 2.0, avgNode.y + avgDir.y * way.getMinlenth() / 2.0));
                        TPoint p2 = new TPoint(new Coordinate(avgNode.x - avgDir.x * way.getMinlenth() / 2.0, avgNode.y - avgDir.y * way.getMinlenth() / 2.0));
                        p1.objectID = p2.objectID = id++;
                        p1.speed = p2.speed = allyFragment.getSpeed();
                        TFragment reFragment = new TFragment(p1, p2);
                        reFragment.speed = allyFragment.getSpeed();
                        re_allyFragment.put(p1.objectID, allyFragment);
                        re_TWay.put(p1.objectID, way);
                        if (p1.distance(p2) > 1e-5 && p1.distance(p2) < 1) reFragments.add(reFragment);

                        //显示代表轨迹
                            /*{
                                RoutePainter painter = JxmapHelper.getRoutePainter(reFragment);
                                painter.setColor(Color.GREEN);
                                painter.line_width = 2;
                                painters.add(painter);
                                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));


                                // Create waypoints from the geo-positions
                                Set<LabelWaypoint> waypoints = new HashSet<LabelWaypoint>(Arrays.asList(
                                        new LabelWaypoint("speed:" + reFragment.speed, new GeoPosition((p1.y + p2.y) / 2.0, (p1.x + p2.x) / 2.0))
                                ));

                                // Set the overlay painter
                                WaypointPainter<LabelWaypoint> labelWaypointPainter = new LabelWaypointOverlayPainter();
                                labelWaypointPainter.setWaypoints(waypoints);
                                painters.add(labelWaypointPainter);
                                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));

                                // Add the JButtons to the map viewer
                                for (LabelWaypoint w : waypoints) {
                                    mapViewer.add(w.getLabel());
                                }
                            }*/
                    }
                }

                LAFD lafd1 = new LAFD(reFragments);
                //lafd1.TH_DIST = T;
                List<TFragment> fragmentsILAF = lafd1.getOutlierFragments();

                /*//显示轨迹划分异常
                double tp = 0.0D, sumP = 0.0D, sumR = 0.0D;
                for (TFragment fragment : fragmentsILAF) {
                    if (fragment.ILAF >= TH_EAF) {
                        AllyFragment allyFragment = re_allyFragment.get(fragment.getObjectId());
                        sumP += allyFragment.af.size();
                        for (TFragment fr : allyFragment.af) {
                            if (fr.getObjectId() % 10 == 1 || fr.getObjectId() <= 10) {
                                tp += 1.0D;
                            }
                        }
                    }
                }

                for (TFragment fragment : simplified_fragments) {
                    if (fragment.getObjectId() % 10 == 1 || fragment.getObjectId() <= 10) sumR += 1.0D;
                }

                System.out.println("Precision:" + tp / sumP + " tp:" + tp + " sumP:" + sumP);
                System.out.println("Recall:" + tp / sumR + " tp:" + tp + " sumR:" + sumR);*/

                // 步骤6 进化的轨迹异常
                Map<Integer, Double> snapshot_ILAF = new HashMap<Integer, Double>();
                Map<Integer, Double> snapshot_cnt = new HashMap<Integer, Double>();
                for (TFragment fragment : fragmentsILAF) {
                    AllyFragment allyFragment = re_allyFragment.get(fragment.getObjectId());
                    for (TFragment fr : allyFragment.af) {
                        double ILAF = 0.0D;
                        if (snapshot_ILAF.containsKey(fr.getObjectId())) ILAF = snapshot_ILAF.get(fr.getObjectId());
                        ILAF += fragment.ILAF;
                        snapshot_ILAF.put(fr.getObjectId(), ILAF);

                        double cnt = 0.0D;
                        if (snapshot_cnt.containsKey(fr.getObjectId())) cnt = snapshot_cnt.get(fr.getObjectId());
                        cnt += 1.0D;
                        snapshot_cnt.put(fr.getObjectId(), cnt);
                    }
                }
                for (int object_id : snapshot_ILAF.keySet()) {
                    double avg = snapshot_ILAF.get(object_id) / snapshot_cnt.get(object_id);
                    double eaf = avg * t * t / (InputParameter.time_max * InputParameter.time_max);
                    double sum = 0;
                    if (object_eaf.containsKey(object_id)) sum = object_eaf.get(object_id);
                    object_eaf.put(object_id, sum + eaf);
                }
            }

            for (double TH_EAF = 0; TH_EAF <= 6.0D; TH_EAF += 1.0D) {
                double tp = 0.0D, sumP = 0.0D, sumR = 28.0D;
                for (Trajectory traj : input_trajs) {
                    if (object_eaf.get(traj.getObjectID()) > TH_EAF) {
                        sumP += 1.0D;
                        if (traj.getObjectID() <= 10 || traj.getObjectID() % 10 == 1) tp += 1.0D;
                    }
                }
                //System.out.print(tp / sumP + " ");//Precision
                System.out.print((tp / 28.0) + " ");//Recall
            }
        }
    }
}
