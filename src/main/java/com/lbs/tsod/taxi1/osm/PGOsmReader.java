package com.lbs.tsod.taxi1.osm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.sql.ResultSet;
import java.util.*;

public class PGOsmReader {

    public static List<TNodeSequence> getNodeSequenceList(String points[], String sequences[]) throws Exception {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        WKTReader reader = new WKTReader(geometryFactory);
        List<TNodeSequence> list_ns = new ArrayList<TNodeSequence>();
        for (int i = 0; i < points.length; i++) {
            Point p = (Point) reader.read(points[i]);
            Coordinate c = new Coordinate(p.getX(), p.getY());
            TNodeSequence ns = new TNodeSequence(new TNode(c), Integer.parseInt(sequences[i]));
            list_ns.add(ns);
        }
        Collections.sort(list_ns);
        return list_ns;
    }

    public static Map<Integer, TWay> read_all_osm_roads() throws Exception {
        System.out.println("正在读取数据库" + PGParameter.database + "中所有的道路....");
        Map<Integer, TWay> map = new HashMap<Integer, TWay>();
        try {
            PostgresSource source = new PostgresSource(PGParameter.host, PGParameter.port, PGParameter.database, PGParameter.user, PGParameter.password);
            source.open();
            ResultSet resultSet = source.execute(PGParameter.sql_select_all);
            while (resultSet.next()) { //way_id,[id1,id2,...idn],[p1,p2,p3...,pn]
                String way_id = resultSet.getString(1);
                String sequences[] = resultSet.getString(2).split(",");
                String points[] = resultSet.getString(3).split(",");
                List<TNodeSequence> list_ns = getNodeSequenceList(points, sequences);
                TWay TWay = new TWay(Integer.parseInt(way_id));
                for (TNodeSequence ns : list_ns) {
                    TWay.nodes.add(ns);
                }
                map.put(TWay.id, TWay);
            }
            System.out.println("读取数据库完成，共有道路:" + map.size());
            source.close();
        } catch (Exception e) {
            System.out.println("读取数据库时遇到错误:" + e.toString());
            e.printStackTrace();
        }
        return map;
    }


    public static List<TWay> read_osm_roads(String sql) throws Exception {
        System.out.println("正在读取数据库" + PGParameter.database + "中所有的相关道路....");
        List<TWay> TWays = new ArrayList<TWay>();
        try {
            PostgresSource source = new PostgresSource(PGParameter.host, PGParameter.port, PGParameter.database, PGParameter.user, PGParameter.password);
            source.open();
            ResultSet resultSet = source.execute(sql);
            while (resultSet.next()) { //way_id,[id1,id2,...idn],[p1,p2,p3...,pn]
                String way_id = resultSet.getString(1);
                String sequences[] = resultSet.getString(2).split(",");
                String points[] = resultSet.getString(3).split(",");
                List<TNodeSequence> list_ns = getNodeSequenceList(points, sequences);
                boolean flag = true;
                for (int i = 1; i < list_ns.size(); i++) {
                    if (list_ns.get(i).sequence_id != list_ns.get(i - 1).sequence_id + 1) {
                        flag = false;
                        break;
                    }
                }
                if (flag && list_ns.size() > 1) {
                    TWay TWay = new TWay(Integer.parseInt(way_id));
                    for (TNodeSequence ns : list_ns) {
                        TWay.nodes.add(ns);
                    }
                    TWays.add(TWay);
                }
            }
            System.out.println("读取数据库完成，共有道路:" + TWays.size());
            source.close();
        } catch (Exception e) {
            System.out.println("读取数据库时遇到错误:" + e.toString());
            e.printStackTrace();
        }
        return TWays;
    }
}
