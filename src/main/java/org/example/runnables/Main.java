package org.example.runnables;

import org.example.Worker;
import org.example.servers.InServer;
import org.example.servers.OutServer;
import org.example.services.CalculateService;
import org.example.services.CalculateServiceImpl;
import org.example.services.ChunksService;
import org.example.services.ChunksServiceImpl;
import org.example.utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        int numberOfWorkers = 10;
        int numberOfPorts = numberOfWorkers * 2;
        ArrayList<Integer> workersPorts = new ArrayList<>(numberOfPorts);
        for (int i = 0; i < numberOfPorts; i++) {
            workersPorts.add(Utils.getRandomPort());
        }

        ServerSocket inServerSocket = new ServerSocket(1234);
        ChunksService chunksService = new ChunksServiceImpl();
        InServer inServer = new InServer(inServerSocket, workersPorts, chunksService);
        inServer.startServer();

        ServerSocket outServerSocket = new ServerSocket(1235);
        OutServer outServer = new OutServer(outServerSocket, workersPorts);
        outServer.startServer();

        Thread.sleep(3000);
        CalculateService calculateService = new CalculateServiceImpl();
        for (int i = 0; i < numberOfPorts; i += 2) {
            Socket inSocket = new Socket(InetAddress.getLocalHost(), 1234, InetAddress.getLocalHost(), workersPorts.get(i));
            Socket outSocket = new Socket(InetAddress.getLocalHost(), 1235, InetAddress.getLocalHost(), workersPorts.get(i + 1));
            Worker worker = new Worker(inSocket, outSocket, calculateService);
            worker.waitData();
        }
    }
}