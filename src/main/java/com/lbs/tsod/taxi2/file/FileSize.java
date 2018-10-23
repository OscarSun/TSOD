package com.lbs.tsod.taxi2.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by toyking on 2016/10/11.
 */
public class FileSize {
    public final static String PATH_IN1 = "E:\\Projects\\Data\\Trajectory\\shanghai\\2013-10-06-Info-all.txt";
    public final static String PATH_IN2 = "E:\\Projects\\Data\\Trajectory\\SODA\\part-00014\\part-00014";

    public static void main(String args[]) throws Exception {
        GaoDeShanghai2013();
        SodaShanghai2015();
    }

    public static void GaoDeShanghai2013() throws Exception {
        int cnt = 0;
        Set<Integer> myset = new HashSet<Integer>();
        BufferedReader br = new BufferedReader(new FileReader(PATH_IN1));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (cnt % 10000000 == 0) System.out.print(cnt + " ");
            String str[] = line.split(",");
            int id = Integer.parseInt(str[1]);
            myset.add(id);
            cnt++;
        }
        br.close();
        System.out.println("GaoDeShanghai2013 轨迹数目：" + myset.size() + " 轨迹点的数目：" + cnt);
    }

    public static void SodaShanghai2015() throws Exception {
        int cnt = 0;
        Set<Integer> myset = new HashSet<Integer>();
        BufferedReader br = new BufferedReader(new FileReader(PATH_IN2));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (cnt % 10000000 == 0) System.out.print(cnt + " ");
            String str[] = line.split(",");
            int id = Integer.parseInt(str[0]);
            myset.add(id);
            cnt++;
        }
        br.close();
        System.out.println("SodaShanghai2015 轨迹数目：" + myset.size() + " 轨迹点的数目：" + cnt);
    }
}
