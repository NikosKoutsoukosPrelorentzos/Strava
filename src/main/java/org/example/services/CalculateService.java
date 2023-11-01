package org.example.services;

import org.example.dtos.CalculatedData;
import org.example.entities.Waypoint;

import java.util.ArrayList;

public interface CalculateService {
    CalculatedData gpxIntermediate(ArrayList<Waypoint> objectsM) throws InterruptedException;
    ArrayList<Waypoint> readGPX(byte[] arrayGpx) throws Exception;
}
