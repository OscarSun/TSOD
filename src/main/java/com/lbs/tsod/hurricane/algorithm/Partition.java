package com.lbs.tsod.hurricane.algorithm;

import com.lbs.tsod.hurricane.model.*;

public class Partition {
    public Trajectory trajectory;

    public Partition(){
        trajectory = new Trajectory();
    }

    public Partition(Trajectory trajectory){
        this.trajectory = trajectory;
    }

    public Trajectory getTrajectoryPartition(){
        int startIndex = 0, length = 1;
        Trajectory result = new Trajectory();
        result.points.add(trajectory.points.get(0));
        while (startIndex + length < trajectory.points.size()) {
            int currentIndex = startIndex + length;
            double cost_par = trajectory.getBaseLinePartitionCost(startIndex, currentIndex);
            double cost_nopar = trajectory.getBaseLineNoPartitionCost(startIndex, currentIndex);
            if (cost_par > cost_nopar) {
                result.points.add(trajectory.points.get(currentIndex-1));
                startIndex = currentIndex-1;
                length = 1;
            } else {
                length += 1;
            }
        }
        result.points.add(trajectory.points.get(trajectory.points.size() - 1));
        return result;
    }
}
