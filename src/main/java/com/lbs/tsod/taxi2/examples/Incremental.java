package com.lbs.tsod.taxi2.examples;

import com.lbs.tsod.taxi2.algorithm.*;
import com.lbs.tsod.taxi2.algorithm.centerline.CenterLineFitting;
import com.lbs.tsod.taxi2.model.*;
import com.lbs.tsod.taxi2.osm.*;
import com.lbs.tsod.taxi2.file.*;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Incremental {

    public final static double TH_ILAF = 3D;

    public static void main(String args[]) throws Exception {

        final JXMapViewer mapViewer = new JXMapViewer();
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        // Setup JXMapViewer
        {
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            // Set the focus
            mapViewer.setZoom(7);
            mapViewer.setAddressLocation(new GeoPosition(31.30467978387644, 121.60001801642865));

            // Add interactions
            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            // Display the viewer in a JFrame
            JFrame frame = new JFrame("增量的轨迹异常检测");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }


        int timegap = 40;
        long time = new SimpleDateFormat("yyyyMMddHHmmss").parse("20131008100000").getTime();
        for (int i = timegap; i <= timegap; i += timegap) {
            // 步骤1 简化的轨迹分段
            Date begin = new Date(time + 60 * (i - timegap) * 1000);
            Date end = new Date(time + 60 * i * 1000);
            List<TFragment> tFragments = InputHelper.getSimplifiedFragment(begin, end);
            System.out.println("简化后的轨迹分段数：" + tFragments.size());

            //显示简化后的轨迹
            {
                for (TFragment fr : tFragments) {
                    painters.add(fr.getRoutePainter());
                }
                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                Thread.sleep(1000);
            }

            // 步骤2 计算每个轨迹分段的ldd
            LAFD lafd = new LAFD(tFragments);
            List<TFragment> fragments = lafd.getOutlierFragments();

            // 步骤3 第一遍聚类
            MicroCluster cluster = new MicroCluster(fragments);
            List<AllyFragment> allyFragments = cluster.getCluster();
            System.out.println("聚类数：" + allyFragments.size());

            // 步骤4 计算代表轨迹
            List<TFragment> reFragments = new ArrayList<TFragment>();
            for (AllyFragment allyFragment : allyFragments) {
                List<LineSegment> clu = new ArrayList<LineSegment>();
                for (TFragment fragment : allyFragment.af) {
                    clu.add(new LineSegment(fragment.tp1, fragment.tp2));
                }

                //显示某个聚类的轨迹
                {
                    for (TFragment fragment : allyFragment.af) {
                        RoutePainter routePainter = JxmapHelper.getRoutePainter(fragment);
                        routePainter.setColor(Color.CYAN);
                        painters.add(routePainter);

                    }
                    mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                    Thread.sleep(100);
                }

                CenterLineFitting fitting = new CenterLineFitting(clu);
                TWay way = fitting.getCenterLine();
                if (way.nodes.size() > 0) {
                    TNode avgNode = way.getAvgNode();
                    TNode avgDir = way.getDir();
                    TPoint p1 = new TPoint(new Coordinate(avgNode.x + avgDir.x * way.getMinlenth() / 2.0, avgNode.y + avgDir.y * way.getMinlenth() / 2.0));
                    TPoint p2 = new TPoint(new Coordinate(avgNode.x - avgDir.x * way.getMinlenth() / 2.0, avgNode.y - avgDir.y * way.getMinlenth() / 2.0));
                    TFragment reFragment = new TFragment(p1, p2);
                    reFragment.speed = allyFragment.getSpeed();
                    reFragments.add(reFragment);

                    //显示某个聚类的代表轨迹
                    {
                        RoutePainter routePainter = reFragment.getRoutePainter();
                        routePainter.setColor(Color.MAGENTA);
                        painters.add(routePainter);
                        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                        Thread.sleep(100);
                    }
                }
            }


            // 步骤5 计算异常
            LAFD lafd1 = new LAFD(reFragments);
            List<TFragment> fragmentsILAF = lafd.getOutlierFragments();
            for (TFragment fragment : fragmentsILAF) {
                if (fragment.ILAF > TH_ILAF) {
                    System.out.println(fragment.getObjectId() + "的局部异常的速度：" + fragment.speed);
                    for (TFragment fr : fragmentsILAF) {
                        if (fr.getDist(fragment) < LAFD.TH_DIST) {
                            System.out.println(fragment.getObjectId() + "的周围点" + fr.getObjectId() + "的速度：" + fr.speed);
                        }
                    }

                    //显示某个异常轨迹
                    {
                        RoutePainter routePainter = fragment.getRoutePainter();
                        routePainter.setColor(Color.RED);
                        painters.add(routePainter);
                        mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
                        Thread.sleep(500);
                    }
                }
            }

        }
    }
}
