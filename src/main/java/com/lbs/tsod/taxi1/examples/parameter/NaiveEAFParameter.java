package com.lbs.tsod.taxi1.examples.parameter;

import com.lbs.tsod.taxi1.algorithm.DensityCaculate;
import com.lbs.tsod.taxi1.algorithm.LAFD;
import com.lbs.tsod.taxi1.algorithm.TrajectorySimplify;
import com.lbs.tsod.taxi1.file.InputParameter;
import com.lbs.tsod.taxi1.model.RoutePainter;
import com.lbs.tsod.taxi1.model.TFragment;
import com.lbs.tsod.taxi1.model.TPoint;
import com.lbs.tsod.taxi1.model.Trajectory;
import com.lbs.tsod.taxi1.osm.JxmapHelper;
import com.lbs.tsod.taxi1.osm.WGSTOGCJ02;
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
import java.io.FileWriter;
import java.util.*;
import java.util.List;

//检测速度异常
public class NaiveEAFParameter {

    public static double TH_EAF = 5D;
    public static int TOP_K = 25;

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
            String PATH = "C:\\Users\\toyking\\Desktop\\实验数据\\";
            WGSTOGCJ02 wgs = new WGSTOGCJ02();
            FileWriter fw = new FileWriter(PATH + "2.txt");
            for (Trajectory traj : simplified_trajs) {
                if (traj.getObjectID() > 10 && traj.getObjectID() % 10 != 1) {
                    fw.write("0");
                    for (TPoint p : traj.points) {
                        //System.out.println(p.x+" "+p.y+" "+wgs.transform(p.x, p.y).x);
                        fw.write("," + wgs.transform(p.x, p.y).x + " " + wgs.transform(p.x, p.y).y);
                    }
                    fw.write("\n");
                }
            }
            for (Trajectory traj : simplified_trajs) {
                if (traj.getObjectID() <= 10 || traj.getObjectID() % 10 == 1) {
                    fw.write("1");
                    for (TPoint p : traj.points) {
                        fw.write("," + wgs.transform(p.x, p.y).x + " " + wgs.transform(p.x, p.y).y);
                    }
                    fw.write("\n");
                }

            }
            fw.close();
        }
        {
            for (Trajectory traj : simplified_trajs) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                painter.setColor(Color.GREEN);
                painter.line_width = 2;
                painters.add(painter);
            }
            for (Trajectory traj : simplified_trajs) {
                if (traj.getObjectID() <= 10 || traj.getObjectID() % 10 == 1) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                    painter.setColor(Color.RED);
                    painter.line_width = 2;
                    painters.add(painter);
                }
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }

        double max_fmeasure = 0;
        for (LAFD.TH_DIST = 2E-4; LAFD.TH_DIST <= 1E-1; LAFD.TH_DIST = LAFD.TH_DIST * 2) {
            System.out.print("TH_DIST:" + LAFD.TH_DIST + " Precision:");
            for (TH_EAF = 0; TH_EAF <= 6.0D; TH_EAF += 1.0D) {
                Map<Integer, Double> object_lafd = new HashMap<Integer, Double>();
                for (int i = 30; i <= InputParameter.time_max; i += 30) {
                    List<TFragment> input_fragments = new ArrayList<TFragment>();
                    for (TFragment fragment : simplified_fragments) {
                        if (fragment.tp2.time >= i - 30 && fragment.tp2.time <= i) {
                            input_fragments.add(fragment);
                        }
                    }

                    // 计算轨迹划分密度
                    DensityCaculate caculate = new DensityCaculate(input_fragments);
                    input_fragments = caculate.getDensity();

                    // 轨迹分段的异常检测
                    LAFD lafd = new LAFD(input_fragments);
                    //lafd.TH_DIST = 4E-3;
                    List<TFragment> outlier_fragments = lafd.getOutlierFragments();
                    /*{
                        int cnt = 0;
                        for (TFragment fragment : outlier_fragments) {
                            if (fragment.ILAF > 1.9) {
                                RoutePainter painter = JxmapHelper.getRoutePainter(fragment);
                                painter.setColor(Color.magenta);
                                painter.line_width = 2;
                                painters.add(painter);
                                cnt++;
                            }
                        }
                        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                        Thread.sleep(1000);
                        System.out.println("异常分段数：" + cnt);
                    }*/


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
                        //System.out.println(traj_eaf.get(id) / traj_cnt.get(id));
                        double EAF = traj_eaf.get(id) / traj_cnt.get(id);
                        object_lafd.put(id, EAF);
                    }
                }

                List<Double> all_eaf = new ArrayList<Double>();
                for (double eaf : object_lafd.values()) all_eaf.add(eaf);
                Collections.sort(all_eaf);

                Set<Integer> outlierID = new HashSet<Integer>();
                {
                    for (int objectId : object_lafd.keySet()) {
                        if (object_lafd.get(objectId) > TH_EAF) outlierID.add(objectId);
                    }
                }

                //System.out.println("进化的异常对象个数：" + outlierID.size());


                List<Trajectory> outlier_traj = new ArrayList<Trajectory>();
                //List<Trajectory> outlier_traj_file = InputParameter.getOutlier();
                for (Trajectory trajectory : input_trajs) {
                    if (outlierID.contains(trajectory.getObjectID())) {
                        outlier_traj.add(trajectory);
                    }
                }
                //System.out.print("TH_DIST:" + LAFD.TH_DIST + " TH_EAF:" + TH_EAF);

                double tp = 0.0D;
                for (Trajectory traj : outlier_traj) {
                    if (traj.getObjectID() <= 10 || traj.getObjectID() % 10 == 1) tp += 1.0D;
                }

                double precision = tp / outlier_traj.size(), recall = tp / 28.0;
                double fmeasure = 2 * precision * recall / (precision + recall);
                //System.out.print((tp / outlier_traj.size())+" ");//Precision
                System.out.print(fmeasure + " ");//Recall

                if (fmeasure > max_fmeasure) {
                    max_fmeasure = fmeasure;
                    for (Trajectory trajectory : input_trajs) {
                        if (!outlierID.contains(trajectory.getObjectID())) {
                            RoutePainter routePainter = JxmapHelper.getRoutePainter(trajectory);
                            routePainter.line_width = 1;
                            routePainter.setColor(Color.GREEN);
                            painters.add(routePainter);
                        }
                    }

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
            System.out.println();
        }
    }
}
