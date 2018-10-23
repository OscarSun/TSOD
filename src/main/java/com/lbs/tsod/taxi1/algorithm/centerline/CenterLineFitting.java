package com.lbs.tsod.taxi1.algorithm.centerline;

import com.lbs.tsod.taxi1.osm.TNode;
import com.lbs.tsod.taxi1.osm.TWay;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CenterLineFitting {
    public final static int MIN_LNS = 2; //拟合时最小需要线段
    public final static double MIN_DIFF = 2E-4; //拟合时的平滑，大约22米

    public List<TrajSegment> list_trajs;

    public CenterLineFitting() {
        list_trajs = new ArrayList<TrajSegment>();
    }

    public CenterLineFitting(List<LineSegment> list) {
        list_trajs = new ArrayList<TrajSegment>();
        for (LineSegment l : list) {
            list_trajs.add(new TrajSegment(new Point(l.p0.x, l.p0.y), new Point(l.p1.x, l.p1.y)));
        }
    }

    public TWay getCenterLine() {
        TWay way = new TWay();
        macroCluster clu = new macroCluster();
        for (int i = 0; i < list_trajs.size(); i++) {
            clu.microCluster.add(i);
        }
        computeTraj(clu, list_trajs);
        for (Point p : clu.repreTraj) {
            way.nodes.add(new TNode(new Coordinate(p.x, p.y)));
        }
        if(way.nodes.size()<=1){
            way = new TWay();
            way.nodes.add(new TNode(new Coordinate(list_trajs.get(0).start.x, list_trajs.get(0).start.y)));
            way.nodes.add(new TNode(new Coordinate(list_trajs.get(0).end.x, list_trajs.get(0).end.y)));
        }
        return way;
    }

    public double getRadianAngle(Point s, Point e) {//-180<angle<=180
        double x_gap = e.x - s.x;
        double y_gap = e.y - s.y;
        double tan = (y_gap) / (x_gap);
        Double angle = Math.atan(tan);
        if (angle.isNaN()) {
            angle = 0.0;
        } else if (x_gap < 0 && y_gap > 0) {
            angle += Math.PI;
        } else if (x_gap < 0 && y_gap < 0) {
            angle -= Math.PI;
        } else if (x_gap < 0 && y_gap == 0) {
            angle = Math.PI;
        }
        return angle;
    }

    public void computeTraj(macroCluster clu, List<TrajSegment> lines) {
        TrajSegment V = new TrajSegment();
        for (Integer i : clu.microCluster) {
            V.start.add2(lines.get(i).start);
            V.end.add2(lines.get(i).end);
        }
        V.start.x /= clu.microCluster.size();
        V.start.y /= clu.microCluster.size();
        V.end.x /= clu.microCluster.size();
        V.end.y /= clu.microCluster.size();
        double avgAngle = getRadianAngle(V.start, V.end);

        //Rotate the axes so that the X axis is parallel to V;
        //Let P be the set of the starting and ending points of the line segments in Ci;
        /*X'-value denotes the coordinate of the X' axis*/
        ArrayList<Point> P = new ArrayList<Point>();

        for (int i = 0; i < clu.microCluster.size(); i++) {
            Point pt1 = new Point();
            pt1.x = lines.get(clu.microCluster.get(i)).start.x * Math.cos(avgAngle) + lines.get(clu.microCluster.get(i)).start.y * Math.sin(avgAngle);
            pt1.y = -lines.get(clu.microCluster.get(i)).start.x * Math.sin(avgAngle) + lines.get(clu.microCluster.get(i)).start.y * Math.cos(avgAngle);
            pt1.mark = clu.microCluster.get(i);
            pt1.isStart = true;
            P.add(pt1);

            Point pt2 = new Point();
            pt2.x = lines.get(clu.microCluster.get(i)).end.x * Math.cos(avgAngle) + lines.get(clu.microCluster.get(i)).end.y * Math.sin(avgAngle);
            pt2.y = -lines.get(clu.microCluster.get(i)).end.x * Math.sin(avgAngle) + lines.get(clu.microCluster.get(i)).end.y * Math.cos(avgAngle);
            pt2.mark = clu.microCluster.get(i);
            P.add(pt2);

            lines.get(clu.microCluster.get(i)).rotateAxes(pt1, pt2);
        }
        //Sort the points in the set P by their X'-values;
        Collections.sort(P);

        ArrayList<Integer> list = new ArrayList<Integer>();//record cross line
        for (int i = 0; i < P.size(); i++) {
            /*count NUMp using a sweep line(or plane)*/
            //Let NUMp be the number of the line segments that contain the X'-value of the point p;
            Integer tmp = P.get(i).mark;//current point
            list.add(tmp);
            for (int j = 0; j < P.size() && P.get(j).x <= P.get(i).x; j++) {
                if (P.get(j).mark == P.get(i).mark) continue;
                if (P.get(j).isStart) {
                    if (lines.get(P.get(j).mark).end2.x >= P.get(i).x) {
                        Integer t = P.get(j).mark;
                        list.add(t);
                    }
                } else {
                    if (lines.get(P.get(j).mark).start2.x >= P.get(i).x) {
                        Integer t = P.get(j).mark;
                        list.add(t);
                    }
                }
            }
            if (list.size() >= MIN_LNS) {
                double diff = Double.MAX_VALUE;
                if (clu.repreTraj2.size() > 0) {
                    diff = Math.abs(P.get(i).x - clu.repreTraj2.get(clu.repreTraj2.size() - 1).x);
                }
                if (diff >= MIN_DIFF) {
                    Point avgPoint = new Point();
                    Point avgPoint2 = new Point();
                    double x = P.get(i).x;
                    double y = 0;
                    for (int l = 0; l < list.size(); l++) {
                        double eachy = 0;
                        if (lines.get(list.get(l)).start2.x == lines.get(list.get(l)).end2.x) {
                            eachy = (lines.get(list.get(l)).start2.y + lines.get(list.get(l)).end2.y) / 2;
                        } else {
                            double k = (lines.get(list.get(l)).end2.y - lines.get(list.get(l)).start2.y) / (lines.get(list.get(l)).end2.x - lines.get(list.get(l)).start2.x);
                            double b = lines.get(list.get(l)).start2.y - k * lines.get(list.get(l)).start2.x;
                            eachy = k * x + b;
                        }
                        y += eachy;
                    }
                    y /= list.size();
                    avgPoint2.x = x;
                    avgPoint2.y = y;
                    clu.insertPoint2(avgPoint2);
                    //Undo the rotation and get the point AVGp;
                    avgPoint.x = x * Math.cos(-avgAngle) + y * Math.sin(-avgAngle);
                    avgPoint.y = -x * Math.sin(-avgAngle) + y * Math.cos(-avgAngle);
                    //Append AVGp to the end of RTRi;
                    clu.insertPoint(avgPoint);
                }
            }
            list.clear();
        }
    }
}
