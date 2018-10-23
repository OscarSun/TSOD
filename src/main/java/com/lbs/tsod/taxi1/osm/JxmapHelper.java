package com.lbs.tsod.taxi1.osm;

import com.lbs.tsod.taxi1.model.RoutePainter;
import com.lbs.tsod.taxi1.model.TFragment;
import com.lbs.tsod.taxi1.model.Trajectory;
import com.vividsolutions.jts.geom.LineSegment;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;


public class JxmapHelper {

    public static RoutePainter getRoutePainter(LineSegment l) {
        List<GeoPosition> positions = new ArrayList<GeoPosition>();
        positions.add(new GeoPosition(l.p0.y, l.p0.x));
        positions.add(new GeoPosition(l.p1.y, l.p1.x));
        RoutePainter routePainter = new RoutePainter(positions);
        return routePainter;
    }

    public static RoutePainter getRoutePainter(TWay way) {
        List<GeoPosition> positions = new ArrayList<GeoPosition>();
        for (int i = 0; i < way.nodes.size(); i++) {
            positions.add(new GeoPosition(way.nodes.get(i).y, way.nodes.get(i).x));
        }
        RoutePainter routePainter = new RoutePainter(positions);
        return routePainter;
    }

    public static RoutePainter getRoutePainter(Trajectory traj) {
        List<GeoPosition> positions = new ArrayList<GeoPosition>();
        for (int i = 0; i < traj.points.size(); i++) {
            positions.add(new GeoPosition(traj.points.get(i).y, traj.points.get(i).x));
        }
        RoutePainter routePainter = new RoutePainter(positions);
        return routePainter;
    }

    public static RoutePainter getRoutePainter(TFragment fragment) {
        LineSegment l = new LineSegment(fragment.tp1, fragment.tp2);
        return getRoutePainter(l);
    }
}
