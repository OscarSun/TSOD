package com.lbs.tsod.taxi1.file;

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

        for (int i = 0; i <= 22; i += 2) {
            String name = i + "";
            if (name.length() == 1) {
                name = "Info-0" + i + ".txt";
            } else {
                name = "Info-" + i + ".txt";
            }
            System.out.println("Reading " + name + " ...");
            BufferedReader br = new BufferedReader(new FileReader("D:\\文件资料\\实验数据\\2013-10-09\\" + name));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String str[] = line.split(",");
                //System.out.println("ID:" + str[1]);
                myset.add(str[1]);
            }
            System.out.println("Total size:" + myset.size());
        }
    }


}
