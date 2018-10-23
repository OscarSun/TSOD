package com.lbs.tsod.taxi2.algorithm.centerline;

public class Point implements Comparable<Point> {
    public double x;
    public double y;
    public int mark;
    public boolean isStart = false;

    public Point() {
        this.x = 0;
        this.y = 0;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point add(Point pt1) {
        Point res = new Point(this.x + pt1.x, this.y + pt1.y);
        return res;
    }

    public void add2(Point pt1) {
        this.x += pt1.x;
        this.y += pt1.y;
    }

    public int compareTo(Point o) {
        if (this.x < o.x) {
            return -1;
        } else if (this.x > o.x) {
            return 1;
        } else {
            return 0;
        }
    }
}
