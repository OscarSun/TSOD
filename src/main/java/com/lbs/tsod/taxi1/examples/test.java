package com.lbs.tsod.taxi1.examples;

import com.lbs.tsod.taxi1.model.TPoint;
import com.lbs.tsod.taxi1.model.Trajectory;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Created by toyking on 2016/10/10.
 */
public class test {
    public static void main(String args[]) {
        Trajectory result = new Trajectory();
        result.points.add(new TPoint(new Coordinate(0, 0)));    //0 p1
        result.points.add(new TPoint(new Coordinate(1, 1)));    //1 p2
        result.points.add(new TPoint(new Coordinate(2, 2.5)));  //2 p3 -> key_point
        result.points.add(new TPoint(new Coordinate(4, 4)));    //3 p4
        result.points.add(new TPoint(new Coordinate(5, 5.5)));  //4 p5 -> key_point
        result.points.add(new TPoint(new Coordinate(6, 3.5)));  //5 p6
        result.points.add(new TPoint(new Coordinate(7, 2.9)));  //6 p7
        result.points.add(new TPoint(new Coordinate(8, 1.7)));  //7 p8
        result.points.add(new TPoint(new Coordinate(9, 0.4)));  //8 p9 -> key_point
        result.points.add(new TPoint(new Coordinate(10, 1.0))); //9 p10

        System.out.println(result.getBaseLinePartitionCost(0, 2) + " " + result.getNoPartitionCost(0, 2));
        System.out.println(result.getBaseLinePartitionCost(0, 3) + " " + result.getNoPartitionCost(0, 3));
        System.out.println(result.getBaseLinePartitionCost(0, 4) + " " + result.getNoPartitionCost(0, 4));
        System.out.println(result.getBaseLinePartitionCost(0, 5) + " " + result.getNoPartitionCost(0, 5));

        System.out.println();

        System.out.println(result.getBaseLinePartitionCost(2, 4) + " " + result.getNoPartitionCost(2, 4));
        System.out.println(result.getBaseLinePartitionCost(2, 5) + " " + result.getNoPartitionCost(2, 5));

        System.out.println();

        System.out.println(result.getBaseLinePartitionCost(4, 6) + " " + result.getNoPartitionCost(4, 6));
        System.out.println(result.getBaseLinePartitionCost(4, 7) + " " + result.getNoPartitionCost(4, 7));
        System.out.println(result.getBaseLinePartitionCost(4, 8) + " " + result.getNoPartitionCost(4, 8));
        System.out.println(result.getBaseLinePartitionCost(4, 9) + " " + result.getNoPartitionCost(4, 9));

        System.out.println();
    }
}
