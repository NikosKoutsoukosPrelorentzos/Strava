package org.example.servers;

import org.example.dtos.ClientToServers;
import org.example.dtos.InServerToWorker;
import org.example.services.ChunksService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class InServer {
    private final ServerSocket serverSocket;
    private final ArrayList<Integer> workersPorts;
    private final ArrayList<Socket> workersSockets;
    private final ChunksService chunksService;

    public InServer(ServerSocket serverSocket, ArrayList<Integer> workersPorts, ChunksService chunksService) {
        this.serverSocket = serverSocket;
        this.workersPorts = workersPorts;
        this.chunksService = chunksService;
        this.workersSockets = new ArrayList<>();
    }

    public void startServer() {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    if (!workersPorts.contains(socket.getPort())) {
                        clientHandler(socket);
                    } else {
                        workerHandler(socket);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void clientHandler(Socket client) {
        new Thread(() -> {
            System.out.println("Client with port: " + client.getPort() + " connected to InServer");
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                ClientToServers clientToServers = (ClientToServers) objectInputStream.readObject();
                roundRobin(clientToServers);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("something happened");
            }
        }).start();
    }

    private void workerHandler(Socket worker) {
        workersSockets.add(worker);
        System.out.println("Worker with port: " + worker.getPort() + " connected to InServer");
    }

    private synchronized void roundRobin(ClientToServers clientToServers) throws Exception {
        int workerCounter = 0;
        List<byte[]> chunks = chunksService.createChunks(clientToServers);

        for (int j = 0; j < clientToServers.size(); j++) {
            InServerToWorker dataChunk = new InServerToWorker(clientToServers.uuid(), chunks.get(j), chunks.size());
            sendToWorker(dataChunk, workerCounter);
            if (workerCounter == workersSockets.size() - 1) {
                workerCounter = 0;
            } else {
                workerCounter++;
            }
        }
    }

    private void sendToWorker(InServerToWorker inServerToWorker, int workerCounter) throws IOException {
        Socket workerSocket = workersSockets.get(workerCounter);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(workerSocket.getOutputStream());
        objectOutputStream.writeObject(inServerToWorker);
        objectOutputStream.flush();
    }
}