package com.lbs.tsod.hurricane.file;

import com.lbs.tsod.hurricane.model.*;
import com.vividsolutions.jts.geom.Coordinate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class Input {

    public static List<Trajectory> getHurricane() throws Exception {
        List<Trajectory> result = new ArrayList<Trajectory>();
        for (int year = 1990; year <= 2000; year += 10) {
            List<Trajectory> tmp = getHurricane(year);
            for (Trajectory trajectory : tmp) {
                if (result.size() >= 221) break;
                result.add(trajectory);
            }
        }
        return result;
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

    public static List<Trajectory> getHurricane(int y) throws Exception {
        List<Trajectory> result = new ArrayList<Trajectory>();
        Trajectory trajectory = new Trajectory();
        int preId = -1;
        BufferedReader br = new BufferedReader(new FileReader("D:\\文件资料\\实验数据\\飓风数据\\" + y + ".data"));
        //BufferedReader br = new BufferedReader(new FileReader("E:\\Projects\\Data\\Trajectory\\飓风数据\\" + y + ".data"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = line.replace("NOT NAMED", "NOTNAMED");
            String str[] = line.split(" ");
            List<String> list_st = new ArrayList<String>();
            for (String st : str) {
                if (st.length() > 0) list_st.add(st);
            }
            if (list_st.size() != 10) {
                System.out.println("飓风数据有解析出错的，已被忽略");
                continue;
            }
            double lat = Double.parseDouble(list_st.get(6));
            double lng = Double.parseDouble(list_st.get(7));
            int year = Integer.parseInt(list_st.get(0));
            int month = Integer.parseInt(list_st.get(1));
            int date1 = Integer.parseInt(list_st.get(2));
            int id = year * 1000 + Integer.parseInt(list_st.get(4));
            //if (year > 2006) continue;
            @SuppressWarnings("deprecation") Date date = new Date(year, month, date1);

            if (id != preId) {
                if (trajectory.points.size() >= 2) result.add(trajectory);
                trajectory = new Trajectory();
                preId = id;
            }
            TPoint tPoint = new TPoint(new Coordinate(lng, lat), id, date.getTime());
            tPoint.wind = Double.parseDouble(list_st.get(8));
            tPoint.pre = Double.parseDouble(list_st.get(9));
            if (tPoint.wind > TPoint.MAX_WIND) TPoint.MAX_WIND = tPoint.wind;
            if (tPoint.wind < TPoint.MIN_WIND) TPoint.MIN_WIND = tPoint.wind;
            if (tPoint.pre > TPoint.MAX_PRE) TPoint.MAX_PRE = tPoint.pre;
            if (tPoint.pre < TPoint.MIN_PRE) TPoint.MIN_PRE = tPoint.pre;
            trajectory.points.add(tPoint);
        }
        br.close();
        if (trajectory.points.size() >= 2) result.add(trajectory);
        return Filter(result);
    }

    public static List<Trajectory> getElk1993() throws Exception {
        List<Trajectory> result = new ArrayList<Trajectory>();
        BufferedReader br = new BufferedReader(new FileReader("E:\\Projects\\Data\\Trajectory\\动物迁徙\\Starkey_OR_Main_Telemetry_1993-1996_Data.txt"));
        System.out.println("Header:" + br.readLine());

        double GRID_MIN_X = 1e7, GRID_MAX_X = -1, GRID_MIN_Y = 1e7, GRID_MAX_Y = -1;
        Map<Integer, List<TPoint>> map = new HashMap<Integer, List<TPoint>>();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = line.replace("\"", " ");
            String str[] = line.split(",");
            if (!str[10].trim().equals("E")) continue;
            double y = Double.parseDouble(str[1].trim());
            double x = Double.parseDouble(str[2].trim());
            int id = Integer.parseInt(str[3].replace("E", "0").trim());
            Date date = new SimpleDateFormat("yyyyMMddHH:mm:ss").parse(str[7].trim() + str[8].trim());
            if (date.getYear() != 93) continue;
            if (x < GRID_MIN_X) GRID_MIN_X = x;
            if (x > GRID_MAX_X) GRID_MAX_X = x;
            if (y < GRID_MIN_Y) GRID_MIN_Y = y;
            if (y > GRID_MAX_Y) GRID_MAX_Y = y;
            if (!map.containsKey(id)) map.put(id, new ArrayList<TPoint>());
            map.get(id).add(new TPoint(new Coordinate(x, y), id, date.getTime()));
        }
        //System.out.println((GRID_MAX_X-GRID_MIN_X)+" "+(GRID_MAX_Y-GRID_MIN_Y));
        for (List<TPoint> list : map.values()) {
            Collections.sort(list, new Comparator<TPoint>() {
                @Override
                public int compare(TPoint o1, TPoint o2) {
                    if (o1.time == o2.time) return 0;
                    else if (o1.time > o2.time) return 1;
                    else return -1;
                }
            });
            for (TPoint point : list) {
                point.x = (point.x - GRID_MIN_X) / 15.0D;
                point.y = (point.y - GRID_MIN_Y) / 15.0D;
            }
            result.add(new Trajectory(list));
        }



        return Filter(result);
    }

    public static List<Trajectory> getDeer1995() throws Exception {
        List<Trajectory> result = new ArrayList<Trajectory>();
        BufferedReader br = new BufferedReader(new FileReader("E:\\Projects\\Data\\Trajectory\\动物迁徙\\Starkey_OR_Main_Telemetry_1993-1996_Data.txt"));
        System.out.println("Header:" + br.readLine());

        double GRID_MIN_X = 1e7, GRID_MAX_X = -1, GRID_MIN_Y = 1e7, GRID_MAX_Y = -1;
        Map<Integer, List<TPoint>> map = new HashMap<Integer, List<TPoint>>();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            line = line.replace("\"", " ");
            String str[] = line.split(",");
            if (!str[10].trim().equals("D")) continue;
            double y = Double.parseDouble(str[1].trim());
            double x = Double.parseDouble(str[2].trim());
            int id = Integer.parseInt(str[3].replace("D", "1").trim());
            Date date = new SimpleDateFormat("yyyyMMddHH:mm:ss").parse(str[7].trim() + str[8].trim());
            if (date.getYear() != 95) continue;
            if (x < GRID_MIN_X) GRID_MIN_X = x;
            if (x > GRID_MAX_X) GRID_MAX_X = x;
            if (y < GRID_MIN_Y) GRID_MIN_Y = y;
            if (y > GRID_MAX_Y) GRID_MAX_Y = y;
            if (!map.containsKey(id)) map.put(id, new ArrayList<TPoint>());
            map.get(id).add(new TPoint(new Coordinate(x, y), id, date.getTime()));
        }
        //System.out.println((GRID_MAX_X-GRID_MIN_X)+" "+(GRID_MAX_Y-GRID_MIN_Y));
        for (List<TPoint> list : map.values()) {
            Collections.sort(list, new Comparator<TPoint>() {
                @Override
                public int compare(TPoint o1, TPoint o2) {
                    if (o1.time == o2.time) return 0;
                    else if (o1.time > o2.time) return 1;
                    else return -1;
                }
            });
            for (TPoint point : list) {
                point.x = (point.x - GRID_MIN_X) / 15.0D;
                point.y = (point.y - GRID_MIN_Y) / 15.0D;
            }
            result.add(new Trajectory(list));
        }
        return Filter(result);
    }

}
