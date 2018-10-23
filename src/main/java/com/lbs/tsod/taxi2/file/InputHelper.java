package com.lbs.tsod.taxi2.file;

import com.lbs.tsod.taxi2.algorithm.*;
import com.lbs.tsod.taxi2.model.*;
import com.vividsolutions.jts.geom.Coordinate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by toyking on 2016/8/21.
 */
public class InputHelper {

    public final static double MIN_DIS = 1E-5;
    public final static double MAX_DIS = 1E-2;

    public final static String PATH_IN = "C:\\Users\\toyking\\Desktop\\Map\\Input\\TSOD\\2013-10-08-Info-10.txt";

    public static List<Trajectory> getSimplifiedTrajs(Date begin, Date end) throws Exception {

        List<Trajectory> simplifiedTrajs = new ArrayList<Trajectory>();
        {
            Map<Integer, List<TPoint>> map = new HashMap<Integer, List<TPoint>>();
            BufferedReader br = new BufferedReader(new FileReader(PATH_IN));
            System.out.println(br.readLine());
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String str[] = line.split(",");
                int id = Integer.parseInt(str[0]);
                double lng = Double.parseDouble(str[1]);
                double lat = Double.parseDouble(str[2]);
                long time = Long.parseLong(str[3]);
                if (time < begin.getTime() || time > end.getTime()) continue;
                TPoint tp = new TPoint(new Coordinate(lng, lat), id, time / 1000);
                if (!map.containsKey(id)) {
                    map.put(id, new ArrayList<TPoint>());
                }
                map.get(id).add(tp);
            }
            br.close();

            for (int id : map.keySet()) {
                List<TPoint> points = map.get(id);
                Trajectory traj = new Trajectory();
                traj.points.add(points.get(0));
                for (int i = 1; i < points.size(); i++) {
                    if (points.get(i).distance(traj.points.get(traj.points.size() - 1)) > MAX_DIS) {
                        TrajectorySimplify trajSimp = new TrajectorySimplify(traj);
                        simplifiedTrajs.add(trajSimp.getSimplifiedTrajectoty());
                        traj = new Trajectory();
                        traj.points.add(points.get(i));
                    } else if (points.get(i).distance(points.get(i - 1)) < MIN_DIS) {
                        continue;
                    } else {
                        traj.points.add(points.get(i));
                    }
                }
                if (traj.points.size() >= 2) {
                    TrajectorySimplify trajSimp = new TrajectorySimplify(traj);
                    simplifiedTrajs.add(trajSimp.getSimplifiedTrajectoty());
                }
            }
            //System.out.println("简化后的轨迹数：" + simplifiedTrajs.size());
        }
        return Filter(simplifiedTrajs);
    }

    public static List<Trajectory> Filter(List<Trajectory> trajs) {
        List<Trajectory> result = new ArrayList<Trajectory>();
        for (Trajectory traj : trajs) {
            List<TPoint> list = new ArrayList<TPoint>();
            for (TPoint point : traj.points) {
                if (list.size() == 0) list.add(point);
                else {
                    if (point.distance(list.get(list.size() - 1)) > 1e-6) {
                        list.add(point);
                    }
                }
            }
            result.add(new Trajectory(list));
        }
        return result;
    }

    public static List<TFragment> getSimplifiedFragment(Date begin, Date end) throws Exception {
        List<Trajectory> simplifiedTrajs = getSimplifiedTrajs(begin, end);
        List<TFragment> tFragments = new ArrayList<TFragment>();
        {
            for (Trajectory traj : simplifiedTrajs) {
                for (int i = 1; i < traj.points.size(); i++) {
                    TFragment tFragment = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                    tFragment.speed = (tFragment.tp2.z - tFragment.tp1.z) / (tFragment.tp2.time - tFragment.tp1.time);
                    tFragments.add(tFragment);
                }
            }
        }
        return tFragments;
    }
}
