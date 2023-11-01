package org.example.repositories;

import org.example.dtos.CalculatedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UsersStatistics {
    private final Map<UUID, Double> averageSpeed;
    private final Map<UUID, Double> averageDistance;
    private final Map<UUID, Double> averageTime;
    private  Double generalAverageSpeed;
    private  Double generalAverageDistance;
    private  Double generalAverageTime;

    public UsersStatistics() {
        this.averageSpeed = new HashMap<>();
        this.averageDistance = new HashMap<>();
        this.averageTime = new HashMap<>();
        this.generalAverageSpeed = 0.0;
        generalAverageDistance = 0.0;
        generalAverageTime = 0.0;
    }

    public Map<UUID, Double> getAverageSpeed() {
        return averageSpeed;
    }

    public Map<UUID, Double> getAverageDistance() {
        return averageDistance;
    }

    public Map<UUID, Double> getAverageTime() {
        return averageTime;
    }

    public Double getGeneralAverageSpeed() {
        return generalAverageSpeed;
    }

    public void setGeneralAverageSpeed(Double speed) {
        if (generalAverageSpeed.equals(0.0)) {
            generalAverageSpeed = speed;
        } else {
            generalAverageSpeed = (generalAverageSpeed + speed) / 2.0;
        }
    }

    public Double getGeneralAverageDistance() {
        return generalAverageDistance;
    }

    public void setGeneralAverageDistance(Double distance) {
        if (generalAverageDistance.equals(0.0)) {
            generalAverageDistance = distance;
        } else {
            generalAverageDistance = (generalAverageDistance + distance) / 2.0;
        }
    }

    public Double getGeneralAverageTime() {
        return generalAverageTime;
    }

    public void setGeneralAverageTime(Long time) {
        if (generalAverageTime.equals(0.0)) {
            generalAverageTime = (double) time;
        } else {
            generalAverageTime += (double) time;
            generalAverageTime /= 2;
        }
    }

    public void updateUserStatistics(UUID client, CalculatedData data) {
        Double oldAverageSpeed = getAverageSpeed().get(client);
        if (oldAverageSpeed == 0) {
            getAverageSpeed().replace(client, oldAverageSpeed, data.speed());
        } else {
            getAverageSpeed().replace(client, oldAverageSpeed, (oldAverageSpeed + data.speed()) / 2.0);
        }

        Double oldAverageTime = getAverageTime().get(client);
        if (oldAverageTime == 0) {
            getAverageTime().replace(client, oldAverageTime, (double) (data.time()));
        } else {
            getAverageTime().replace(client, oldAverageTime, (data.time() + oldAverageTime) / 2.0);
        }

        Double oldAverageDistance = getAverageDistance().get(client);
        if (oldAverageDistance == 0) {
            getAverageDistance().replace(client, oldAverageDistance, data.distance());
        } else {
            getAverageDistance().replace(client, oldAverageDistance, (oldAverageDistance + data.distance()) / 2.0);
        }

        setGeneralAverageSpeed(data.speed());
        setGeneralAverageDistance(data.distance());
        setGeneralAverageTime(data.time());

        System.out.println("---------------------+++++++++++++++++++++++++++++++++---------------------------");
        System.out.println(getAverageDistance());
        System.out.println(generalAverageTime);
        System.out.println(generalAverageSpeed);
    }
}