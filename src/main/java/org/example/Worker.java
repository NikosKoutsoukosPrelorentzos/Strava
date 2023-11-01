package org.example;

import org.example.dtos.CalculatedData;
import org.example.dtos.WorkerToOutServer;
import org.example.dtos.InServerToWorker;
import org.example.entities.Waypoint;
import org.example.services.CalculateService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Worker {
    private final Socket inSocket;
    private final Socket outSocket;
    private final CalculateService calculateService;

    public Worker(Socket socket, Socket outSocket, CalculateService calculateService) {
        this.inSocket = socket;
        this.outSocket = outSocket;
        this.calculateService = calculateService;
    }

    public void waitData() {
        new Thread(() -> {
            try {
                while (inSocket.isConnected()) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(inSocket.getInputStream());
                    InServerToWorker inServerToWorker = (InServerToWorker) objectInputStream.readObject();
                    System.out.println("Incoming data to worker: ");
                    processData(inServerToWorker);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void processData(InServerToWorker inServerToWorker) {
        new Thread(() -> {
            try {
                ArrayList<Waypoint> workerWaypoints = calculateService.readGPX(inServerToWorker.gpx());
                CalculatedData calculatedData = calculateService.gpxIntermediate(workerWaypoints);
                WorkerToOutServer workerToOutServer = new WorkerToOutServer(inServerToWorker.uuid(), calculatedData, inServerToWorker.size());
                sendData(workerToOutServer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private synchronized void sendData(WorkerToOutServer workerToOutServer) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outSocket.getOutputStream());
            objectOutputStream.writeObject(workerToOutServer);
            objectOutputStream.flush();
            System.out.println("Worker sent data");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}