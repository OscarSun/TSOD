package com.lbs.tsod.taxi1.algorithm;


import com.lbs.tsod.taxi1.model.*;


public class TrajectorySimplify {
    public Trajectory trajectory;

    public TrajectorySimplify() {
        trajectory = new Trajectory();
    }

    public TrajectorySimplify(Trajectory trajectory) {
        this.trajectory = trajectory;
    }

    public Trajectory getSimplifiedTrajectoty() {
        for (int i = 0; i < trajectory.points.size(); i++) {
            if (i == 0) trajectory.points.get(0).z = 0;
            else
                trajectory.points.get(i).z = trajectory.points.get(i - 1).z + trajectory.points.get(i).distance(trajectory.points.get(i - 1));
        }
        if (trajectory.points.size() < 3) return trajectory;
        int startIndex = 0, length = 1;
        double max_off = 0;
        int max_off_index = 0;
        Trajectory result = new Trajectory();
        result.points.add(trajectory.points.get(0));
        while (startIndex + length < trajectory.points.size()) {
            int currentIndex = startIndex + length;
            double cost_par = trajectory.getPartitionCost(startIndex, currentIndex);
            double cost_nopar = trajectory.getNoPartitionCost(startIndex, currentIndex);
            if (cost_nopar - cost_par > max_off) {
                max_off = cost_nopar - cost_par;
                max_off_index = currentIndex;
            }
            if (cost_par > cost_nopar) {
                result.points.add(trajectory.points.get(max_off_index));
                startIndex = max_off_index + 1;
                length = 1;
                max_off = 0;
                max_off_index = startIndex;
            } else {
                length += 1;
            }
        }
        result.points.add(trajectory.points.get(trajectory.points.size() - 1));
        return result;
    }
}
