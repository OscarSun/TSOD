package com.lbs.tsod.taxi2.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by toyking on 2017/6/29.
 */
public class TotalTaxiCount {

    public static Set<String> myset = new HashSet<String>();

    public static void main(String args[]) throws Exception {

        System.out.println("Reading " + " ...");
        int cnt =0;
        BufferedReader br = new BufferedReader(new FileReader("D:\\文件资料\\实验数据\\part-00000\\part-00000"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (++cnt % 10000000 == 0) System.out.println(cnt + " Total cnt:" + myset.size());
            String str[] = line.split(",");
            if (str.length != 13) continue;
            myset.add(str[0]);
        }
    }
}
