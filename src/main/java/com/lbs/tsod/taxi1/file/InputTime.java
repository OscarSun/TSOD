package com.lbs.tsod.taxi1.file;

import com.lbs.tsod.taxi1.model.RoutePainter;
import com.lbs.tsod.taxi1.model.TPoint;
import com.lbs.tsod.taxi1.model.Trajectory;
import com.lbs.tsod.taxi1.osm.JxmapHelper;
import com.lbs.tsod.taxi1.osm.PGOsmReader;
import com.lbs.tsod.taxi1.osm.PGParameter;
import com.lbs.tsod.taxi1.osm.TWay;
import com.vividsolutions.jts.geom.Coordinate;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputTime {
    public final static String PATH = "C:\\Users\\toyking\\Desktop\\Map\\Input\\TSOD\\";
    public final static Coordinate left_bottom = new Coordinate(121.5831, 31.2814);
    public final static Coordinate right_top = new Coordinate(121.6228, 31.3074);
    public static long time_max = 0;

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

        //获取所有范围内的道路
        List<TWay> ways_all = new ArrayList<TWay>();
        {
            String sql = PGParameter.sql_select_part;
            sql = sql.replace("x-min", left_bottom.x + "");
            sql = sql.replace("x-max", right_top.x + "");
            sql = sql.replace("y-min", left_bottom.y + "");
            sql = sql.replace("y-max", right_top.y + "");
            List<TWay> ways_tmp = PGOsmReader.read_osm_roads(sql);
            for (TWay way : ways_tmp) {
                if (way.lenth() > 1e-2) ways_all.add(way);
            }
        }
        {
            for (TWay way : ways_all) {
                RoutePainter painter = JxmapHelper.getRoutePainter(way);
                painter.setColor(Color.RED);
                painter.line_width = 3;
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }

        // 生成轨迹
        List<Trajectory> trajectories = new ArrayList<Trajectory>();
        {
            System.out.println("正在随机生成轨迹...");
            int id = 1;
            for (TWay way : ways_all) {
                for (int t = 0; t < 10; t++) {
                    Trajectory trajectory = new Trajectory();
                    long time = 0;
                    for (int i = 1; i < way.nodes.size(); i++) {
                        Coordinate c = new Coordinate(), c1 = way.nodes.get(i - 1), c2 = way.nodes.get(i);
                        double len_sum = c1.distance(c2);
                        for (double len = 0; len < len_sum; len += 3e-4 + Math.random() * 1e-3) {
                            c.x = c1.x + (c2.x - c1.x) * len / len_sum + (Math.random() - 0.5) * 2e-4;
                            c.y = c1.y + (c2.y - c1.y) * len / len_sum + (Math.random() - 0.5) * 2e-4;
                            TPoint point = new TPoint(c, id);
                            if (id > 10) point.speed = 10 + Math.random() * 10;
                            else point.speed = 70 + Math.random() * 10;
                            if (t == 0) point.speed += 40;
                            time += 10;
                            point.time = time;
                            trajectory.points.add(point);
                        }
                    }
                    trajectories.add(trajectory);
                    id++;
                }
            }
            System.out.println("共生成轨迹数：" + trajectories.size());
        }
        /*{
            for (Trajectory traj : trajectories) {
                RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                if (traj.getObjectID() % 4 == 0) painter.setColor(Color.BLUE);
                if (traj.getObjectID() % 4 == 1) painter.setColor(Color.RED);
                if (traj.getObjectID() % 4 == 2) painter.setColor(Color.GREEN);
                if (traj.getObjectID() % 4 == 3) painter.setColor(Color.CYAN);
                painter.line_width = 2;
                painters.add(painter);
            }
            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }*/

        // 写入文件
        {
            FileWriter fw = new FileWriter(PATH + "outlier_trajectories_time.txt");
            fw.write("id,lng,lat,speed,time\n");
            for (Trajectory traj : trajectories) {
                for (TPoint p : traj.points) {
                    fw.write(p.objectID + "," + p.x + "," + p.y + "," + p.speed + "," + p.time + "\n");
                }
            }
            fw.close();
            System.out.println("已写入文件！");
        }
    }

    public static List<Trajectory> getTaxi() throws Exception {
        Map<Integer, List<TPoint>> map = new HashMap<Integer, List<TPoint>>();
        {
            BufferedReader br = new BufferedReader(new FileReader(PATH + "outlier_trajectories_time.txt"));
            System.out.println("Header:" + br.readLine());
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String str[] = line.split(",");
                int id = Integer.parseInt(str[0]);
                Coordinate c = new Coordinate(Double.parseDouble(str[1]), Double.parseDouble(str[2]));
                TPoint point = new TPoint(c, id);
                point.speed = Double.parseDouble(str[3]);
                point.time = Long.parseLong(str[4]);
                if (point.speed > TPoint.MAX_SPEED) TPoint.MAX_SPEED = point.speed;
                if (point.speed < TPoint.MIN_SPEED) TPoint.MIN_SPEED = point.speed;
                if (point.time > time_max) time_max = point.time;
                if (!map.containsKey(id)) map.put(id, new ArrayList<TPoint>());
                map.get(id).add(point);
            }
            br.close();
        }
        List<Trajectory> trajectories = new ArrayList<Trajectory>();
        for (List<TPoint> points : map.values()) {
            Trajectory traj = new Trajectory(points);
            trajectories.add(traj);
        }
        return trajectories;
    }


}
