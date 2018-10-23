package com.lbs.tsod.taxi2.examples;

import com.lbs.tsod.taxi2.algorithm.*;
import com.lbs.tsod.taxi2.file.*;
import com.lbs.tsod.taxi2.model.RoutePainter;
import com.lbs.tsod.taxi2.model.TFragment;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Evolution {
    public final static double timestap = 6;
    public final static int TOP_K = 3;

    public static void main(String args[]) throws Exception {
        final JXMapViewer mapViewer = new JXMapViewer();
        {
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            mapViewer.setZoom(7);
            mapViewer.setAddressLocation(new GeoPosition(31.30467978387644, 121.60001801642865));

            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            JFrame frame = new JFrame("进化的轨迹异常检测");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        // 步骤1 进化的轨迹异常检测——动态可视化输出
        long time = new SimpleDateFormat("yyyyMMddHHmmss").parse("20131008100000").getTime();
        Map<Integer, Double> object_lafd = new HashMap<Integer, Double>();
        List<TFragment> all_fragment = new ArrayList<TFragment>();
        for (int i = 2; i <= timestap; i += 2) {
            // 步骤1 简化轨迹分段
            Date begin = new Date(time + 60 * (i - 2) * 1000);
            Date end = new Date(time + 60 * i * 1000);
            List<TFragment> simplified_fragments = InputHelper.getSimplifiedFragment(begin, end);
            System.out.println("简化后的轨迹分段数：" + simplified_fragments.size());
            {
                for (TFragment fragment : simplified_fragments) {
                    all_fragment.add(fragment);
                    RoutePainter painter = fragment.getRoutePainter();
                    double speed = fragment.getSpeed() * 1e4 * 11 * 3600 / 1000;
                    if (speed < 10) painter.setColor(new Color(175, 238, 238));
                    else if (speed < 20) painter.setColor(new Color(176, 224, 230));
                    else if (speed < 30) painter.setColor(new Color(176, 196, 222));
                    else if (speed < 40) painter.setColor(new Color(135, 206, 250));
                    else if (speed < 50) painter.setColor(new Color(0, 191, 255));
                    else if (speed < 60) painter.setColor(new Color(30, 144, 255));
                    else if (speed < 70) painter.setColor(new Color(65, 105, 255));
                    else if (speed < 80) painter.setColor(new Color(0, 0, 255));
                    else painter.setColor(new Color(0, 0, 128));
                    painter.line_width = 2;
                    painters.add(painter);
                }
                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            }


            // 轨迹划分密度
            DensityCaculate caculate = new DensityCaculate(simplified_fragments);
            simplified_fragments = caculate.getDensity();

            // 步骤2 轨迹分段的异常检测
            List<TFragment> outlier_fragments;
            LAFD lafd = new LAFD(simplified_fragments);
            lafd.TH_DIST = 4E-3;
            outlier_fragments = lafd.getOutlierFragments();
            /*{
                for (TFragment fragment : outlier_fragments) {
                    if (fragment.ILAF > 2D) {
                        RoutePainter painter = fragment.getRoutePainter();
                        painter.setColor(Color.MAGENTA);
                        painter.line_width = 3;
                        painters.add(painter);
                        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                        Thread.sleep(1000);
                    }
                }
            }*/
            {
                for (TFragment fragment : outlier_fragments) {
                    if (fragment.ILAF > 2D) {
                        System.out.println(fragment.getObjectId() + "的局部异常的速度：" + fragment.speed);
                        for (TFragment fr : outlier_fragments) {
                            if (fr.getObjectId() == fragment.getObjectId()) continue;
                            if (fr.getDist(fragment) * fragment.adj < LAFD.TH_DIST) {
                                System.out.println(fragment.getObjectId() + "的周围点" + fr.getObjectId() + "的速度：" + fr.speed);
                            }
                        }
                    }
                }
            }

            // 步骤3 进化的轨迹异常
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
            for(int id:traj_eaf.keySet()){
                double EAF = 0D;
                if (object_lafd.containsKey(id))  EAF = object_lafd.get(id);
                EAF+=i * i * 1.0 / (timestap * timestap)*traj_eaf.get(id) / traj_cnt.get(id);
                object_lafd.put(id, EAF);
            }
        }

        Thread.sleep(20000);

        List<Double> all_eaf = new ArrayList<Double>();
        for(double eaf:object_lafd.values()) all_eaf.add(eaf);
        Collections.sort(all_eaf);

        Set<Integer> outlierID = new HashSet<Integer>();
        for (int objectId : object_lafd.keySet()) {
            if (object_lafd.get(objectId) > all_eaf.get(all_eaf.size() - TOP_K)) outlierID.add(objectId);
        }

        System.out.print("进化的异常对象个数：" + outlierID.size() + "，分别是：");
        for (int id : outlierID) {
            System.out.print(id + " ");
        }
        System.out.println();

        for (TFragment fragment : all_fragment) {
            if (outlierID.contains(fragment.getObjectId())) {
                RoutePainter routePainter = fragment.getRoutePainter();
                routePainter.line_width = 3;
                routePainter.setColor(Color.RED);
                painters.add(routePainter);
            }
        }
        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
    }


}
