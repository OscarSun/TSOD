package com.lbs.tsod.taxi2.examples;

import com.lbs.tsod.taxi2.model.LabelWaypoint;
import com.lbs.tsod.taxi2.algorithm.LAFD;
import com.lbs.tsod.taxi2.algorithm.MicroCluster;
import com.lbs.tsod.taxi2.algorithm.TrajectorySimplify;
import com.lbs.tsod.taxi2.algorithm.centerline.CenterLineFitting;
import com.lbs.tsod.taxi2.file.InputSoda15;
import com.lbs.tsod.taxi2.model.*;
import com.lbs.tsod.taxi2.osm.JxmapHelper;
import com.lbs.tsod.taxi2.osm.TNode;
import com.lbs.tsod.taxi2.osm.TWay;
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
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class IncrementalSoda15 {

    public final static int TOP_K = 8;

    public static void main(String args[]) throws Exception {

        final JXMapViewer mapViewer = new JXMapViewer();
        {
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            // Set the focus
            mapViewer.setZoom(7);
            mapViewer.setAddressLocation(new GeoPosition(31.2737, 121.4152));

            // Add interactions
            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            // Display the viewer in a JFrame
            JFrame frame = new JFrame("增量的轨迹异常检测SODA15日");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        List<Trajectory> all_trajs = InputSoda15.getTaxi();
        /*{
            for (Trajectory traj : all_trajs) {
                for (int j = 1; j < traj.points.size(); j++) {
                    TFragment fr = new TFragment(traj.points.get(j - 1), traj.points.get(j));
                    RoutePainter painter = JxmapHelper.getRoutePainter(fr);
                    if (fr.getSpeed() < 10) painter.setColor(new Color(0, 0, 128));
                    else if (fr.getSpeed() < 20) painter.setColor(new Color(0, 0, 255));
                    else if (fr.getSpeed() < 30) painter.setColor(new Color(65, 105, 255));
                    else if (fr.getSpeed() < 40) painter.setColor(new Color(30, 144, 255));
                    else if (fr.getSpeed() < 50) painter.setColor(new Color(0, 191, 255));
                    else if (fr.getSpeed() < 60) painter.setColor(new Color(135, 206, 250));
                    else if (fr.getSpeed() < 70) painter.setColor(new Color(176, 196, 222));
                    else if (fr.getSpeed() < 80) painter.setColor(new Color(176, 224, 230));
                    else painter.setColor(new Color(175, 238, 238));
                    painter.line_width = 2;
                    painters.add(painter);
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }
        Thread.sleep(30000);*/

        FileWriter fw = new FileWriter("C:\\Users\\toyking\\Desktop\\Map\\Output\\outlier15.txt");
        Map<Integer, Double> object_eaf = new HashMap<Integer, Double>();
        long time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-04-15 08:00:00").getTime();
        for (int t = 5; t <= 30; t += 5) {

            // 步骤1 输入轨迹
            Date begin = new Date(time + (t - 5) * 60 * 1000);
            Date end = new Date(time + t * 60 * 1000);
            List<Trajectory> input_trajs = InputSoda15.getTaxi(begin, end);
            System.out.println("共有轨迹数：" + input_trajs.size());
            /*{
                for (Trajectory traj : input_trajs) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                    painter.setColor(Color.GREEN);
                    painter.line_width = 2;
                    painters.add(painter);
                }
                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            }*/

            // 步骤2 简化轨迹
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
                    for (int j = 1; j < traj.points.size(); j++) {
                        TFragment fr = new TFragment(traj.points.get(j - 1), traj.points.get(j));
                        simplified_fragments.add(fr);
                    }
                }
                System.out.println("共有轨迹分段数：" + simplified_fragments.size());
            }

            // 步骤3 第一遍聚类
            MicroCluster cluster = new MicroCluster(simplified_fragments);
            List<AllyFragment> allyFragments = cluster.getCluster();
            System.out.println("聚类数：" + allyFragments.size());

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

                    /*//显示某个聚类的轨迹
                    {
                        for (TFragment fragment : allyFragment.af) {
                            RoutePainter painter = fragment.getRoutePainter();
                            painter.line_width = 2;
                            painter.setColor(Color.CYAN);
                            painters.add(painter);

                        }
                        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                        Thread.sleep(100);
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

                    if (t == 30) {
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
                    }
                }
            }

            if (t == 30) {
                for (TFragment fr : reFragments) {
                    writeFile(fw, 2, re_TWay.get(fr.getObjectId()));
                    RoutePainter painter = JxmapHelper.getRoutePainter(re_TWay.get(fr.getObjectId()));
                    painter.setColor(Color.GREEN);
                    /*if (fr.getSpeed() < 10) painter.setColor(new Color(0, 0, 128));
                    else if (fr.getSpeed() < 20) painter.setColor(new Color(0, 0, 255));
                    else if (fr.getSpeed() < 30) painter.setColor(new Color(65, 105, 255));
                    else if (fr.getSpeed() < 40) painter.setColor(new Color(30, 144, 255));
                    else if (fr.getSpeed() < 50) painter.setColor(new Color(0, 191, 255));
                    else if (fr.getSpeed() < 60) painter.setColor(new Color(135, 206, 250));
                    else if (fr.getSpeed() < 70) painter.setColor(new Color(176, 196, 222));
                    else if (fr.getSpeed() < 80) painter.setColor(new Color(176, 224, 230));
                    else painter.setColor(new Color(175, 238, 238));*/
                    painter.line_width = 2;
                    painters.add(painter);
                }
                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            }

            // 步骤5 计算异常
            LAFD lafd1 = new LAFD(reFragments);
            lafd1.TH_DIST = 3E-2;
            List<TFragment> fragmentsILAF = lafd1.getOutlierFragments();

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
                double eaf = avg * t * t / (30.0 * 30.0);
                double sum = 0;
                if (object_eaf.containsKey(object_id)) sum = object_eaf.get(object_id);
                object_eaf.put(object_id, sum + eaf);
            }

            //显示轨迹划分异常
            if (t == 30) {
                List<Double> list = new ArrayList<Double>();
                for (TFragment fragment : fragmentsILAF) list.add(fragment.ILAF);
                Collections.sort(list);
                for (TFragment fragment : fragmentsILAF) {
                    if (fragment.ILAF >= list.get(list.size() - TOP_K)) {
                        writeFile(fw, 3, re_TWay.get(fragment.getObjectId()));
                        RoutePainter painter = JxmapHelper.getRoutePainter(re_TWay.get(fragment.getObjectId()));
                        painter.setColor(Color.yellow);
                        painter.line_width = 4;
                        painters.add(painter);
                    }
                }
                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            }

        }

        //显示轨迹异常
        List<Double> list = new ArrayList<Double>();
        for (double eaf : object_eaf.values()) list.add(eaf);
        Collections.sort(list);

        for (double eaf : list) System.out.println("EAF:" + eaf);


        for (Trajectory traj : all_trajs) {
            if (object_eaf.containsKey(traj.getObjectID()) && object_eaf.get(traj.getObjectID()) > list.get(list.size() - TOP_K)) {
                writeFile(fw, 1, traj);
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                painter.line_width = 2;
                painter.setColor(Color.RED);
                painters.add(painter);
            }
        }
        fw.close();
        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));

        /*WGSTOGCJ02 wgstogcj02 = new WGSTOGCJ02();
        System.out.println(wgstogcj02.transform(121.4152, 31.2737).x + " " + wgstogcj02.transform(121.4152, 31.2737).y);
        System.out.println(wgstogcj02.transform(121.4638, 31.3031).x + " " + wgstogcj02.transform(121.4638, 31.3031).y);*/
    }


    public static void writeFile(FileWriter fw, int type, Trajectory traj) throws Exception {
        if (traj.points.size() == 88) {
            WGSTOGCJ02 wgstogcj02 = new WGSTOGCJ02();
            fw.write(type + "");
            for (int i = 0; i < traj.points.size(); i++) {
                if (i <= 50) continue;
                Coordinate c = wgstogcj02.transform(traj.points.get(i).x, traj.points.get(i).y);
                fw.write("," + c.x + " " + c.y);
            }
            fw.write("\r\n");
            return;
        }
        WGSTOGCJ02 wgstogcj02 = new WGSTOGCJ02();
        fw.write(type + "");
        for (int i = 0; i < traj.points.size(); i++) {
            Coordinate c = wgstogcj02.transform(traj.points.get(i).x, traj.points.get(i).y);
            fw.write("," + c.x + " " + c.y);
        }
        fw.write("\r\n");
    }

    public static void writeFile(FileWriter fw, int type, TWay way) throws Exception {
        if (way.nodes.size() == 88) {
            WGSTOGCJ02 wgstogcj02 = new WGSTOGCJ02();
            fw.write(type + "");
            for (int i = 0; i < way.nodes.size(); i++) {
                if (i <= 50) continue;
                Coordinate c = wgstogcj02.transform(way.nodes.get(i).x, way.nodes.get(i).y);
                fw.write("," + c.x + " " + c.y);
            }
            fw.write("\r\n");
            return;
        }
        WGSTOGCJ02 wgstogcj02 = new WGSTOGCJ02();
        fw.write(type + "");
        for (int i = 0; i < way.nodes.size(); i++) {
            Coordinate c = wgstogcj02.transform(way.nodes.get(i).x, way.nodes.get(i).y);
            fw.write("," + c.x + " " + c.y);
        }
        fw.write("\r\n");
    }
}
