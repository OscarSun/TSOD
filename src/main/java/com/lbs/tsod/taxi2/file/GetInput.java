package com.lbs.tsod.taxi2.file;

import com.lbs.tsod.taxi2.osm.*;
import com.lbs.tsod.taxi2.algorithm.MapMatching;
import com.vividsolutions.jts.geom.Coordinate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by toyking on 2016/8/22.
 */
public class GetInput {
    public final static String PATH_IN = "E:\\Projects\\Data\\Trajectory\\shanghai\\2013-10-08\\Info-10.txt";
    public final static String PATH_OUT = "C:\\Users\\toyking\\Desktop\\Map\\Input\\TSOD\\2013-10-08-Info-10.txt";
    public final static Coordinate left_bottom = new Coordinate(121.5831, 31.2814);
    public final static Coordinate right_top = new Coordinate(121.6228, 31.3074);


    public static void main(String args[]) throws Exception {

        //获取所有范围内的道路
        List<TWay> ways_all;
        {
            String sql = PGParameter.sql_select_part;
            sql = sql.replace("x-min", left_bottom.x + "");
            sql = sql.replace("x-max", right_top.x + "");
            sql = sql.replace("y-min", left_bottom.y + "");
            sql = sql.replace("y-max", right_top.y + "");
            ways_all = PGOsmReader.read_osm_roads(sql);
        }

        //读取范围内所有的轨迹
        List<TNode> nodes_all = new ArrayList<TNode>();
        {
            BufferedReader br = new BufferedReader(new FileReader(PATH_IN));
            WGSTOGCJ02 wgstogcj02 = new WGSTOGCJ02();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String str[] = line.split(",");

                Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(str[0]);
                int id = Integer.parseInt(str[1]);
                Coordinate c = new Coordinate(Double.parseDouble(str[2]), Double.parseDouble(str[3]));

                //高德坐标系转地球坐标系
                double lng = wgstogcj02.gcj2wgs(c.x, c.y).x;
                double lat = wgstogcj02.gcj2wgs(c.x, c.y).y;

                if (lng < left_bottom.x || lng > right_top.x || lat < left_bottom.y || lat > right_top.y) continue;

                nodes_all.add(new TNode(id, new Coordinate(lng, lat), date.getTime()));
            }
            br.close();
        }


        //地图匹配
        MapMatching mapMatching = new MapMatching(nodes_all, ways_all, 1E-4);
        List<TNode> result = mapMatching.getMapMatchedTrajs();

        //输出结果到文件
        FileWriter fw = new FileWriter(PATH_OUT);
        fw.write("Header：taxiID,longitude,latitude,timestamp\r\n");
        for (TNode nd : result) {
            fw.write(nd.id + "," + nd.x + "," + nd.y + "," + nd.time + "\r\n");
        }
        fw.close();
    }

}

