package com.lbs.tsod.taxi2.file;

import com.lbs.tsod.taxi2.algorithm.MapMatching;
import com.lbs.tsod.taxi2.model.*;
import com.lbs.tsod.taxi2.model.WGSTOGCJ02;
import com.lbs.tsod.taxi2.osm.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by toyking on 2016/10/4.
 */
public class InputSoda15 {
    public final static String PATH_IN = "E:\\Projects\\Data\\Trajectory\\SODA\\part-00015\\part-00015";
    public final static String PATH_OUT = "C:\\Users\\toyking\\Desktop\\Map\\Input\\TSOD\\part-00015.txt";
    public final static Coordinate left_bottom = new Coordinate(121.4152, 31.2737);
    public final static Coordinate right_top = new Coordinate(121.4638, 31.3031);


    public static void main(String args[]) throws Exception {
        final JXMapViewer mapViewer = new JXMapViewer();
        {
            // Setup JXMapViewer
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            // Set the focus
            mapViewer.setZoom(5);
            mapViewer.setAddressLocation(new GeoPosition(left_bottom.y, left_bottom.x));

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
                if (way.lenth() > 2e-3) ways_all.add(way);
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

        //读取范围内所有的轨迹
        List<TNode> nodes_all = new ArrayList<TNode>();
        {
            Date begin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-04-15 08:00:00");
            Date end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-04-15 08:30:00");

            int cnt = 0;
            BufferedReader br = new BufferedReader(new FileReader(PATH_IN));
            //WGSTOGCJ02 wgstogcj02 = new WGSTOGCJ02();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (++cnt % 10000000 == 0) System.out.print(cnt + " ");
                String str[] = line.split(",");
                if (str.length != 13) continue;

                if (str[7].length() != "yyyy-MM-dd HH:mm:ss".length()) continue;
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str[7]);
                int id = Integer.parseInt(str[0]);
                Coordinate c = new Coordinate(Double.parseDouble(str[8]), Double.parseDouble(str[9]));
                double speed = Double.parseDouble(str[10]);
                if (date.getTime() < begin.getTime() || date.getTime() > end.getTime()) continue;

                //高德坐标系转地球坐标系
                //double lng = wgstogcj02.gcj2wgs(c.x, c.y).x;
                //double lat = wgstogcj02.gcj2wgs(c.x, c.y).y;

                if (c.x < left_bottom.x || c.x > right_top.x || c.y < left_bottom.y || c.y > right_top.y) continue;
                TNode node = new TNode(id, c, date.getTime());
                node.speed = speed;
                nodes_all.add(node);
                //if (nodes_all.size() % 1000 == 0) System.out.print(nodes_all.size());
            }
            br.close();
        }

        System.out.println("\n范围内共有轨迹：" + nodes_all.size());


        //地图匹配
        MapMatching mapMatching = new MapMatching(nodes_all, ways_all, 1E-4);
        List<TNode> result = mapMatching.getMapMatchedTrajs();

        Collections.sort(result, new Comparator<TNode>() {
            @Override
            public int compare(TNode o1, TNode o2) {
                return o1.time == o2.time ? 0 : (o1.time < o2.time ? -1 : 1);
            }
        });

        //输出结果到文件
        FileWriter fw = new FileWriter(PATH_OUT);
        fw.write("Header：taxiID,longitude,latitude,timestamp,speed\r\n");
        for (TNode nd : result) {
            fw.write(nd.id + "," + nd.x + "," + nd.y + "," + nd.time + "," + nd.speed + "\r\n");
        }
        fw.close();
    }


    public static List<Trajectory> getTaxi() throws Exception {
        Map<Integer, List<TPoint>> map = new HashMap<Integer, List<TPoint>>();
        {
            BufferedReader br = new BufferedReader(new FileReader(PATH_OUT));
            System.out.println(br.readLine());
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String str[] = line.split(",");

                int id = Integer.parseInt(str[0]);
                double lng = Double.parseDouble(str[1]);
                double lat = Double.parseDouble(str[2]);
                Date date = new Date(Long.parseLong(str[3]));

                TPoint tp = new TPoint(new Coordinate(lng, lat), id, date.getTime());
                tp.speed =Double.parseDouble(str[4]);

                if (!map.containsKey(id)) map.put(id, new ArrayList<TPoint>());
                map.get(id).add(tp);
            }
            br.close();
        }

        List<Trajectory> result = new ArrayList<Trajectory>();
        for (int id : map.keySet()) {
            Trajectory traj = new Trajectory(map.get(id));
            result.add(traj);
        }
        return result;
    }

    public static List<Trajectory> getTaxi(Date begin, Date end) throws Exception {
        Map<Integer, List<TPoint>> map = new HashMap<Integer, List<TPoint>>();
        {
            BufferedReader br = new BufferedReader(new FileReader(PATH_OUT));
            System.out.println(br.readLine());
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String str[] = line.split(",");

                int id = Integer.parseInt(str[0]);
                double lng = Double.parseDouble(str[1]);
                double lat = Double.parseDouble(str[2]);
                Date date = new Date(Long.parseLong(str[3]));
                if (date.before(begin) || date.after(end)) continue;

                TPoint tp = new TPoint(new Coordinate(lng, lat), id, date.getTime());
                tp.speed =Double.parseDouble(str[4]);

                if (!map.containsKey(id)) map.put(id, new ArrayList<TPoint>());
                map.get(id).add(tp);
            }
            br.close();
        }

        List<Trajectory> result = new ArrayList<Trajectory>();
        for (int id : map.keySet()) {
            Trajectory traj = new Trajectory(map.get(id));
            result.add(traj);
        }
        return result;
    }
}
