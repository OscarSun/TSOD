package com.lbs.tsod.taxi1.examples;

import com.lbs.tsod.taxi1.algorithm.*;
import com.lbs.tsod.taxi1.model.*;
import com.lbs.tsod.taxi1.file.*;

import com.vividsolutions.jts.geom.Coordinate;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimplifyCheck {
    public final static int MAX_CNT = 3;

    public final static String PATH_IN = "E:\\Projects\\Data\\Trajectory\\shanghai\\2013-10-08\\Info-10.txt";

    public static void main(String args[]) throws Exception {
        // 1. 获取轨迹
        List<Trajectory> trajs = Input.getTaxi();
        {
            BufferedReader br = new BufferedReader(new FileReader(PATH_IN));
            Trajectory traj = new Trajectory();
            int preTaxiId = -1;
            for (String line = br.readLine(); line != null && trajs.size() < MAX_CNT; line = br.readLine()) {
                String str[] = line.split(",");
                double lng = Double.parseDouble(str[2]);
                double lat = Double.parseDouble(str[3]);
                int taxiId = Integer.parseInt(str[1]);
                Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(str[0]);
                if (taxiId != preTaxiId) {
                    if (traj.points.size() > 0) trajs.add(traj);
                    preTaxiId = taxiId;
                    traj = new Trajectory();
                }
                traj.points.add(new TPoint(new Coordinate(lng, lat), taxiId, date.getTime() / 1000));
            }
            br.close();
        }

        // 2. 简化轨迹
        List<Trajectory> simplyfiedTrajs = new ArrayList<Trajectory>();
        {
            for (Trajectory traj : trajs) {
                TrajectorySimplify trajSimp = new TrajectorySimplify(traj);
                simplyfiedTrajs.add(trajSimp.getSimplifiedTrajectoty());
            }
        }

        // 3. 可视化输出结果
        {
            // Setup JXMapViewer
            final JXMapViewer mapViewer = new JXMapViewer();
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            GeoPosition shanghai = new GeoPosition(31.2253441, 121.4888922);

            // Set the focus
            mapViewer.setZoom(7);
            mapViewer.setAddressLocation(shanghai);

            // Add interactions
            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            // Display the viewer in a JFrame
            JFrame frame = new JFrame("轨迹简化验证");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

            // 输出简化前的轨迹
            {
                for (Trajectory traj : trajs) {
                    List<GeoPosition> positions = new ArrayList<GeoPosition>();
                    for (TPoint point : traj.points) {
                        positions.add(new GeoPosition(point.y,point.x));
                    }
                    RoutePainter routePainter = new RoutePainter(positions);
                    painters.add(routePainter);
                }
            }

            // 输出简化后的轨迹
            {
                for (Trajectory traj : simplyfiedTrajs) {
                    List<GeoPosition> positions = new ArrayList<GeoPosition>();
                    for (TPoint point : traj.points) {
                        positions.add(new GeoPosition(point.y,point.x));
                    }
                    RoutePainter routePainter = new RoutePainter(positions);
                    routePainter.setColor(Color.RED);
                    painters.add(routePainter);
                }
            }

            mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
        }
    }
}
