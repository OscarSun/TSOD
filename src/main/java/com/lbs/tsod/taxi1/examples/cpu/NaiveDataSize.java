package com.lbs.tsod.taxi1.examples.cpu;

import com.lbs.tsod.taxi1.algorithm.DensityCaculate;
import com.lbs.tsod.taxi1.algorithm.LAFD;
import com.lbs.tsod.taxi1.algorithm.TrajectorySimplify;
import com.lbs.tsod.taxi1.file.InputDataSize;
import com.lbs.tsod.taxi1.file.InputParameter;
import com.lbs.tsod.taxi1.model.RoutePainter;
import com.lbs.tsod.taxi1.model.TFragment;
import com.lbs.tsod.taxi1.model.Trajectory;
import com.lbs.tsod.taxi1.osm.JxmapHelper;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

//检测速度异常
public class NaiveDataSize {

    public static void main(String args[]) throws Exception {
        final JXMapViewer mapViewer = new JXMapViewer();
        {
            mapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));

            mapViewer.setZoom(5);
            mapViewer.setAddressLocation(new GeoPosition(31.2814, 121.5831));

            MouseInputListener mia = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(mia);
            mapViewer.addMouseMotionListener(mia);
            mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

            JFrame frame = new JFrame("TRAOD");
            frame.setLayout(new BorderLayout());
            frame.add(mapViewer);
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();


        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double sum = 0;

        //共7天，每天的数据分为12个滑动窗口，
        for (int day = 1; day <= 7; day++) {
            // 输入轨迹
            InputDataSize.main(new String[]{});
            List<Trajectory> input_trajs = InputDataSize.getTaxi();
            System.out.println("共有轨迹数：" + input_trajs.size());
            /*{
                for (Trajectory traj : input_trajs) {
                    RoutePainter painter = JxmapHelper.getRoutePainter(traj);
                    painter.setColor(Color.GREEN);
                    painter.line_width = 1;
                    painters.add(painter);
                }
                mapViewer.setOverlayPainter(new CompoundPainter<JXMapViewer>(painters));
            }*/

            Date date_begin = new Date();

            // 简化轨迹
            List<Trajectory> simplified_trajs = new ArrayList<Trajectory>();
            {
                for (Trajectory traj : input_trajs) {
                    TrajectorySimplify simplify = new TrajectorySimplify(traj);
                    simplified_trajs.add(simplify.getSimplifiedTrajectoty());
                }
            }
            List<TFragment> simplified_fragments = new ArrayList<TFragment>();
            {
                for (Trajectory traj : simplified_trajs) {
                    for (int i = 1; i < traj.points.size(); i++) {
                        TFragment fr = new TFragment(traj.points.get(i - 1), traj.points.get(i));
                        simplified_fragments.add(fr);
                    }
                }
                System.out.println("共有轨迹分段数：" + simplified_fragments.size());
            }

            for (int i = 1; i <= 12; i++) {
                List<TFragment> input_fragments = new ArrayList<TFragment>();
                {
                    for (int t = 0; t < simplified_fragments.size(); t++) {
                        if (t % 12 == i - 1) {
                            input_fragments.add(simplified_fragments.get(t));
                        }
                    }
                }

                // 计算轨迹划分密度
                DensityCaculate caculate = new DensityCaculate(input_fragments);
                input_fragments = caculate.getDensity();

                // 轨迹分段的异常检测
                LAFD lafd = new LAFD(input_fragments);
                //lafd.TH_DIST = 4E-3;
                List<TFragment> outlier_fragments = lafd.getOutlierFragments();

                Map<Integer, Double> traj_eaf = new HashMap<Integer, Double>();
                Map<Integer, Double> traj_cnt = new HashMap<Integer, Double>();
                for (TFragment fragment : outlier_fragments) {
                    double EAF = 0D;
                    if (traj_eaf.containsKey(fragment.getObjectId())) EAF = traj_eaf.get(fragment.getObjectId());
                    EAF += fragment.ILAF;
                    traj_eaf.put(fragment.getObjectId(), EAF);

                    double cnt = 0D;
                    if (traj_cnt.containsKey(fragment.getObjectId())) cnt = traj_cnt.get(fragment.getObjectId());
                    cnt += 1.0D;
                    traj_cnt.put(fragment.getObjectId(), cnt);
                }
            }

            double time = ((new Date()).getTime() - date_begin.getTime()) / 1000.0D;
            sum += time;

            System.out.println("the " + day + "th day costing time is:" + sum);
        }
    }
}

