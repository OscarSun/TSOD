package com.lbs.tsod.animal.model;

import com.vividsolutions.jts.geom.Coordinate;

import java.math.BigDecimal;

/**
 * Created by toyking on 2016/8/10.
 */
public class TPoint extends Coordinate {
    public int objectID;
    public long time; //自某天以来此Date对象表示的秒数。

    //Taxi
    public static double MAX_SPEED = -1, MIN_SPEED = 1E7;
    public double speed;

    //Hurricane
    public static double MAX_WIND = -1, MIN_WIND = 1E7;
    public static double MAX_PRE = -1, MIN_PRE = 1E7;
    public double wind;
    public double pre;

    public TPoint() {
        super();
        this.objectID = -1;
        this.time = -1;
    }

    public TPoint(Coordinate c) {
        super(c);
        this.objectID = -1;
        this.time = -1;
    }

    public TPoint(Coordinate c, int objectID) {
        super(c);
        this.objectID = objectID;
    }

    public TPoint(Coordinate c, int objectID, long time) {
        super(c);
        this.objectID = objectID;
        this.time = time;
    }

    /**
     * 求两轨迹点之间的实际距离，单位是米
     *
     * @param e
     * @return
     */
    public double getEarthLength(Coordinate e) {
        if (this.x == e.x && this.y == e.y) return 0.0D;
        double R = 6370996.81;
        double t = Math.cos(this.y * Math.PI / 180) * Math.cos(e.y * Math.PI / 180) * Math.cos(this.x * Math.PI / 180 - e.x * Math.PI / 180) + Math.sin(this.y * Math.PI / 180) * Math.sin(e.y * Math.PI / 180);
        if (Math.abs(t) > 1) {
            BigDecimal b = new BigDecimal(t);
            t = b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();//避免t的绝对值大于1
        }
        double res = R * Math.acos(t);
        return res;
    }
}
